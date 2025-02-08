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

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

}
