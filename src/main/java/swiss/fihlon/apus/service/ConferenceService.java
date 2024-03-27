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
import swiss.fihlon.apus.conference.Session;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

import static java.util.stream.Collectors.groupingBy;

@Service
public final class ConferenceService {

    private static final Duration UPDATE_FREQUENCY = Duration.ofMinutes(5);

    private final ScheduledFuture<?> updateScheduler;
    private List<Session> sessions;

    public ConferenceService(@NotNull final TaskScheduler taskScheduler) {
        updateSessions();
        updateScheduler = taskScheduler.scheduleAtFixedRate(this::updateSessions, UPDATE_FREQUENCY);
    }

    @PreDestroy
    public void stopUpdateScheduler() {
        updateScheduler.cancel(true);
    }

    private void updateSessions() {
        final var newSessions = generateSampleData().stream()
                .sorted()
                .toList();
        synchronized (this) {
            sessions = newSessions;
        }
    }

    public List<Session> getAllSessions() {
        synchronized (this) {
            return List.copyOf(sessions);
        }
    }

    public List<Session> getRunningSessions() {
        final LocalDateTime now = LocalDateTime.now();
        return getAllSessions().stream()
                .filter(session -> session.startDate().isBefore(now) && session.endDate().isAfter(now))
                .toList();
    }

    public List<Session> getFutureSessions() {
        final LocalDateTime now = LocalDateTime.now();
        return getAllSessions().stream()
                .filter(session -> session.startDate().isAfter(now))
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
        return nextSessions.stream()
                .sorted()
                .toList();
    }

    @SuppressWarnings("java:S125")
    private List<Session> generateSampleData() {
        final int sampleDataSize = 100;
        final int sampleSessionParallel = 15;
        final int sampleDuration = 10;

        final List<Session> sampleData = new ArrayList<>(sampleDataSize);

        LocalDateTime startDate = LocalDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS)
                .withSecond(0);
        while (startDate.getMinute() % 5 != 0) {
            startDate = startDate.minusMinutes(1);
        }

        while (sampleData.size() < sampleDataSize) {
            for (int counter = 0; counter < sampleSessionParallel; counter++) {
                final int index = sampleData.size();
                final String id = UUID.randomUUID().toString();
                final LocalDateTime endDate = startDate.plusMinutes(sampleDuration);
                final String room = String.valueOf((char) ('A' + counter));
                // final String random = RandomStringUtils.random(RandomUtils.nextInt(1, 150), "abcd efghi jklmn opqrst uvwxyz ");
                // final String title = random + " Test Session #" + index;
                final String title = "Test Session #" + index;
                final String speaker = "Speaker #" + (counter + 1);
                sampleData.add(new Session(id, startDate, endDate, room, title, speaker));
                if (sampleData.size() >= sampleDataSize) {
                    break;
                }
            }
            startDate = startDate.plusMinutes(sampleDuration);
        }

        return sampleData;
    }

}
