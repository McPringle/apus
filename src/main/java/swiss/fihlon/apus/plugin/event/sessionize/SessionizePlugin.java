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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
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
        this.eventApi = String.format("https://sessionize.com/api/v2/%s/view/Sessions", this.eventId);
    }

    @Override
    public boolean isEnabled() {
        return !eventId.equals("0");
    }


    @Override
    @NotNull public List<Session> getSessions() {
        final ArrayList<Session> sessions = new ArrayList<>();
        int lastSlotId = 0;
        try {
            final String json = getJSON();
            final JSONObject jsonObject = new JSONArray(json).getJSONObject(0);
            final JSONArray sessionizeSessions = jsonObject.getJSONArray("sessions");
            for (int counter = 0; counter < sessionizeSessions.length(); counter++) {
                JSONObject singleSession = sessionizeSessions.getJSONObject(counter);

                String id = singleSession.getString("id");
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


                Session session = new Session(id,startDate, endDate, room, title,
                        speakers.stream().map(Speaker::new).toList(), Language.EN, Track.NONE);

                sessions.add(session);
            }
            LOGGER.info("Successfully loaded {} sessions for event ID {}", sessions.size(), eventId);
        } catch (IOException | URISyntaxException | JSONException e) {
            throw new SessionImportException(String.format("Error parsing slot %d: %s", lastSlotId, e.getMessage()), e);
        }
        return sessions;
    }

    private String getJSON() throws IOException, URISyntaxException {
        LOGGER.info("Starting download of JSON for event ID {} using address {}", eventId, eventApi);
        try (InputStream in = new URI(eventApi).toURL().openStream()) {
            final String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            LOGGER.info("Successfully downloaded JSON for event ID {}", eventId);
            return json;
        }
    }
}
