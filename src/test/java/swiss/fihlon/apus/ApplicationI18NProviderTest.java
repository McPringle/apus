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
package swiss.fihlon.apus;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.configuration.CustomConfig;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationI18NProviderTest {

    private AppConfig createLanguageConfig(@NotNull final String language) {
        final var customConfig = new CustomConfig(language, "");
        final var appConfig = mock(AppConfig.class);
        when(appConfig.custom()).thenReturn(customConfig);
        return appConfig;
    }

    @Test
    void getProvidedLocales() {
        final var i18nProvider = new ApplicationI18NProvider(createLanguageConfig(""));
        assertEquals(2, i18nProvider.getProvidedLocales().size());
        assertIterableEquals(List.of(Locale.ENGLISH, Locale.GERMAN), i18nProvider.getProvidedLocales());
    }

    @Test
    void englishTranslationWithoutParameters() {
        final var i18nProvider = new ApplicationI18NProvider(createLanguageConfig("en"));
        assertEquals("Rooms & Sessions", i18nProvider.getTranslation("event.heading", null));
    }

    @Test
    void englishTranslationWithNullParameters() {
        final var i18nProvider = new ApplicationI18NProvider(createLanguageConfig("en"));
        assertEquals("Rooms & Sessions", i18nProvider.getTranslation("event.heading", null, (Object) null));
    }

    @Test
    void englishTranslationWithStringParameter() {
        final var i18nProvider = new ApplicationI18NProvider(createLanguageConfig("en"));
        assertEquals("ends in 42 minutes", i18nProvider.getTranslation("event.session.countdown.minutes", null, "42"));
    }

    @Test
    void englishTranslationWithIntParameter() {
        final var i18nProvider = new ApplicationI18NProvider(createLanguageConfig("en"));
        assertEquals("ends in 42 minutes", i18nProvider.getTranslation("event.session.countdown.minutes", null, 42));
    }

    @Test
    void germanTranslationWithoutParameters() {
        final var i18nProvider = new ApplicationI18NProvider(createLanguageConfig("de"));
        assertEquals("Räume & Vorträge", i18nProvider.getTranslation("event.heading", null));
    }

    @Test
    void germanTranslationWithNullParameters() {
        final var i18nProvider = new ApplicationI18NProvider(createLanguageConfig("de"));
        assertEquals("Räume & Vorträge", i18nProvider.getTranslation("event.heading", null, (Object) null));
    }

    @Test
    void germanTranslationWithStringParameter() {
        final var i18nProvider = new ApplicationI18NProvider(createLanguageConfig("de"));
        assertEquals("endet in 42 Minuten", i18nProvider.getTranslation("event.session.countdown.minutes", null, "42"));
    }

    @Test
    void germanTranslationWithIntParameter() {
        final var i18nProvider = new ApplicationI18NProvider(createLanguageConfig("de"));
        assertEquals("endet in 42 Minuten", i18nProvider.getTranslation("event.session.countdown.minutes", null, 42));
    }

}
