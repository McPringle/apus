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
package swiss.fihlon.apus.conference.doag;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import swiss.fihlon.apus.conference.Session;
import swiss.fihlon.apus.conference.SessionImportException;
import swiss.fihlon.apus.configuration.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Period;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ConferenceAPI {

    public static final String CONFERENCE_API_LOCATION = "https://meine.doag.org/api/event/action.getCPEventAgenda/eventId.%d/";

    private final String location;

    public ConferenceAPI(@NotNull final Configuration configuration) {
        this.location = String.format(CONFERENCE_API_LOCATION, configuration.getDoag().eventId());
    }

    public List<Session> getSessions() {
        final ArrayList<Session> sessions = new ArrayList<>();
        int lastSlotId = 0;
        try {
            final String json = getJSON();
            final JSONObject jsonObject = new JSONObject(json);
            final JSONObject schedule = jsonObject.getJSONObject("schedule");
            final JSONObject conference = schedule.getJSONObject("conference");
            final String acronym = conference.getString("acronym");
            final JSONArray days = conference.getJSONArray("days");
            for (int dayCounter = 0; dayCounter < days.length(); dayCounter++) {
                final JSONObject day = days.getJSONObject(dayCounter);

                // modify session date to today so we have live test data
                final Period daysBetween = LocalDate.now().until(LocalDate.of(2024, Month.APRIL, 9));
                final LocalDate date = LocalDate.parse(day.getString("date")).minus(daysBetween);

                final JSONObject rooms = day.getJSONObject("rooms");
                @SuppressWarnings("unchecked")
                final Iterator<String> roomKeys = rooms.keys();
                while (roomKeys.hasNext()) {
                    final String room = roomKeys.next();
                    if (room.contains("info°center") || room.contains("ring°kartbahn") || room.contains("ring°boulevard")) {
                        continue;
                    }
                    final JSONArray slots = rooms.getJSONArray(room);
                    for (int slotCounter = 0; slotCounter < slots.length(); slotCounter++) {
                        final JSONObject slot = slots.getJSONObject(slotCounter);
                        lastSlotId = slot.getInt("id");
                        final String type = slot.getString("type");
                        if (!"lecture".equalsIgnoreCase(type)) {
                            continue;
                        }
                        final String language = getLanguage(slot);
                        final String title = getTitle(slot, language);
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
                                room,
                                title,
                                String.join(", ", speakers));
                        sessions.add(session);
                    }
                }
            }
        } catch (IOException | URISyntaxException | JSONException e) {
            throw new SessionImportException(String.format("Error parsing slot %d: %s", lastSlotId, e.getMessage()), e);
        }
        return sessions;
    }

    private String getLanguage(@NotNull final JSONObject slot) {
        try {
            return slot.getJSONArray("language").getString(0);
        } catch (final JSONException e) {
            return "de";
        }
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
        try (InputStream in = new URI(location).toURL().openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
