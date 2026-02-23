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
package swiss.fihlon.apus.plugin.event.doag;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import swiss.fihlon.apus.util.DownloadUtil;
import swiss.fihlon.apus.util.TemplateUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

@Service
public final class DoagPlugin implements EventPlugin {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(DoagPlugin.class);

    private final int eventId;
    private final @NotNull String eventApi;

    public DoagPlugin(final @NotNull AppConfig appConfig) {
        this.eventId = appConfig.doag().eventId();
        this.eventApi = TemplateUtil.replaceVariables(appConfig.doag().eventApi(), Map.of("event", Integer.toString(eventId)));
    }

    @Override
    public boolean isEnabled() {
        return eventId > 0;
    }

    @Override
    public @NotNull Stream<@NotNull Session> getSessions() {
        final ArrayList<Session> sessions = new ArrayList<>();
        int lastSlotId = 0;
        try {
            final String json = DownloadUtil.getString(eventApi);
            final JSONObject jsonObject = new JSONObject(json);
            final JSONObject schedule = jsonObject.getJSONObject("schedule");
            final JSONObject conference = schedule.getJSONObject("conference");
            final String acronym = conference.getString("acronym");
            final JSONArray days = conference.getJSONArray("days");
            for (int dayCounter = 0; dayCounter < days.length(); dayCounter++) {
                final JSONObject day = days.getJSONObject(dayCounter);

                final JSONObject rooms = day.getJSONObject("rooms");
                final Iterator<String> roomKeys = rooms.keys();
                while (roomKeys.hasNext()) {
                    final String roomName = roomKeys.next();
                    final JSONArray slots = rooms.getJSONArray(roomName);
                    for (int slotCounter = 0; slotCounter < slots.length(); slotCounter++) {
                        final JSONObject slot = slots.getJSONObject(slotCounter);
                        lastSlotId = slot.getInt("id");
                        final Session session = createSession(slot, acronym, roomName);
                        if (checkSkipSession(slot, session)) {
                            continue;
                        }
                        sessions.add(session);
                    }
                }
            }
            LOGGER.info("Successfully loaded {} sessions for event ID {}", sessions.size(), eventId);
        } catch (IOException | URISyntaxException | JSONException e) {
            throw new SessionImportException(String.format("Error parsing slot %d: %s", lastSlotId, e.getMessage()), e);
        }
        return sessions.stream();
    }

    private static boolean checkSkipSession(final @NotNull JSONObject slot, final @NotNull Session session) {
        final var type = slot.getString("type");
        final var roomName = session.room().name();
        return !type.equalsIgnoreCase("lecture")
                || roomName.contains("Heinrich Mack");
    }

    private @NotNull Session createSession(final @NotNull JSONObject slot,
                                           final @NotNull String acronym,
                                           final @NotNull String roomName) {
        final Language language = getLanguage(slot);
        final String title = getTitle(slot, language.getLanguageCode());
        final ZonedDateTime date = ZonedDateTime.parse(slot.getString("date"));
        final Duration duration = parseDuration(slot.getString("duration"));
        final JSONArray persons = slot.getJSONArray("persons");
        final ArrayList<String> speakers = new ArrayList<>(persons.length());
        for (int personCounter = 0; personCounter < persons.length(); personCounter++) {
            final JSONObject person = persons.getJSONObject(personCounter);
            final String publicName = person.getString("public_name");
            speakers.add(publicName);
        }
        return new Session(
                String.format("%s:%d", acronym, slot.getInt("id")),
                date,
                date.plus(duration),
                new Room(roomName),
                title,
                speakers.stream().map(Speaker::new).toList(),
                language,
                Track.NONE
                );
    }

    private @NotNull Language getLanguage(final @NotNull JSONObject slot) {
        try {
            final var languageArray = slot.getJSONArray("language");
            final var languageString = languageArray.getString(0);
            final var languageCode = languageString.toLowerCase(Locale.getDefault());
            return Language.languageWithCode(languageCode);
        } catch (final JSONException e) {
            LOGGER.warn("Error reading language from slot '{}': {}", slot.getInt("id"), e.getMessage());
        }
        return Language.UNKNOWN;
    }

    private @NotNull String getTitle(final @NotNull JSONObject slot, final @NotNull String defaultLanguage) throws JSONException {
        for (final String language : List.of(defaultLanguage, "de", "en")) {
            try {
                final String title = slot.getJSONObject("title").getString(language);
                if (!title.isBlank()) {
                    return title;
                }
            } catch (final JSONException e) {
                // skip and try next language
            }
        }
        throw new JSONException(String.format("No title with language 'de' or 'en' for session '%s'", slot.getInt("id")));
    }

    @SuppressWarnings("StringSplitter") // safe to ignore here
    private @NotNull Duration parseDuration(final @NotNull String duration) {
        final var values = duration.split(":");
        final var hours = values[0];
        final var minutes = values[1];
        return Duration.ofHours(Long.parseLong(hours)).plusMinutes(Long.parseLong(minutes));
    }
}
