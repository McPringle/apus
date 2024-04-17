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
package swiss.fihlon.apus.plugin.conference;

import jakarta.annotation.PreDestroy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.conference.Room;
import swiss.fihlon.apus.conference.Session;
import swiss.fihlon.apus.conference.SessionImportException;
import swiss.fihlon.apus.plugin.conference.doag.DoagPlugin;
import swiss.fihlon.apus.configuration.Configuration;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;

@Service
public final class ConferenceService {

    private static final Duration UPDATE_FREQUENCY = Duration.ofMinutes(5);
    private static final Logger LOGGER = LoggerFactory.getLogger(ConferenceService.class);

    private final Configuration configuration;
    private final ScheduledFuture<?> updateScheduler;
    private Map<Room, List<Session>> roomsWithSessions = new TreeMap<>();

    public ConferenceService(@NotNull final TaskScheduler taskScheduler,
                             @NotNull final Configuration configuration) {
        this.configuration = configuration;
        updateSessions();
        updateScheduler = taskScheduler.scheduleAtFixedRate(this::updateSessions, UPDATE_FREQUENCY);
    }

    @PreDestroy
    public void stopUpdateScheduler() {
        updateScheduler.cancel(true);
    }

    private void updateSessions() {
        try {
            final var sessions = new DoagPlugin(configuration).getSessions().stream()
                    .sorted()
                    .toList();

            final var rooms = sessions.stream()
                    .map(Session::room)
                    .distinct()
                    .sorted()
                    .toList();

            final LocalDateTime now = LocalDateTime.now();
            final Map<Room, List<Session>> newRoomsWithSessions = new TreeMap<>();
            for (final Room room : rooms) {
                newRoomsWithSessions.put(room, new ArrayList<>());
            }
            final List<Session> runningAndNextSessions = sessions.stream()
                    .filter(session -> session.endDate().isAfter(now))
                    .toList();
            for (final Session session : runningAndNextSessions) {
                newRoomsWithSessions.get(session.room()).add(session);
            }

            synchronized (this) {
                roomsWithSessions = newRoomsWithSessions;
            }
        } catch (final SessionImportException e) {
            LOGGER.error("Failed to import sessions: {}", e.getMessage());
        }
    }

    public Map<Room, List<Session>> getRoomsWithSessions() {
        synchronized (this) {
            return new TreeMap<>(roomsWithSessions);
        }
    }
}
