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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


@Service
public final class SessionizePlugin implements EventPlugin {
    public static final Logger LOGGER = LoggerFactory.getLogger(SessionizePlugin.class);

    private static final int CATEGORY_ID_LANGUAGE = 68911;
    private static final int LANGUAGE_ID_ENGLISH = 242155;
    private static final int LANGUAGE_ID_GERMAN = 242156;

    private final String eventId;
    private final String eventApi;
    private final String speakerApi;

    public SessionizePlugin(@NotNull final Configuration configuration) {
        this.eventId = configuration.getSessionize().eventId();
        this.eventApi = configuration.getSessionize().eventApi();
        this.speakerApi = configuration.getSessionize().speakerApi();
    }

    @Override
    public boolean isEnabled() {
        return !eventId.equals("0");
    }


    @Override
    @NotNull
    public Stream<Session> getSessions() {
        final Map<String, Speaker> allSpeakers = getAllSpeakers();
        final ArrayList<Session> sessions = new ArrayList<>();
        String lastSessionId = "";
        try {
            final String json = DownloadUtil.getString(String.format(eventApi, eventId));
            final JSONObject jsonObject = new JSONArray(json).getJSONObject(0);
            final JSONArray sessionizeSessions = jsonObject.getJSONArray("sessions");
            for (int counter = 0; counter < sessionizeSessions.length(); counter++) {
                final JSONObject sessionData = sessionizeSessions.getJSONObject(counter);
                lastSessionId = sessionData.getString("id");
                sessions.add(getSession(sessionData, allSpeakers));
            }
            LOGGER.info("Successfully loaded {} sessions for event ID {}", sessions.size(), eventId);
        } catch (IOException | URISyntaxException | JSONException e) {
            throw new SessionImportException(String.format("Error parsing session %s: %s", lastSessionId, e.getMessage()), e);
        }
        return sessions.stream();
    }

    @NotNull
    private Map<String, Speaker> getAllSpeakers() {
        final Map<String, Speaker> allSpeakers = new HashMap<>();
        String lastSpeakerId = "";
        try {
            final String json = DownloadUtil.getString(String.format(speakerApi, eventId));
            final JSONArray speakerArray = new JSONArray(json);
            for (int counter = 0; counter < speakerArray.length(); counter++) {
                final JSONObject speakerData = speakerArray.getJSONObject(counter);
                lastSpeakerId = speakerData.getString("id");
                final String fullName = speakerData.getString("fullName");
                final String profilePicture = speakerData.getString("profilePicture");
                allSpeakers.put(lastSpeakerId, new Speaker(fullName, profilePicture));
            }
            LOGGER.info("Successfully loaded {} speakers for event ID {}", allSpeakers.size(), eventId);
        } catch (IOException | URISyntaxException | JSONException e) {
            throw new SessionImportException(String.format("Error parsing speaker %s: %s", lastSpeakerId, e.getMessage()), e);
        }
        return allSpeakers;
    }

    @NotNull
    private Session getSession(@NotNull final JSONObject sessionData, @NotNull final Map<String, Speaker> allSpeakers) {
        final JSONArray speakersData = sessionData.getJSONArray("speakers");
        final ArrayList<Speaker> speakers = new ArrayList<>(speakersData.length());
        for (int speakerCounter = 0; speakerCounter < speakersData.length(); speakerCounter++) {
            final JSONObject speakerData = speakersData.getJSONObject(speakerCounter);
            final String speakerId = speakerData.getString("id");
            final Speaker speaker = allSpeakers.get(speakerId);
            if (speaker == null) {
                throw new SessionImportException(String.format("Error parsing sessions: Can't find speaker with id %s!", speakerId));
            } else {
                speakers.add(speaker);
            }
        }

        return new Session(
                String.format("%s:%s", eventId, sessionData.getString("id")),
                LocalDateTime.parse(sessionData.getString("startsAt")),
                LocalDateTime.parse(sessionData.getString("endsAt")),
                new Room(sessionData.getString("room")),
                sessionData.getString("title"),
                speakers,
                getLanguage(sessionData),
                Track.NONE);
    }

    @NotNull
    private Language getLanguage(@NotNull final JSONObject singleSession) {
        final JSONArray categories = singleSession.getJSONArray("categories");
        for (int categoryCounter = 0; categoryCounter < categories.length(); categoryCounter++) {
            final JSONObject category = categories.getJSONObject(categoryCounter);
            if (category.getInt("id") == CATEGORY_ID_LANGUAGE) {
                final JSONArray categoryItems = category.getJSONArray("categoryItems");
                final JSONObject firstCategoryItem = categoryItems.getJSONObject(0);
                final int languageId = firstCategoryItem.getInt("id");
                return switch (languageId) {
                    case LANGUAGE_ID_ENGLISH -> Language.EN;
                    case LANGUAGE_ID_GERMAN -> Language.DE;
                    default -> throw new JSONException("Unknown language ID: " + languageId);
                };
            }
        }
        return Language.UNKNOWN;
    }
}
