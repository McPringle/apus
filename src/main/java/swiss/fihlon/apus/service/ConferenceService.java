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
package swiss.fihlon.apus.service;

import jakarta.annotation.PreDestroy;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.conference.Room;
import swiss.fihlon.apus.conference.Session;
import swiss.fihlon.apus.conference.doag.ConferenceAPI;
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

    private final Configuration configuration;
    private final ScheduledFuture<?> updateScheduler;
    private List<Session> sessions;
    private List<Room> rooms;

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
        final var newSessions = new ConferenceAPI(configuration).getSessions().stream()
                .sorted()
                .toList();
        final var newRooms = newSessions.stream()
                .map(Session::room)
                .distinct()
                .sorted()
                .toList();
        synchronized (this) {
            sessions = newSessions;
            rooms = newRooms;
        }
    }

    public List<Session> getAllSessions() {
        synchronized (this) {
            return List.copyOf(sessions);
        }
    }

    public Map<Room, List<Session>> getRoomsWithSessions() {
        final LocalDateTime now = LocalDateTime.now();
        final Map<Room, List<Session>> roomsWithSessions = new TreeMap<>();
        for (final Room room : rooms) {
            roomsWithSessions.put(room, new ArrayList<>());
        }
        final List<Session> runningAndNextSessions = sessions.stream()
                .filter(session -> session.endDate().isAfter(now))
                .toList();
        for (final Session session : runningAndNextSessions) {
            roomsWithSessions.get(session.room()).add(session);
        }
        return roomsWithSessions;
    }
}
