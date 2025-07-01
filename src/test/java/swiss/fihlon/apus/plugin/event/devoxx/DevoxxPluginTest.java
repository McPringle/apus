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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.utility.MountableFile;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.SessionImportException;
import swiss.fihlon.apus.event.Speaker;
import swiss.fihlon.apus.event.Track;
import swiss.fihlon.apus.util.FixedPortContainer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DevoxxPluginTest {

    private static final @NotNull String NGINX_IMAGE_NAME = "nginx:latest";
    private static final int NGINX_FIXED_PORT = 8088;
    private static final int NGINX_CONTAINER_PORT = 80;
    private static @Nullable FixedPortContainer nginxContainer;

    @BeforeAll
    static void setUp() {
        nginxContainer = new FixedPortContainer(NGINX_IMAGE_NAME, NGINX_FIXED_PORT, NGINX_CONTAINER_PORT)
                .withExposedPorts(NGINX_CONTAINER_PORT)
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource("testdata/test.svg"),
                        "/usr/share/nginx/html/test.svg"
                )
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource("testdata/test.png"),
                        "/usr/share/nginx/html/test.png"
                );
        nginxContainer.start();
    }

    @AfterAll
    static void tearDown() {
        if (nginxContainer != null) {
            nginxContainer.stop();
        }
    }

    private static Stream<Arguments> provideDataForDisabledTest() {
        return Stream.of(
                Arguments.of("", "", "Monday"),
                Arguments.of(" ", " ", "Monday"),
                Arguments.of("", "foobar", "Monday"),
                Arguments.of(" ", "foobar", "Monday"),
                Arguments.of("localhost", "", "Monday"),
                Arguments.of("localhost", "foobar", ""),
                Arguments.of("localhost", "foobar", " ")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForDisabledTest")
    void isDisabled(final @NotNull String eventApi, final @NotNull String eventId, final @NotNull String weekday) {
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
    @SuppressWarnings("ZoneIdOfZ") // because that is what the JSON interface in production uses
    void getSessions() {
        final var timezone = ZoneId.of("Z");
        final var appConfig = mock(AppConfig.class);
        final var devoxxConfig = new DevoxxConfig(
                "file:src/test/resources/testdata/devoxx.json?eventId=${event}&weekday=${weekday}",
                "BBAD", "monday");
        when(appConfig.devoxx()).thenReturn(devoxxConfig);
        when(appConfig.timezone()).thenReturn(timezone);

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
        assertEquals(ZonedDateTime.of(2024, 1, 3, 19, 0, 0, 0, timezone), session.startDate());
        assertEquals(ZonedDateTime.of(2024, 1, 3, 19, 45, 0, 0, timezone), session.endDate());
        assertEquals(new Room("Room A"), session.room());
        assertEquals("Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat", session.title());
        assertEquals(2, session.speakers().size());
        assertEquals(new Speaker("Saul Goodman", "https://foo.bar/03415469-efed-4fde-b98c-b0e2db6225e7.png"),
                session.speakers().get(0));
        assertEquals(new Speaker("Mike Ehrmantraut", "https://foo.bar/0ad4f2e3-36c6-4093-b492-8098fa7e5560.png"),
                session.speakers().get(1));
        assertEquals(Language.UNKNOWN, session.language());

        assertTracks(sessions);
    }

    private void assertTracks(final @NotNull List<Session> sessions) {
        // the first session contains a PNG wrapped in an SVG
        final var expectedPNG = """
                <svg width="500" height="500" xmlns="http://www.w3.org/2000/svg">
                    <image href="http://localhost:8088/test.png" x="0" y="0" width="40" height="40"/>
                </svg>""";
        assertEquals(expectedPNG, sessions.get(0).track().svgCode().trim());

        // the second session contains an SVG
        final var expectedSVG = """
                <svg width="100" height="100" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="50" cy="50" r="40" stroke="black" stroke-width="3" fill="blue" />
                </svg>""";
        assertEquals(expectedSVG, sessions.get(1).track().svgCode().trim());

        // all other sessions should return none
        assertEquals(Track.NONE, sessions.get(2).track());
        assertEquals(Track.NONE, sessions.get(3).track());
        assertEquals(Track.NONE, sessions.get(4).track());
        assertEquals(Track.NONE, sessions.get(5).track());
        assertEquals(Track.NONE, sessions.get(6).track());
        assertEquals(Track.NONE, sessions.get(7).track());
    }

    @Test
    void parseExceptionSessions() {
        final var appConfig = mock(AppConfig.class);
        final var devoxxConfig = new DevoxxConfig(
                "file:src/test/resources/testdata/devoxx-broken.json?eventId=${event}&weekday=${weekday}",
                "1", "monday");
        when(appConfig.devoxx()).thenReturn(devoxxConfig);

        final var devoxxPlugin = new DevoxxPlugin(appConfig);
        assertThrows(SessionImportException.class, devoxxPlugin::getSessions);
    }

}
