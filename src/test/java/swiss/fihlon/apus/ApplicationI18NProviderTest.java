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

import com.vaadin.flow.server.InvalidI18NConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import swiss.fihlon.apus.configuration.AppConfig;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationI18NProviderTest {

    private @NotNull AppConfig mockAppConfig(@NotNull final String language) {
        final var appConfig = mock(AppConfig.class);
        when(appConfig.language()).thenReturn(language);
        when(appConfig.locale()).thenReturn(switch (language) {
            case "de" -> Locale.GERMAN;
            case "fr" -> Locale.FRENCH;
            default -> Locale.ENGLISH;
        });
        return appConfig;
    }

    @Test
    void getProvidedLocales() {
        final var i18nProvider = new ApplicationI18NProvider(mockAppConfig(""));
        assertNotNull(i18nProvider);
        assertEquals(2, i18nProvider.getProvidedLocales().size());
        assertIterableEquals(List.of(Locale.ENGLISH, Locale.GERMAN), i18nProvider.getProvidedLocales());
    }

    @Test
    void exceptionWithUnsupportedLanguage() {
        final var appConfig = mockAppConfig("fr");
        assertThrows(InvalidI18NConfigurationException.class, () -> new ApplicationI18NProvider(appConfig));
    }

    @Test
    void englishTranslationWithoutParameters() {
        final var appConfig = mockAppConfig("en");
        final var i18nProvider = new ApplicationI18NProvider(appConfig);
        assertNotNull(i18nProvider);
        assertEquals("Rooms & Sessions", i18nProvider.getTranslation("event.heading", null));
    }

    @Test
    void englishTranslationWithStringParameter() {
        final var appConfig = mockAppConfig("en");
        final var i18nProvider = new ApplicationI18NProvider(appConfig);
        assertNotNull(i18nProvider);
        assertEquals("ends in 42 minutes", i18nProvider.getTranslation("event.session.countdown.minutes", null, "42"));
    }

    @Test
    void englishTranslationWithIntParameter() {
        final var appConfig = mockAppConfig("en");
        final var i18nProvider = new ApplicationI18NProvider(appConfig);
        assertNotNull(i18nProvider);
        assertEquals("ends in 42 minutes", i18nProvider.getTranslation("event.session.countdown.minutes", null, 42));
    }

    @Test
    void germanTranslationWithoutParameters() {
        final var appConfig = mockAppConfig("de");
        final var i18nProvider = new ApplicationI18NProvider(appConfig);
        assertNotNull(i18nProvider);
        assertEquals("Räume & Vorträge", i18nProvider.getTranslation("event.heading", null));
    }

    @Test
    void germanTranslationWithStringParameter() {
        final var appConfig = mockAppConfig("de");
        final var i18nProvider = new ApplicationI18NProvider(appConfig);
        assertNotNull(i18nProvider);
        assertEquals("endet in 42 Minuten", i18nProvider.getTranslation("event.session.countdown.minutes", null, "42"));
    }

    @Test
    void germanTranslationWithIntParameter() {
        final var appConfig = mockAppConfig("de");
        final var i18nProvider = new ApplicationI18NProvider(appConfig);
        assertNotNull(i18nProvider);
        assertEquals("endet in 42 Minuten", i18nProvider.getTranslation("event.session.countdown.minutes", null, 42));
    }

}
