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
package swiss.fihlon.apus.plugin.event;

import jakarta.annotation.PreDestroy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.SessionImportException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.plugin.event.demo.EventDemoPlugin;

@Service
public final class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    private final List<EventPlugin> eventPlugins;
    private final ScheduledFuture<?> updateScheduler;
    private final Period dateAdjust;
    private Map<Room, List<Session>> roomsWithSessions = new TreeMap<>();

    public EventService(@NotNull final TaskScheduler taskScheduler,
                        @NotNull final AppConfig appConfig,
                        @NotNull final List<EventPlugin> eventPlugins) {
        final var demoMode = appConfig.demoMode();
        this.eventPlugins = demoMode ? List.of(new EventDemoPlugin(appConfig)) : eventPlugins;
        this.dateAdjust = demoMode ? Period.ZERO : appConfig.event().dateAdjust();
        if (isEnabled()) {
            updateSessions();
            final var updateFrequency = Duration.ofMinutes(appConfig.event().updateFrequency());
            if (updateFrequency.isPositive()) {
                updateScheduler = taskScheduler.scheduleAtFixedRate(this::updateSessions, updateFrequency);
            } else {
                updateScheduler = null;
            }
        } else {
            LOGGER.warn("No event plugin is enabled. No agenda will be displayed.");
            updateScheduler = null;
        }
    }

    @PreDestroy
    public void stopUpdateScheduler() {
        if (updateScheduler != null) {
            updateScheduler.cancel(true);
        }
    }

    private void updateSessions() {
        try {
            final var sessions = eventPlugins.parallelStream()
                    .filter(EventPlugin::isEnabled)
                    .flatMap(EventPlugin::getSessions)
                    .map(this::dateAdjust)
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

    private Session dateAdjust(@NotNull final Session session) {
        if (dateAdjust.isZero()) {
            return session;
        }
        return new Session(
                session.id(),
                session.startDate().plus(dateAdjust),
                session.endDate().plus(dateAdjust),
                session.room(),
                session.title(),
                session.speakers(),
                session.language(),
                session.track()
        );
    }

    public Map<Room, List<Session>> getRoomsWithSessions() {
        synchronized (this) {
            return new TreeMap<>(roomsWithSessions);
        }
    }

    public boolean isEnabled() {
        return eventPlugins.stream().anyMatch(EventPlugin::isEnabled);
    }
}
