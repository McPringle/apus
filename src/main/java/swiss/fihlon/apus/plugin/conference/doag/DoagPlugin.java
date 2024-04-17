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
package swiss.fihlon.apus.plugin.conference.doag;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swiss.fihlon.apus.conference.Language;
import swiss.fihlon.apus.conference.Room;
import swiss.fihlon.apus.conference.Session;
import swiss.fihlon.apus.conference.SessionImportException;
import swiss.fihlon.apus.conference.Speaker;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.plugin.conference.ConferencePlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class DoagPlugin implements ConferencePlugin {

    public static final Logger LOGGER = LoggerFactory.getLogger(DoagPlugin.class);
    public static final String CONFERENCE_API_LOCATION = "https://meine.doag.org/api/event/action.getCPEventAgenda/eventId.%d/";

    private final int eventId;

    public DoagPlugin(@NotNull final Configuration configuration) {
        this.eventId = configuration.getDoag().eventId();
    }

    @NotNull public List<Session> getSessions() {
        if (eventId == 0) {
            return List.of();
        }

        final ArrayList<Session> sessions = new ArrayList<>();
        int lastSlotId = 0;
        try {
            LOGGER.info("Starting download of sessions for event ID {}", eventId);
            final String json = getJSON();
            final JSONObject jsonObject = new JSONObject(json);
            final JSONObject schedule = jsonObject.getJSONObject("schedule");
            final JSONObject conference = schedule.getJSONObject("conference");
            final String acronym = conference.getString("acronym");
            final JSONArray days = conference.getJSONArray("days");
            for (int dayCounter = 0; dayCounter < days.length(); dayCounter++) {
                final JSONObject day = days.getJSONObject(dayCounter);
                final LocalDate date = LocalDate.parse(day.getString("date"));

                final JSONObject rooms = day.getJSONObject("rooms");
                final Iterator<String> roomKeys = rooms.keys();
                while (roomKeys.hasNext()) {
                    final String roomName = roomKeys.next();
                    if (roomName.contains("info°center") || roomName.contains("ring°kartbahn") || roomName.contains("ring°boulevard")) {
                        continue;
                    }
                    final JSONArray slots = rooms.getJSONArray(roomName);
                    for (int slotCounter = 0; slotCounter < slots.length(); slotCounter++) {
                        final JSONObject slot = slots.getJSONObject(slotCounter);
                        lastSlotId = slot.getInt("id");
                        final String type = slot.getString("type");
                        if (!"lecture".equalsIgnoreCase(type)) {
                            continue;
                        }
                        final Language language = getLanguage(slot);
                        final String title = getTitle(slot, language.getLanguageCode());
                        final LocalTime startTime = LocalTime.parse(slot.getString("start"));
                        final Duration duration = parseDuration(slot.getString("duration"));
                        final JSONArray persons = slot.getJSONArray("persons");
                        final ArrayList<String> speakers = new ArrayList<>(persons.length());
                        for (int personCounter = 0; personCounter < persons.length(); personCounter++) {
                            final JSONObject person = persons.getJSONObject(personCounter);
                            final String publicName = person.getString("public_name");
                            speakers.add(publicName);
                        }
                        final Session session = new Session(
                                String.format("%s:%d", acronym, slot.getInt("id")),
                                LocalDateTime.of(date, startTime),
                                LocalDateTime.of(date, startTime).plus(duration),
                                new Room(roomName),
                                title,
                                speakers.stream().map(Speaker::new).toList(),
                                language);
                        sessions.add(session);
                    }
                }
            }
            LOGGER.info("Successfully loaded {} sessions for event ID {}", sessions.size(), eventId);
        } catch (IOException | URISyntaxException | JSONException e) {
            throw new SessionImportException(String.format("Error parsing slot %d: %s", lastSlotId, e.getMessage()), e);
        }
        return sessions;
    }

    private Language getLanguage(@NotNull final JSONObject slot) {
        String languageCode = "de";
        try {
            languageCode = slot.getJSONArray("language").getString(0);
        } catch (final JSONException e) {
            LOGGER.error("Error reading language from slot '{}': {}",
                    slot.getInt("id"), e.getMessage());
        }
        return Language.languageWithCode(languageCode);
    }

    private String getTitle(@NotNull final JSONObject slot, @NotNull final String defaultLanguage) throws JSONException {
        for (final String language : List.of(defaultLanguage, "de", "en")) {
            try {
                final String title = slot.getJSONObject("title").getString(language);
                if (title != null && !title.isBlank()) {
                    return title;
                }
            } catch (final JSONException e) {
                // skip and try next language
            }
        }
        throw new JSONException(String.format("No title with language 'de' or 'en' for session '%s'", slot.getString("id")));
    }

    private Duration parseDuration(@NotNull final String duration) {
        final String minutes = duration.split(":")[1];
        return Duration.ofMinutes(Long.parseLong(minutes));
    }

    private String getJSON() throws IOException, URISyntaxException {
        final var location = String.format(CONFERENCE_API_LOCATION, eventId);
        try (InputStream in = new URI(location).toURL().openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
