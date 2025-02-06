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
package swiss.fihlon.apus.plugin.event.devoxx;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.event.*;
import swiss.fihlon.apus.plugin.event.EventPlugin;
import swiss.fihlon.apus.util.DownloadUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


@Service
public final class DevoxxPlugin implements EventPlugin {
    public static final Logger LOGGER = LoggerFactory.getLogger(DevoxxPlugin.class);

    private final String eventApi;

    public DevoxxPlugin(@NotNull final Configuration configuration) {
        this.eventApi = configuration.getDevoxx().eventApi();
    }

    @Override
    public boolean isEnabled() {
        return !eventApi.isBlank();
    }


    @Override
    @NotNull
    public Stream<Session> getSessions() {
        final ArrayList<Session> sessions = new ArrayList<>();
        String lastSessionId = "";
        try {
            final String json = DownloadUtil.getString(eventApi);
            final JSONArray devoxxSessions = new JSONArray(json);
            for (int counter = 0; counter < devoxxSessions.length(); counter++) {
                final JSONObject sessionData = devoxxSessions.getJSONObject(counter);
                if (sessionData.isNull("proposal") ) continue;
                JSONObject proposal = sessionData.getJSONObject("proposal");
                Session session = new Session(
                        Integer.toString(sessionData.getInt("id")),
                        ZonedDateTime.parse(sessionData.getString("fromDate")).toLocalDateTime(),
                        ZonedDateTime.parse(sessionData.getString("toDate")).toLocalDateTime(),
                        new Room(sessionData.getJSONObject("room").getString("name")),
                        proposal.getString("title"),
                        getSpeakers(proposal.getJSONArray("speakers")),
                        Language.UNKNOWN,
                        Track.NONE);
                sessions.add(session);
            }
            //LOGGER.info("Successfully loaded {} sessions for event ID {}", sessions.size(), eventId);
        } catch (IOException | URISyntaxException | JSONException e) {
            throw new SessionImportException(String.format("Error parsing session %s: %s", lastSessionId, e.getMessage()), e);
        }
        return sessions.stream();
    }

    private @NotNull List<Speaker> getSpeakers(JSONArray speakersData) {
        final ArrayList<Speaker> speakers = new ArrayList<>();
        for(int counter = 0; counter < speakersData.length(); counter++){
            JSONObject speakerData = speakersData.getJSONObject(counter);
            speakers.add(new Speaker(speakerData.getString("fullName"), speakerData.getString("imageUrl")));
        }
        return speakers;
    }

}
