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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.SessionImportException;
import swiss.fihlon.apus.plugin.event.demo.EventDemoPlugin;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;

@Service
public final class EventService {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    private final @NotNull List<@NotNull EventPlugin> eventPlugins;
    private final @Nullable ScheduledFuture<?> updateScheduler;
    private final @NotNull Period dateAdjust;
    private final @NotNull Duration timeAdjust;
    private final @NotNull ZoneId timezone;
    private @NotNull Map<@NotNull Room, @NotNull List<@NotNull Session>> roomsWithSessions = new TreeMap<>();

    public EventService(final @NotNull TaskScheduler taskScheduler,
                        final @NotNull AppConfig appConfig,
                        final @NotNull List<@NotNull EventPlugin> eventPlugins) {
        final var demoMode = appConfig.demoMode();
        this.eventPlugins = demoMode ? List.of(new EventDemoPlugin(appConfig)) : eventPlugins;
        this.dateAdjust = demoMode ? Period.ZERO : appConfig.event().dateAdjust();
        this.timeAdjust = demoMode ? Duration.ZERO : appConfig.event().timeAdjust();
        this.timezone = appConfig.timezone();
        if (isEnabled()) {
            updateSessions();
            final var updateFrequency = Duration.ofMinutes(appConfig.event().updateFrequency());
            if (updateFrequency.isPositive()) {
                final var startTime = Instant.now().plus(updateFrequency);
                updateScheduler = taskScheduler.scheduleAtFixedRate(this::updateSessions, startTime, updateFrequency);
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

            final ZonedDateTime now = ZonedDateTime.now(timezone);
            final Map<Room, List<Session>> newRoomsWithSessions = new TreeMap<>();
            final List<Session> runningAndNextSessions = sessions.stream()
                    .filter(session -> session.endDate().isAfter(now))
                    .toList();
            for (final Session session : runningAndNextSessions) {
                final var roomWithSessions = newRoomsWithSessions.get(session.room());
                if (roomWithSessions != null) {
                    roomWithSessions.add(session);
                } else {
                    newRoomsWithSessions.put(session.room(), new ArrayList<>(List.of(session)));
                }
            }

            newRoomsWithSessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());

            synchronized (this) {
                roomsWithSessions = newRoomsWithSessions;
            }
        } catch (final SessionImportException e) {
            LOGGER.error("Failed to import sessions: {}", e.getMessage());
        }
    }

    private @NotNull Session dateAdjust(@NotNull final Session session) {
        if (dateAdjust.isZero() && timeAdjust.isZero()) {
            return session;
        }
        return new Session(
                session.id(),
                session.startDate()
                        .plus(dateAdjust)
                        .plus(timeAdjust),
                session.endDate()
                        .plus(dateAdjust)
                        .plus(timeAdjust),
                session.room(),
                session.title(),
                session.speakers(),
                session.language(),
                session.track()
        );
    }

    public @NotNull Map<@NotNull Room, @NotNull List<@NotNull Session>> getRoomsWithSessions() {
        synchronized (this) {
            return new TreeMap<>(roomsWithSessions);
        }
    }

    public boolean isEnabled() {
        return eventPlugins.stream().anyMatch(EventPlugin::isEnabled);
    }
}
