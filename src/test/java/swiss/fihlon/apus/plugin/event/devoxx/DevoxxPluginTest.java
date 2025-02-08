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
package swiss.fihlon.apus.plugin.event.devoxx;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.SessionImportException;
import swiss.fihlon.apus.event.Speaker;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DevoxxPluginTest {

    private static Stream<Arguments> provideDataForDisabledTest() {
        return Stream.of(
                Arguments.of(null, null, null),
                Arguments.of(null, null, ""),
                Arguments.of(null, null, " "),
                Arguments.of(null, null, "Monday"),
                Arguments.of(null, "", "Monday"),
                Arguments.of("", null, "Monday"),
                Arguments.of("", "", "Monday"),
                Arguments.of(" ", null, "Monday"),
                Arguments.of(null, " ", "Monday"),
                Arguments.of(" ", " ", "Monday"),
                Arguments.of(null, "foobar", "Monday"),
                Arguments.of("", "foobar", "Monday"),
                Arguments.of(" ", "foobar", "Monday"),
                Arguments.of("localhost", null, "Monday"),
                Arguments.of("localhost", "", "Monday"),
                Arguments.of("localhost", "foobar", null),
                Arguments.of("localhost", "foobar", ""),
                Arguments.of("localhost", "foobar", " ")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForDisabledTest")
    void isDisabled(@Nullable final String eventApi, @Nullable final String eventId, @Nullable final String weekday) {
        final var appConfig = mock(AppConfig.class);
        final var devoxxConfig = new DevoxxConfig(eventApi, eventId, weekday);
        when(appConfig.devoxx()).thenReturn(devoxxConfig);

        final var devoxxPlugin = new DevoxxPlugin(appConfig);
        assertFalse(devoxxPlugin.isEnabled());
    }

    @Test
    void isEnabled() {
        final var appConfig = mock(AppConfig.class);
        final var devoxxConfig = new DevoxxConfig("localhost", "foobar", "monday");
        when(appConfig.devoxx()).thenReturn(devoxxConfig);

        final var devoxxPlugin = new DevoxxPlugin(appConfig);
        assertTrue(devoxxPlugin.isEnabled());
    }

    @Test
    void getSessions() {
        final var appConfig = mock(AppConfig.class);
        final var devoxxConfig = new DevoxxConfig(
                "file:src/test/resources/devoxx.json?eventId=%s&weekday=%s",
                "BBAD", "monday");
        when(appConfig.devoxx()).thenReturn(devoxxConfig);

        final var devoxxPlugin = new DevoxxPlugin(appConfig);
        final var sessions = devoxxPlugin.getSessions().toList();
        assertEquals(8, sessions.size());

        final var sessionIds = sessions.stream().map(Session::id).toList();
        for (int counter = 1; counter <= 8; counter++) {
            final var sessionId = String.format("BBAD:%d", counter);
            assertTrue(sessionIds.contains(sessionId));
        }

        // full check of session with ID "BBAD:5"
        final var session = sessions.get(4);
        assertEquals("BBAD:5", session.id());
        assertEquals(LocalDateTime.of(2024, 1, 3, 19, 0), session.startDate());
        assertEquals(LocalDateTime.of(2024, 1, 3, 19, 45), session.endDate());
        assertEquals(new Room("Room A"), session.room());
        assertEquals("Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat", session.title());
        assertEquals(2, session.speakers().size());
        assertEquals(new Speaker("Saul Goodman", "https://foo.bar/03415469-efed-4fde-b98c-b0e2db6225e7.png"),
                session.speakers().get(0));
        assertEquals(new Speaker("Mike Ehrmantraut", "https://foo.bar/0ad4f2e3-36c6-4093-b492-8098fa7e5560.png"),
                session.speakers().get(1));
        assertEquals(Language.UNKNOWN, session.language());
    }

    @Test
    void parseExceptionSessions() {
        final var appConfig = mock(AppConfig.class);
        final var devoxxConfig = new DevoxxConfig(
                "file:src/test/resources/devoxx-broken.json?eventId=%s&weekday=%s",
                "1", "monday");
        when(appConfig.devoxx()).thenReturn(devoxxConfig);

        final var devoxxPlugin = new DevoxxPlugin(appConfig);
        assertThrows(SessionImportException.class, devoxxPlugin::getSessions);
    }

}
