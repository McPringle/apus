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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.NoOpTaskScheduler;
import swiss.fihlon.apus.MemoryAppender;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.SessionImportException;
import swiss.fihlon.apus.event.Speaker;
import swiss.fihlon.apus.event.Track;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventServiceTest {

    static AppConfig mockConfiguration(@NotNull final Period dateAdjust, boolean demoMode) {
        final var eventConfig = new EventConfig(dateAdjust, "", 60,
                true, true, 0);
        final var appConfig = mock(AppConfig.class);
        when(appConfig.demoMode()).thenReturn(demoMode);
        when(appConfig.event()).thenReturn(eventConfig);
        return appConfig;
    }

    @Test
    void getRoomsWithSessionsInDemoMode() {
        // TestEventPlugin should be ignored and replaced by EventDemoPlugin
        final EventService eventService = new EventService(
                new NoOpTaskScheduler(), mockConfiguration(Period.ZERO, true), List.of(new TestEventPlugin()));
        final var roomsWithSessions = eventService.getRoomsWithSessions();

        // The result should be four rooms
        assertEquals(4, roomsWithSessions.size());

        // There should be exactly 32 sessions from the EventDemoPlugin
        final var sessionCount = roomsWithSessions.values()
                .stream()
                .flatMap(List::stream)
                .map(Session::id)
                .filter(id -> id.startsWith("DEMO-"))
                .count();
        assertEquals(32, sessionCount);
    }

    @Test
    void getRoomsWithSessions() {
        // TestEventPlugin creates a shuffled list of sessions...
        final EventService eventService = new EventService(
                new NoOpTaskScheduler(), mockConfiguration(Period.ZERO, false), List.of(new TestEventPlugin()));

        // ...which is sorted and grouped by the EventService.
        final var roomsWithSessions = eventService.getRoomsWithSessions();

        // The result be two rooms...
        assertEquals(2, roomsWithSessions.size());

        // ... where the first room starts with session 0 ...
        final var room0 = roomsWithSessions.get(new Room("Room 0"));
        assertEquals(3, room0.size());
        assertEquals("TEST0", room0.getFirst().id());

        // ... and the second room starts with session -1.
        final var room1 = roomsWithSessions.get(new Room("Room 1"));
        assertEquals(3, room1.size());
        assertEquals("TEST-1", room1.getFirst().id());
    }

    @Test
    void noEventPluginEnabled() {
        final MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        @SuppressWarnings("LoggerInitializedWithForeignClass") final Logger logger = (Logger) LoggerFactory.getLogger(EventService.class);
        logger.addAppender(memoryAppender);

        memoryAppender.start();
        final var eventService = new EventService(
                new NoOpTaskScheduler(), mockConfiguration(Period.ZERO, false), List.of(new DisabledEventPlugin()));
        eventService.stopUpdateScheduler();
        memoryAppender.stop();

        final int errorCount = memoryAppender.searchMessages("No event plugin is enabled. No agenda will be displayed.", Level.WARN).size();
        assertEquals(1, errorCount);
    }

    @Test
    void getSessionsWithDateAdjust() {
        final var expectedDate = LocalDate.now().plusDays(10);
        final var eventService = new EventService(
                new NoOpTaskScheduler(), mockConfiguration(Period.ofDays(10), false), List.of(new NowEventPlugin()));
        final var roomsWithSessions = eventService.getRoomsWithSessions();
        for (final var sessions : roomsWithSessions.values()) {
            for (final var session : sessions) {
                assertEquals(expectedDate, session.startDate().toLocalDate());
            }
        }
    }

    @Test
    void importExceptionHandling() {
        final MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        @SuppressWarnings("LoggerInitializedWithForeignClass") final Logger logger = (Logger) LoggerFactory.getLogger(EventService.class);
        logger.addAppender(memoryAppender);

        memoryAppender.start();
        new EventService(new NoOpTaskScheduler(), mockConfiguration(Period.ZERO, false), List.of(new ExceptionEventPlugin()));
        memoryAppender.stop();

        final int errorCount = memoryAppender.searchMessages("Failed to import sessions", Level.ERROR).size();
        assertEquals(1, errorCount);
    }

    /*
     * This is the test data creator.
     */
    static final class TestEventPlugin implements EventPlugin {

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public @NotNull Stream<Session> getSessions() {
            final List<Session> sessions = new ArrayList<>();
            for (int i = -5; i <= 5; i++) {
                sessions.add(createSession(i, LocalDateTime.now().minusHours(i)));
            }
            Collections.shuffle(sessions);
            return sessions.stream();
        }

        private @NotNull Session createSession(final int i, @NotNull final LocalDateTime startDate) {
            final var id = "TEST" + i;
            final var endDate = startDate.plusHours(1);
            final var room = new Room("Room " + (Math.abs(i) % 2));
            final var title = "Session " + i;
            final var speakers = List.of(new Speaker("Speaker 1"));
            final var language = Language.EN;
            final var track = Track.NONE;
            return new Session(id, startDate, endDate, room, title, speakers, language, track);
        }

    }

    static final class ExceptionEventPlugin implements EventPlugin {

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public @NotNull Stream<Session> getSessions() {
            throw new SessionImportException("This is a test", new RuntimeException());
        }

    }

    static final class DisabledEventPlugin implements EventPlugin {

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public @NotNull Stream<Session> getSessions() {
            throw new SessionImportException("This method should never be called", new RuntimeException());
        }

    }

    static final class NowEventPlugin implements EventPlugin {

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public @NotNull Stream<Session> getSessions() {
            final var id = "TEST-0";
            final var startDate = LocalDateTime.now();
            final var endDate = startDate.plusHours(1);
            final var room = new Room("Room X");
            final var title = "Test Session";
            final var speakers = List.of(new Speaker("Speaker 1"));
            final var language = Language.EN;
            final var track = Track.NONE;
            final var session = new Session(id, startDate, endDate, room, title, speakers, language, track);
            return Stream.of(session);
        }

    }

}
