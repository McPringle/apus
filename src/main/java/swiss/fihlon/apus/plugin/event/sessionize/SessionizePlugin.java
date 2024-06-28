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
package swiss.fihlon.apus.plugin.event.sessionize;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import swiss.fihlon.apus.util.DownloadUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public final class SessionizePlugin implements EventPlugin {
    public static final Logger LOGGER = LoggerFactory.getLogger(SessionizePlugin.class);

    private final String eventId;
    private final String eventApi;

    public SessionizePlugin(@NotNull final Configuration configuration) {
        this.eventId = configuration.getSessionize().eventId();
        this.eventApi = configuration.getSessionize().eventApi();
    }

    @Override
    public boolean isEnabled() {
        return !eventId.equals("0");
    }


    @Override
    @NotNull public List<Session> getSessions() {
        final ArrayList<Session> sessions = new ArrayList<>();
        String lastSessionId = "";
        try {
            final String json = DownloadUtil.getString(String.format(eventApi, eventId));
            final JSONObject jsonObject = new JSONArray(json).getJSONObject(0);
            final JSONArray sessionizeSessions = jsonObject.getJSONArray("sessions");
            for (int counter = 0; counter < sessionizeSessions.length(); counter++) {
                JSONObject singleSession = sessionizeSessions.getJSONObject(counter);
                String sessionId = singleSession.getString("id");
                lastSessionId = sessionId;
                LocalDateTime startDate = LocalDateTime.parse(singleSession.getString("startsAt"));
                if (startDate.toLocalDate().getDayOfMonth() == 16) {
                    continue;
                }
                LocalDateTime endDate = LocalDateTime.parse(singleSession.getString("endsAt"));
                Room room = new Room(singleSession.getString("room"));
                String title = singleSession.getString("title");
                final JSONArray persons = singleSession.getJSONArray("speakers");
                final ArrayList<String> speakers = new ArrayList<>(persons.length());
                for (int personCounter = 0; personCounter < persons.length(); personCounter++) {
                    final JSONObject person = persons.getJSONObject(personCounter);
                    final String publicName = person.getString("name");
                    speakers.add(publicName);
                }


                Session session = new Session(id, startDate, endDate, room, title,
                        speakers.stream().map(Speaker::new).toList(), Language.EN, Track.NONE);

                sessions.add(session);
            }
            LOGGER.info("Successfully loaded {} sessions for event ID {}", sessions.size(), eventId);
        } catch (IOException | URISyntaxException | JSONException e) {
            throw new SessionImportException(String.format("Error parsing session %s: %s", lastSessionId, e.getMessage()), e);
        }
        return sessions;
    }
}
