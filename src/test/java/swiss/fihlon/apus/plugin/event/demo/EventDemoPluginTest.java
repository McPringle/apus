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
package swiss.fihlon.apus.plugin.event.demo;

import org.junit.jupiter.api.Test;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.plugin.event.EventConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventDemoPluginTest {

    @Test
    void isEnabled() {
        final var eventConfig = mock(EventConfig.class);
        when(eventConfig.demoRoomCount()).thenReturn(1);
        final var configuration = mock(Configuration.class);
        when(configuration.getEvent()).thenReturn(eventConfig);

        final var demoEventPlugin = new EventDemoPlugin(configuration);
        assertTrue(demoEventPlugin.isEnabled());
    }

    @Test
    void isDisabled() {
        final var eventConfig = mock(EventConfig.class);
        when(eventConfig.demoRoomCount()).thenReturn(0);
        final var configuration = mock(Configuration.class);
        when(configuration.getEvent()).thenReturn(eventConfig);

        final var demoEventPlugin = new EventDemoPlugin(configuration);
        assertFalse(demoEventPlugin.isEnabled());
    }

    @Test
    void getSessionsForOneRoom() {
        final var eventConfig = mock(EventConfig.class);
        when(eventConfig.demoRoomCount()).thenReturn(1);
        final var configuration = mock(Configuration.class);
        when(configuration.getEvent()).thenReturn(eventConfig);

        final var demoEventPlugin = new EventDemoPlugin(configuration);
        final var sessions = demoEventPlugin.getSessions().toList();
        assertEquals(24, sessions.size());

        // no duplicate ids and speaker, exactly 24 entries (one room, each hour one session for 24 hours)
        assertEquals(24, sessions.stream().map(Session::id).distinct().count());
        assertEquals(24, sessions.stream().flatMap(session -> session.speakers().stream()).distinct().count());
        assertEquals(1, sessions.stream().map(Session::room).distinct().count());
    }

    /**
     * This test is run with three rooms. The plugin creates closed rooms
     * (rooms without a session) every even hour in every even room number.
     * Therefore, with 3 rooms we'll end up with 60 sessions instead of 72.
     * 12 session slots not assigned (see `continue` statement in plugin).
     */
    @Test
    void getSessionsForMultipleRooms() {
        final var eventConfig = mock(EventConfig.class);
        when(eventConfig.demoRoomCount()).thenReturn(3);
        final var configuration = mock(Configuration.class);
        when(configuration.getEvent()).thenReturn(eventConfig);

        final var demoEventPlugin = new EventDemoPlugin(configuration);
        final var sessions = demoEventPlugin.getSessions().toList();
        assertEquals(60, sessions.size());

        // no duplicate ids and speaker, exactly 240 entries (three rooms, each hour one session for 24 hours)
        assertEquals(60, sessions.stream().map(Session::id).distinct().count());
        assertEquals(60, sessions.stream().flatMap(session -> session.speakers().stream()).distinct().count());
        assertEquals(3, sessions.stream().map(Session::room).distinct().count());
    }
}
