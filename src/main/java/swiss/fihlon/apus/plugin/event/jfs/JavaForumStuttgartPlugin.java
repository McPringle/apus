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
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.SessionImportException;
import swiss.fihlon.apus.event.Speaker;
import swiss.fihlon.apus.event.Track;
import swiss.fihlon.apus.plugin.event.EventPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public final class JavaForumStuttgartPlugin implements EventPlugin {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(JavaForumStuttgartPlugin.class);

    private final @NotNull String jsonUrl;
    private final @NotNull ZoneId timezone;

    public JavaForumStuttgartPlugin(final @NotNull AppConfig appConfig) {
        jsonUrl = appConfig.jfs().jsonUrl();
        timezone = appConfig.timezone();
    }

    @Override
    public boolean isEnabled() {
        return !jsonUrl.isEmpty();
    }

    @Override
    public @NotNull Stream<@NotNull Session> getSessions() {
        List<Talk> allTalks = List.of();
        Map<String, List<String>> allAssignments = Map.of();
        Map<String, Speaker> allSpeakers = Map.of();
        final Map<String, Track> allTracks = getTracks();

        final Path jsonFile = downloadJsonFile();

        try (BufferedReader reader = Files.newBufferedReader(jsonFile)) {
            final var content = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            final var jsonArray = new JSONArray(content);

            allTalks = getTalks(jsonArray);

            if (allTalks.isEmpty()) {
                throw new SessionImportException(String.format(
                        "Error importing session data for Java Forum Stuttgart: No talks found in '%s' from '%s'! ",
                        jsonFile, jsonUrl));
            }

            allAssignments = getAssignments(jsonArray);
            allSpeakers = getSpeakers(jsonArray);

        } catch (final IOException e) {
            LOGGER.error("IO error: {}", e.getMessage());
        } catch (final JSONException e) {
            LOGGER.error("JSON error: {}", e.getMessage());
        } finally {
            try {
                Files.delete(jsonFile);
                LOGGER.info("Successfully deleted temporary database file: {}", jsonFile);
            } catch (final IOException ioe) {
                LOGGER.warn("Unable to delete temporary database file '{}' : {}", jsonFile, ioe.getMessage());
            }
        }

        LOGGER.info("Successfully imported {} sessions for Java Forum Stuttgart", allTalks.size());
        final var finalAllAssignments = allAssignments;
        final var finalAllSpeakers = allSpeakers;
        return allTalks.stream().map(talk -> mapToSession(talk, finalAllAssignments, finalAllSpeakers, allTracks));
    }

    private @NotNull Path downloadJsonFile() {
        try {
            final Path temporaryJsonFile = Files.createTempFile("jfs-", ".json");
            LOGGER.info("Start downloading JSON from {} ...", jsonUrl);
            final URI uri = URI.create(jsonUrl);
            final URL url = uri.toURL();
            try (InputStream is = url.openStream();
                 OutputStream os = Files.newOutputStream(temporaryJsonFile)) {
                is.transferTo(os);
            }
            LOGGER.info("Successfully downloaded database to temporary file {}", temporaryJsonFile);
            return temporaryJsonFile;
        } catch (final IOException e) {
            throw new SessionImportException(String.format(
                    "Error downloading JSON file from '%s': %s",
                    jsonUrl, e.getMessage()), e);
        }
    }

    private @NotNull List<@NotNull Talk> getTalks(final @NotNull JSONArray talksArray) {
        final ArrayList<Talk> talks = new ArrayList<>();

        for (int i = 0; i < talksArray.length(); i++) {
            final var obj = talksArray.getJSONObject(i);

            final String id = Integer.toString(obj.getInt("id"));
            final String title = obj.getString("title");
            final String room = obj.getString("room");
            final String topic = obj.getString("topic");
            final String timeSlot = obj.getString("timeSlot");
            final boolean isInEnglish = obj.getBoolean("isInEnglish");

            talks.add(new Talk(id, title, room, topic, timeSlot, isInEnglish));
        }

        return talks;
    }

    private @NotNull Map<@NotNull String, @NotNull List<@NotNull String>> getAssignments(final @NotNull JSONArray array) {
        final HashMap<String, List<String>> assignments = new HashMap<>();

        for (int i = 0; i < array.length(); i++) {
            final var obj = array.getJSONObject(i);
            final var talkId = Integer.toString(obj.getInt("id"));

            final ArrayList<String> speakers = new ArrayList<>();
            final var speakersArray = obj.getJSONArray("speakers");
            for (int j = 0; j < speakersArray.length(); j++) {
                final var speaker = speakersArray.getJSONObject(j);
                final var speakerId = Integer.toString(speaker.getInt("id"));
                speakers.add(speakerId);
            }

            assignments.put(talkId, speakers);
        }

        return assignments;
    }

    private @NotNull Map<@NotNull String, @NotNull Speaker> getSpeakers(final @NotNull JSONArray array) {
        final HashMap<String, Speaker> speakers = new HashMap<>();

        for (int i = 0; i < array.length(); i++) {
            final var obj = array.getJSONObject(i);

            final var speakersArray = obj.getJSONArray("speakers");
            for (int j = 0; j < speakersArray.length(); j++) {
                final var speaker = speakersArray.getJSONObject(j);
                final var speakerId = Integer.toString(speaker.getInt("id"));
                final var name = speaker.getString("name");
                speakers.put(speakerId, new Speaker(name, null));
            }
        }
        return speakers;
    }

    private Session mapToSession(final @NotNull Talk talk,
                                 final @NotNull Map<@NotNull String, @NotNull List<@NotNull String>> allAssignments,
                                 final @NotNull Map<@NotNull String, @NotNull Speaker> allSpeakers,
                                 final @NotNull Map<@NotNull String, @NotNull Track> allTracks) {
        final var id = String.format("JFS:%s", talk.id());
        final var room = new Room(talk.room());
        final var title = talk.title();
        final var speakers = getSpeakersForTalk(talk, allAssignments, allSpeakers);
        final var startDate = getStartDate(talk).atZone(timezone);
        final var endDate = getEndDate(talk).atZone(timezone);
        final var track = getTrack(talk, allTracks);
        final var language = talk.isInEnglish() ? Language.EN : Language.DE;
        return new Session(id, startDate, endDate, room, title, speakers, language, track);
    }

    private @NotNull List<@NotNull Speaker> getSpeakersForTalk(final @NotNull Talk talk,
                                                               final @NotNull Map<@NotNull String, @NotNull List<@NotNull String>> allAssignments,
                                                               final @NotNull Map<@NotNull String, @NotNull Speaker> allSpeakers) {
        final var assignments = allAssignments.get(talk.id());
        return assignments.stream()
                .map(allSpeakers::get)
                .toList();
    }

    @SuppressWarnings("StringSplitter") // safe to ignore here
    private @NotNull LocalDateTime getStartDate(final @NotNull Talk talk) {
        final var time = LocalTime.parse(talk.timeSlot().split("-")[0].trim());
        return LocalDateTime.of(LocalDate.now(timezone), time);
    }

    @SuppressWarnings("StringSplitter") // safe to ignore here
    private @NotNull LocalDateTime getEndDate(final @NotNull Talk talk) {
        final var time = LocalTime.parse(talk.timeSlot().split("-")[1].replace("Uhr", "").trim());
        return LocalDateTime.of(LocalDate.now(timezone), time);
    }

    private @NotNull Map<@NotNull String, @NotNull Track> getTracks() {
        return Map.of(
                "Architektur & Sicherheit", new Track(TrackIcons.ARCHITECTURE_SECURITY.getSvgCode()),
                "Microservices, Container & Cloud", new Track(TrackIcons.CLOUD.getSvgCode()),
                "Core Java & JVM-Sprachen", new Track(TrackIcons.CORE_JAVA.getSvgCode()),
                "Enterprise Java & Frameworks", new Track(TrackIcons.ENTERPRISE_FRAMEWORKS.getSvgCode()),
                "Frontend-Entwicklung", new Track(TrackIcons.FRONTEND.getSvgCode()),
                "IDE & Tools", new Track(TrackIcons.IDE_TOOLS.getSvgCode()),
                "Methodik & Praxis", new Track(TrackIcons.METHODS_PRACTICE.getSvgCode()),
                "Open Source & Community", new Track(TrackIcons.OPENSOURCE.getSvgCode()),
                "Test & Betrieb", new Track(TrackIcons.TEST_BETRIEB.getSvgCode()),
                "Trends & neue Technologien (KI o.a.)", new Track(TrackIcons.TRENDS.getSvgCode())
        );
    }

    private Track getTrack(final @NotNull Talk talk, final @NotNull Map<@NotNull String, @NotNull Track> allTracks) {
        return allTracks.getOrDefault(talk.topic(), Track.NONE);
    }

}
