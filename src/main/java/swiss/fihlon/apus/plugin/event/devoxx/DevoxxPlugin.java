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
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;


@Service
public final class DevoxxPlugin implements EventPlugin {
    public static final @NotNull Logger LOGGER = LoggerFactory.getLogger(DevoxxPlugin.class);
    private static final @NotNull String PNG_SVG_WRAPPER_TEMPLATE = """
            <svg width="500" height="500" xmlns="http://www.w3.org/2000/svg">
                <image href="${PNG_URL}" x="0" y="0" width="40" height="40"/>
            </svg>""";

    private final @NotNull String eventApi;
    private final @NotNull String eventId;
    private final @NotNull String weekday;

    public DevoxxPlugin(@NotNull final AppConfig configuration) {
        this.eventApi = configuration.devoxx().eventApi();
        this.eventId = configuration.devoxx().eventId();
        this.weekday = configuration.devoxx().weekday();
    }

    @Override
    public boolean isEnabled() {
        final var eventApiOk = !eventApi.isBlank();
        final var eventIdOk = !eventId.isBlank();
        final var weekdayOk = !weekday.isBlank();
        return eventApiOk && eventIdOk && weekdayOk;
    }


    @Override
    @SuppressWarnings("java:S2142") // InterruptedException is caught and stops session loading
    public @NotNull Stream<@NotNull Session> getSessions() {
        final var sessions = new ArrayList<Session>();
        var lastSessionId = "";
        try {
            final var url = TemplateUtil.replaceVariables(
                    eventApi, Map.of("event", eventId, "weekday", weekday.toLowerCase(Locale.getDefault())));
            final var json = DownloadUtil.getString(url);
            final var devoxxSessions = new JSONArray(json);
            for (int counter = 0; counter < devoxxSessions.length(); counter++) {
                final var sessionData = devoxxSessions.getJSONObject(counter);
                if (sessionData.isNull("proposal")) {
                    continue;
                }
                lastSessionId = "%s:%d".formatted(eventId, sessionData.getInt("id"));
                var proposal = sessionData.getJSONObject("proposal");
                var session = new Session(
                        lastSessionId,
                        ZonedDateTime.parse(sessionData.getString("fromDate")),
                        ZonedDateTime.parse(sessionData.getString("toDate")),
                        new Room(sessionData.getJSONObject("room").getString("name")),
                        proposal.getString("title"),
                        getSpeakers(proposal.getJSONArray("speakers")),
                        Language.UNKNOWN, // TODO parse language #292
                        getTrack(proposal));
                sessions.add(session);
            }
            LOGGER.info("Successfully loaded {} sessions for event ID {} on {}", sessions.size(), eventId, weekday);
        } catch (final IOException | URISyntaxException | JSONException | InterruptedException e) {
            throw new SessionImportException(String.format("Error parsing session %s: %s", lastSessionId, e.getMessage()), e);
        }
        return sessions.stream();
    }

    private @NotNull List<@NotNull Speaker> getSpeakers(@NotNull final JSONArray speakersData) {
        final var speakers = new ArrayList<Speaker>();
        for (int counter = 0; counter < speakersData.length(); counter++) {
            final var speakerData = speakersData.getJSONObject(counter);
            speakers.add(new Speaker(speakerData.getString("fullName"), speakerData.getString("imageUrl")));
        }
        return speakers;
    }

    private @NotNull Track getTrack(@NotNull final JSONObject proposal) throws IOException, InterruptedException {
        if (proposal.has("track")) {
            if (!proposal.isNull("track")) {
                final var trackData = proposal.getJSONObject("track");
                if (trackData.has("imageURL")) {
                    if (!trackData.isNull("imageURL")) {
                        final var imageURL = trackData.getString("imageURL");
                        if (imageURL.length() > 4) {
                            final var extension = imageURL.substring(imageURL.length() - 4).toLowerCase(Locale.getDefault());
                            return switch (extension) {
                                case ".png" -> trackWithPNG(imageURL);
                                case ".svg" -> trackWithSVG(imageURL);
                                default -> Track.NONE;
                            };
                        }
                    }
                }
            }
        }
        return Track.NONE;
    }

    private @NotNull Track trackWithPNG(@NotNull final String imageURL) {
        final var svgCode = TemplateUtil.replaceVariables(PNG_SVG_WRAPPER_TEMPLATE, Map.of("PNG_URL", imageURL));
        return new Track(svgCode);
    }

    private @NotNull Track trackWithSVG(@NotNull final String imageURL) throws IOException, InterruptedException {
        final URI uri = URI.create(imageURL);
        return Track.fromURI(uri);
    }

}
