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
package swiss.fihlon.apus.plugin.event.doag;

import org.junit.jupiter.api.Test;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.SessionImportException;
import swiss.fihlon.apus.event.Speaker;
import swiss.fihlon.apus.configuration.Configuration;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DoagPluginTest {

    @Test
    void isEnabled() {
        final var configuration = mock(Configuration.class);
        final var doagConfig = new DoagConfig(1, "");
        when(configuration.getDoag()).thenReturn(doagConfig);

        final var doagPlugin = new DoagPlugin(configuration);
        assertTrue(doagPlugin.isEnabled());
    }

    @Test
    void isDisabled() {
        final var configuration = mock(Configuration.class);
        final var doagConfig = new DoagConfig(0, "");
        when(configuration.getDoag()).thenReturn(doagConfig);

        final var doagPlugin = new DoagPlugin(configuration);
        assertFalse(doagPlugin.isEnabled());
    }

    @Test
    void getSessions() {
        final var configuration = mock(Configuration.class);
        final var doagConfig = new DoagConfig(1, "file:src/test/resources/DOAG.json?eventId=%d");
        when(configuration.getDoag()).thenReturn(doagConfig);

        final var doagPlugin = new DoagPlugin(configuration);
        final var sessions = doagPlugin.getSessions();
        assertEquals(8, sessions.size());

        // no loop to check order of list, sorted by time of date
        assertEquals("BBAD:1", sessions.get(0).id());
        assertEquals("BBAD:2", sessions.get(1).id());
        assertEquals("BBAD:4", sessions.get(2).id());
        assertEquals("BBAD:3", sessions.get(3).id());
        assertEquals("BBAD:6", sessions.get(4).id());
        assertEquals("BBAD:5", sessions.get(5).id());
        assertEquals("BBAD:7", sessions.get(6).id());
        assertEquals("BBAD:8", sessions.get(7).id());

        // full check of session with ID "BBAD:5"
        final var session = sessions.get(5);
        assertEquals("BBAD:5", session.id());
        assertEquals(LocalDateTime.of(2024, 1, 3, 19, 0), session.startDate());
        assertEquals(LocalDateTime.of(2024, 1, 3, 19, 45), session.endDate());
        assertEquals(new Room("Room A"), session.room());
        assertEquals("Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat", session.title());
        assertEquals(2, session.speakers().size());
        assertEquals(new Speaker("Saul Goodman"), session.speakers().get(0));
        assertEquals(new Speaker("Mike Ehrmantraut"), session.speakers().get(1));
        assertEquals(Language.DE, session.language());
    }

    @Test
    void parseExceptionHandling() {
        final var configuration = mock(Configuration.class);
        final var doagConfig = new DoagConfig(1, "file:src/test/resources/DOAG-broken.json?eventId=%d");
        when(configuration.getDoag()).thenReturn(doagConfig);

        final var doagPlugin = new DoagPlugin(configuration);
        assertThrows(SessionImportException.class, doagPlugin::getSessions);
    }
}
