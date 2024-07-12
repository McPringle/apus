/*
 * Apus - A social wall for conferences with additional features.
 * Copyright (C) Marcus Fihlon and the individual contributors to Apus.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package swiss.fihlon.apus.plugin.event.jfs;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.SessionImportException;
import swiss.fihlon.apus.event.Speaker;
import swiss.fihlon.apus.event.Track;
import swiss.fihlon.apus.plugin.event.EventPlugin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public final class JavaForumStuttgartPlugin implements EventPlugin {

    public static final Logger LOGGER = LoggerFactory.getLogger(JavaForumStuttgartPlugin.class);

    private final String dbUrl;

    public JavaForumStuttgartPlugin(@NotNull final Configuration configuration) {
        dbUrl = configuration.getJfs().dbUrl();
    }

    @Override
    public boolean isEnabled() {
        return dbUrl != null && !dbUrl.isEmpty();
    }

    @Override
    public @NotNull List<Session> getSessions() {
        final ArrayList<Session> sessions = new ArrayList<>();

        final List<Talk> allTalks;
        final Map<String, List<String>> allAssignments;
        final Map<String, Speaker> allSpeakers;
        final Map<String, Track> allTracks = getTracks();

        final Path databaseFile = downloadDatabaseFile();
        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.toAbsolutePath().toFile().getAbsolutePath());
                Statement statement = connection.createStatement()
            ) {
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            allTalks = getTalks(statement);
            allAssignments = getAssignments(statement);
            allSpeakers = getSpeakers(statement);
        } catch (SQLException e) {
            // if the error message is "out of memory", it probably means no database file is found
            throw new SessionImportException(String.format(
                    "Error importing session data for Java Forum Stuttgart: %s",
                    e.getMessage()), e);
        } finally {
            try {
                Files.delete(databaseFile);
                LOGGER.info("Successfully deleted temporary database file: {}", databaseFile);
            } catch (final IOException e) {
                LOGGER.warn("Unable to delete temporary database file: {}", databaseFile);
            }
        }

        for (final Talk talk : allTalks) {
            final String id = String.format("JFS:%s", talk.id());
            final Room room = new Room(talk.room());
            final String title = talk.title();
            final List<Speaker> speakers = getSpeakersForTalk(talk, allAssignments, allSpeakers);
            final LocalDateTime startDate = getStartDate(talk);
            final LocalDateTime endDate = getEndDate(talk);
            final Track track = getTrack(talk, allTracks);

            sessions.add(new Session(id, startDate, endDate, room, title, speakers, Language.UNKNOWN, track));
        }

        LOGGER.info("Successfully imported {} sessions for Java Forum Stuttgart", sessions.size());

        return sessions;
    }

    private @NotNull Path downloadDatabaseFile() {
        try {
            final Path temporaryDatabaseFile = Files.createTempFile("jfs-", ".db");
            LOGGER.info("Start downloading database from {} ...", dbUrl);
            final URI uri = URI.create(dbUrl);
            final URL url = uri.toURL();
            try (
                    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                    FileOutputStream fos = new FileOutputStream(temporaryDatabaseFile.toFile())
            ) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
            LOGGER.info("Successfully downloaded database to temporary file {}", temporaryDatabaseFile);
            return temporaryDatabaseFile;
        } catch (final IOException e) {
            throw new SessionImportException(String.format(
                    "Error downloading database file from '%s': %s",
                    dbUrl, e.getMessage()), e);
        }
    }

    private @NotNull List<Talk> getTalks(@NotNull final Statement statement) throws SQLException {
        final ArrayList<Talk> talks = new ArrayList<>();

        final ResultSet resultSet = statement.executeQuery("""
                SELECT id, title, room, topic, timeSlot
                FROM talk
                ORDER BY timeSlot""");

        while (resultSet.next()) {
            final String id = resultSet.getString("id");
            final String title = resultSet.getString("title");
            final String room = resultSet.getString("room");
            final String topic = resultSet.getString("topic");
            final String timeSlot = resultSet.getString("timeSlot");

            talks.add(new Talk(id, title, room, topic, timeSlot));
        }

        return talks;
    }

    private @NotNull Map<String, List<String>> getAssignments(@NotNull final Statement statement) throws SQLException {
        final HashMap<String, List<String>> assignments = new HashMap<>();

        final ResultSet resultSet = statement.executeQuery("""
                SELECT speaker_id, talk_id
                FROM speakertalk""");

        while (resultSet.next()) {
            final String talkId = resultSet.getString("talk_id");
            final String speakerId = resultSet.getString("speaker_id");

            final ArrayList<String> speakers = (ArrayList<String>) assignments.getOrDefault(talkId, new ArrayList<>());
            speakers.add(speakerId);
            assignments.put(talkId, speakers);
        }

        return assignments;
    }

    private @NotNull Map<String, Speaker> getSpeakers(@NotNull final Statement statement) throws SQLException {
        final HashMap<String, Speaker> speakers = new HashMap<>();

        final ResultSet resultSet = statement.executeQuery("""
                SELECT id, name
                FROM speaker""");

        while (resultSet.next()) {
            final String id = resultSet.getString("id");
            final String name = resultSet.getString("name");

            speakers.put(id, new Speaker(name));
        }

        return speakers;
    }

    private @NotNull List<Speaker> getSpeakersForTalk(@NotNull final Talk talk,
                                             @NotNull final Map<String, List<String>> allAssignments,
                                             @NotNull final Map<String, Speaker> allSpeakers) {
        return allAssignments.get(talk.id()).stream()
                .map(allSpeakers::get)
                .toList();
    }

    private @NotNull LocalDateTime getStartDate(@NotNull final Talk talk) {
        final LocalTime time = LocalTime.parse(talk.timeSlot().split("-")[0].trim());
        return LocalDateTime.of(LocalDate.now(), time);
    }

    private @NotNull LocalDateTime getEndDate(@NotNull final Talk talk) {
        final LocalTime time = LocalTime.parse(talk.timeSlot().split("-")[1].replace("Uhr", "").trim());
        return LocalDateTime.of(LocalDate.now(), time);
    }

    private Map<String, Track> getTracks() {
        return Map.of(
                "Architektur & Sicherheit", new Track(TrackIcons.ARCHITECTURE_SECURITY.getSvgCode()),
                "Test & Betrieb", new Track(TrackIcons.BETRIEB.getSvgCode()),
                "Microservices, Container & Cloud", new Track(TrackIcons.CLOUD.getSvgCode()),
                "Core Java & JVM-Sprachen", new Track(TrackIcons.CORE_JAVA.getSvgCode()),
                "Enterprise Java & Frameworks", new Track(TrackIcons.ENTERPRISE_FRAMEWORKS.getSvgCode()),
                "Frontend-Entwicklung", new Track(TrackIcons.FRONTEND.getSvgCode()),
                "IDE & Tools", new Track(TrackIcons.IDE_TOOLS.getSvgCode()),
                "Methodik & Praxis", new Track(TrackIcons.METHODS_PRACTICE.getSvgCode()),
                "Open Source & Community", new Track(TrackIcons.OPENSOURCE.getSvgCode()),
                "Trends & neue Technologien (KI o.a.)", new Track(TrackIcons.TRENDS.getSvgCode())
        );
    }

    private Track getTrack(@NotNull final Talk talk, @NotNull final Map<String, Track> allTracks) {
        return allTracks.getOrDefault(talk.topic(), Track.NONE);
    }

}
