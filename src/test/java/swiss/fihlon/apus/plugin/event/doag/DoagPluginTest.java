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
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.SessionImportException;
import swiss.fihlon.apus.event.Speaker;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DoagPluginTest {

    @Test
    void isEnabled() {
        final var appConfig = mock(AppConfig.class);
        final var doagConfig = new DoagConfig(1, "");
        when(appConfig.doag()).thenReturn(doagConfig);

        final var doagPlugin = new DoagPlugin(appConfig);
        assertTrue(doagPlugin.isEnabled());
    }

    @Test
    void isDisabled() {
        final var appConfig = mock(AppConfig.class);
        final var doagConfig = new DoagConfig(0, "");
        when(appConfig.doag()).thenReturn(doagConfig);

        final var doagPlugin = new DoagPlugin(appConfig);
        assertFalse(doagPlugin.isEnabled());
    }

    @Test
    void getSessions() {
        final var timezone = ZoneId.of("+02:00");
        final var appConfig = mock(AppConfig.class);
        final var doagConfig = new DoagConfig(1, "file:src/test/resources/testdata/DOAG.json?eventId=${event}");
        when(appConfig.doag()).thenReturn(doagConfig);
        when(appConfig.timezone()).thenReturn(timezone);

        final var doagPlugin = new DoagPlugin(appConfig);
        final var sessions = doagPlugin.getSessions().toList();
        assertEquals(8, sessions.size());

        final var sessionIds = sessions.stream().map(Session::id).toList();
        for (int counter = 1; counter <= 8; counter++) {
            final var sessionId = String.format("BBAD:%d", counter);
            assertTrue(sessionIds.contains(sessionId));
        }

        // full check of session with ID "BBAD:5"
        final var session = sessions.get(5);
        assertEquals("BBAD:5", session.id());
        assertEquals(ZonedDateTime.of(2024, 1, 3, 19, 0, 0, 0, timezone), session.startDate());
        assertEquals(ZonedDateTime.of(2024, 1, 3, 19, 45, 0, 0, timezone), session.endDate());
        assertEquals(new Room("Room A"), session.room());
        assertEquals("Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat", session.title());
        assertEquals(2, session.speakers().size());
        assertEquals(new Speaker("Saul Goodman"), session.speakers().get(0));
        assertEquals(new Speaker("Mike Ehrmantraut"), session.speakers().get(1));
        assertEquals(Language.DE, session.language());
    }

    @Test
    void exceptionWithBrokenTitle() {
        final var appConfig = mock(AppConfig.class);
        final var doagConfig = new DoagConfig(1, "file:src/test/resources/testdata/DOAG-broken-title.json?eventId=${event}");
        when(appConfig.doag()).thenReturn(doagConfig);

        final var doagPlugin = new DoagPlugin(appConfig);
        final var exception = assertThrows(SessionImportException.class, doagPlugin::getSessions);
        assertEquals("Error parsing slot 1: No title with language 'de' or 'en' for session '1'", exception.getMessage());
    }

    @Test
    void exceptionWithBlankTitle() {
        final var appConfig = mock(AppConfig.class);
        final var doagConfig = new DoagConfig(1, "file:src/test/resources/testdata/DOAG-blank-title.json?eventId=${event}");
        when(appConfig.doag()).thenReturn(doagConfig);

        final var doagPlugin = new DoagPlugin(appConfig);
        final var exception = assertThrows(SessionImportException.class, doagPlugin::getSessions);
        assertEquals("Error parsing slot 1: No title with language 'de' or 'en' for session '1'", exception.getMessage());
    }

}
