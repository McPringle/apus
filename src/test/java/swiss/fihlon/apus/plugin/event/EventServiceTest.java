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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.NoOpTaskScheduler;
import swiss.fihlon.apus.MemoryAppender;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.SessionImportException;
import swiss.fihlon.apus.event.Speaker;
import swiss.fihlon.apus.event.Track;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventServiceTest {

    private static Configuration configurationMock;

    @BeforeAll
    static void mockConfiguration() {
        configurationMock = mock(Configuration.class);
        when(configurationMock.getEvent()).thenReturn(new EventConfig(null, 0, null, 60, true, true, 0));
    }

    @Test
    void getRoomsWithSessions() {

        // TestEventPlugin creates a shuffled list of sessions...

        final EventService eventService = new EventService(new NoOpTaskScheduler(), configurationMock, List.of(new TestEventPlugin()));

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

    @Test
    void importExceptionHandling() {
        final MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        final Logger logger = (Logger) LoggerFactory.getLogger(EventService.class);
        logger.addAppender(memoryAppender);

        memoryAppender.start();
        new EventService(new NoOpTaskScheduler(), configurationMock, List.of(new ExceptionEventPlugin()));
        memoryAppender.stop();

        final int errorCount = memoryAppender.search("Failed to import sessions", Level.ERROR).size();
        assertEquals(1, errorCount);
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

}
