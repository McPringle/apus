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
package swiss.fihlon.apus.plugin.event.sessionize;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionizePluginTest {

    @Test
    void isEnabled() {
        final var appConfig = mock(AppConfig.class);
        final var sessionizeConfig = new SessionizeConfig("1", "", "");
        when(appConfig.sessionize()).thenReturn(sessionizeConfig);

        final var sessionizePlugin = new SessionizePlugin(appConfig);
        assertTrue(sessionizePlugin.isEnabled());
    }

    @Test
    void isDisabled() {
        final var appConfig = mock(AppConfig.class);
        final var sessionizeConfig = new SessionizeConfig("0", "", "");
        when(appConfig.sessionize()).thenReturn(sessionizeConfig);

        final var sessionizePlugin = new SessionizePlugin(appConfig);
        assertFalse(sessionizePlugin.isEnabled());
    }

    @Test
    void getSessions() {
        final var timezone = ZoneId.of("Europe/Zurich");
        final var appConfig = mock(AppConfig.class);
        final var sessionizeConfig = new SessionizeConfig("BBAD",
                "file:src/test/resources/testdata/sessionize.json?eventId=${event}",
                "file:src/test/resources/testdata/sessionize-speakers.json?eventId=${event}");
        when(appConfig.sessionize()).thenReturn(sessionizeConfig);
        when(appConfig.timezone()).thenReturn(timezone);

        final var sessionizePlugin = new SessionizePlugin(appConfig);
        final var sessions = sessionizePlugin.getSessions().toList();
        assertEquals(8, sessions.size());

        final var sessionIds = sessions.stream().map(Session::id).toList();
        for (int counter = 1; counter <= 8; counter++) {
            final var sessionId = String.format("BBAD:%d", counter);
            assertTrue(sessionIds.contains(sessionId));
        }

        // full check of session with ID "BBAD:5"
        final var session = sessions.get(4);
        assertEquals("BBAD:5", session.id());
        assertEquals(ZonedDateTime.of(2024, 1, 3, 19, 0, 0, 0, timezone), session.startDate());
        assertEquals(ZonedDateTime.of(2024, 1, 3, 19, 45, 0, 0, timezone), session.endDate());
        assertEquals(new Room("Room A"), session.room());
        assertEquals("Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat", session.title());
        assertEquals(2, session.speakers().size());
        assertEquals(new Speaker("Saul Goodman", "https://foo.bar/03415469-efed-4fde-b98c-b0e2db6225e7.png"),
                session.speakers().get(0));
        assertEquals(new Speaker("Mike Ehrmantraut", "https://foo.bar/0ad4f2e3-36c6-4093-b492-8098fa7e5560.png"),
                session.speakers().get(1));
        assertEquals(Language.DE, session.language());
    }

    @Test
    void parseExceptionSessions() {
        final var timezone = ZoneId.of("Europe/Zurich");
        final var appConfig = mock(AppConfig.class);
        final var sessionizeConfig = new SessionizeConfig("1",
                "file:src/test/resources/testdata/sessionize-broken.json?eventId=${event}",
                "file:src/test/resources/testdata/sessionize-speakers.json?eventId=${event}");
        when(appConfig.sessionize()).thenReturn(sessionizeConfig);
        when(appConfig.timezone()).thenReturn(timezone);

        final var sessionizePlugin = new SessionizePlugin(appConfig);
        assertThrows(SessionImportException.class, sessionizePlugin::getSessions);
    }

    @Test
    void parseExceptionSpeakers() {
        final var timezone = ZoneId.of("Europe/Zurich");
        final var appConfig = mock(AppConfig.class);
        final var sessionizeConfig = new SessionizeConfig("1",
                "file:src/test/resources/testdata/sessionize.json?eventId=${event}",
                "file:src/test/resources/testdata/sessionize-speakers-broken.json?eventId=${event}");
        when(appConfig.sessionize()).thenReturn(sessionizeConfig);
        when(appConfig.timezone()).thenReturn(timezone);

        final var sessionizePlugin = new SessionizePlugin(appConfig);
        final var exception = assertThrows(SessionImportException.class, sessionizePlugin::getSessions);
        final var message = exception.getMessage();
        assertNotNull(message);
        assertTrue(message.startsWith("Error parsing speaker"));
    }

    @Test
    void parseExceptionUnknownSpeaker() {
        final var timezone = ZoneId.of("Europe/Zurich");
        final var appConfig = mock(AppConfig.class);
        final var sessionizeConfig = new SessionizeConfig("1",
                "file:src/test/resources/testdata/sessionize-unknown-speaker.json?eventId=${event}",
                "file:src/test/resources/testdata/sessionize-speakers.json?eventId=${event}");
        when(appConfig.sessionize()).thenReturn(sessionizeConfig);
        when(appConfig.timezone()).thenReturn(timezone);

        final var sessionizePlugin = new SessionizePlugin(appConfig);
        final var exception = assertThrows(SessionImportException.class, sessionizePlugin::getSessions);
        assertEquals("Error parsing sessions: Can't find speaker with id 2c9d3dc1-9f79-457f-0000-42ce42f6366a!", exception.getMessage());
    }

    @Test
    void parseExceptionUnknownLanguage() {
        final var timezone = ZoneId.of("Europe/Zurich");
        final var appConfig = mock(AppConfig.class);
        final var sessionizeConfig = new SessionizeConfig("1",
                "file:src/test/resources/testdata/sessionize-unknown-language.json?eventId=${event}",
                "file:src/test/resources/testdata/sessionize-speakers.json?eventId=${event}");
        when(appConfig.sessionize()).thenReturn(sessionizeConfig);
        when(appConfig.timezone()).thenReturn(timezone);

        final var sessionizePlugin = new SessionizePlugin(appConfig);
        final var exception = assertThrows(SessionImportException.class, sessionizePlugin::getSessions);
        assertEquals("Error parsing session 1: Unknown language ID: 123456", exception.getMessage());
    }

    @Test
    void unknownLanguage() {
        final var timezone = ZoneId.of("Europe/Zurich");
        final var appConfig = mock(AppConfig.class);
        final var sessionizeConfig = new SessionizeConfig("1",
                "file:src/test/resources/testdata/sessionize-no-categories.json?eventId=${event}",
                "file:src/test/resources/testdata/sessionize-speakers.json?eventId=${event}");
        when(appConfig.sessionize()).thenReturn(sessionizeConfig);
        when(appConfig.timezone()).thenReturn(timezone);

        final var sessionizePlugin = new SessionizePlugin(appConfig);
        final var sessions = sessionizePlugin.getSessions().toList();
        assertEquals(1, sessions.size());

        final var session = sessions.getFirst();
        assertEquals(Language.UNKNOWN, session.language());
    }

    @Test
    void noLanguage() {
        final var timezone = ZoneId.of("Europe/Zurich");
        final var appConfig = mock(AppConfig.class);
        final var sessionizeConfig = new SessionizeConfig("1",
                "file:src/test/resources/testdata/sessionize-no-language.json?eventId=${event}",
                "file:src/test/resources/testdata/sessionize-speakers.json?eventId=${event}");
        when(appConfig.sessionize()).thenReturn(sessionizeConfig);
        when(appConfig.timezone()).thenReturn(timezone);

        final var sessionizePlugin = new SessionizePlugin(appConfig);
        final var sessions = sessionizePlugin.getSessions().toList();
        assertEquals(1, sessions.size());

        final var session = sessions.getFirst();
        assertEquals(Language.UNKNOWN, session.language());
    }
}
