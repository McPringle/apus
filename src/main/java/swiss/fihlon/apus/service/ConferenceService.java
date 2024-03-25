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

import org.springframework.stereotype.Service;
import swiss.fihlon.apus.conference.Session;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;

@Service
public final class ConferenceService {

    private final List<Session> sessions;

    public ConferenceService() {
        sessions = new ArrayList<>(100);

        int lastMinute = -1;
        String room = "";
        for (int counter = 0; counter < 100; counter++) {
            final String id = UUID.randomUUID().toString();
            final LocalDateTime startDate = LocalDateTime.now()
                    .plusMinutes(Math.round(counter / 3f + 0.5f)) // 3 sessions start each minute
                    .truncatedTo(ChronoUnit.SECONDS)
                    .withSecond(0);
            final LocalDateTime endDate = startDate.plusMinutes(1);

            final int minute = startDate.getMinute();
            if (minute == lastMinute) {
                room = String.valueOf((char) (room.charAt(0) + 1));
            } else {
                lastMinute = minute;
                room = "A";
            }
            final String title = "Test Session #" + counter;
            final String speaker = "Speaker #" + counter;
            sessions.add(new Session(id, startDate, endDate, room, title, speaker));
        }
    }

    public List<Session> getRunningSessions() {
        final LocalDateTime now = LocalDateTime.now();
        return sessions.stream()
                .filter(session -> session.startDate().isBefore(now) && session.endDate().isAfter(now))
                .sorted()
                .toList();
    }

    public List<Session> getFutureSessions() {
        final LocalDateTime now = LocalDateTime.now();
        return sessions.stream()
                .filter(session -> session.startDate().isAfter(now))
                .sorted()
                .toList();
    }

    public List<Session> getNextSessions() {
        final LocalDate today = LocalDate.now();
        final var sessionsPerRoom = getFutureSessions().stream()
                .filter(session -> session.startDate().toLocalDate().isEqual(today))
                .collect(groupingBy(Session::room));

        final List<Session> nextSessions = new ArrayList<>(sessionsPerRoom.keySet().size());
        for (final Map.Entry<String, List<Session>> entry : sessionsPerRoom.entrySet()) {
            nextSessions.add(entry.getValue().getFirst());
        }
        return nextSessions;
    }

}
