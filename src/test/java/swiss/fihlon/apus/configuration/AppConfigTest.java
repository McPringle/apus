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
package swiss.fihlon.apus.configuration;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AppConfigTest {

    @Autowired
    private @NotNull AppConfig appConfig;

    private static @NotNull Stream<Arguments> provideDataForLocaleTest() {
        return Stream.of(
                Arguments.of(Locale.GERMAN, "DE"),
                Arguments.of(Locale.GERMAN, "de"),
                Arguments.of(Locale.ENGLISH, "EN"),
                Arguments.of(Locale.ENGLISH, "en"),
                Arguments.of(Locale.ENGLISH, "FR"),
                Arguments.of(Locale.ENGLISH, "fr"),
                Arguments.of(Locale.ENGLISH, " "),
                Arguments.of(Locale.ENGLISH, "")
        );
    }

    private @NotNull AppConfig createAppConfig(final @NotNull String language) {
        return new AppConfig(appConfig.version(), language, appConfig.timezone(), appConfig.password(),
                appConfig.demoMode(), appConfig.styles(), appConfig.event(), appConfig.social(),
                appConfig.devoxx(), appConfig.doag(), appConfig.jfs(), appConfig.sessionize(),
                appConfig.blueSky(), appConfig.mastodon());
        }

    @ParameterizedTest
    @MethodSource("provideDataForLocaleTest")
    void locale(final @NotNull Locale expectedLocale, final @NotNull String language) {
        assertEquals(expectedLocale, createAppConfig(language).locale());
    }

}
