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
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import swiss.fihlon.apus.conference.Session;
import swiss.fihlon.apus.conference.SessionImportException;

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

public final class ConferenceAPI {

    private final String location;

    public ConferenceAPI(@NotNull final String location) {
        this.location = location;
    }

    public List<Session> getSessions() {
        final ArrayList<Session> sessions = new ArrayList<>();
        try {
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
                @SuppressWarnings("unchecked")
                final Iterator<String> roomKeys = rooms.keys();
                while (roomKeys.hasNext()) {
                    final String room = roomKeys.next();
                    if (room.startsWith("info°center")) {
                        continue;
                    }
                    final JSONArray slots = rooms.getJSONArray(room);
                    for (int slotCounter = 0; slotCounter < slots.length(); slotCounter++) {
                        final JSONObject slot = slots.getJSONObject(slotCounter);
                        final String type = slot.getString("type");
                        if (!"lecture".equalsIgnoreCase(type)) {
                            continue;
                        }
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
                                slot.getString("title"),
                                String.join(", ", speakers));
                        sessions.add(session);
                    }
                }
            }
        } catch (IOException | URISyntaxException | JSONException e) {
            throw new SessionImportException(e);
        }
        return sessions;
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
