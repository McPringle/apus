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

import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.server.InvalidI18NConfigurationException;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import swiss.fihlon.apus.configuration.AppConfig;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@SpringComponent
public final class ApplicationI18NProvider implements I18NProvider {

    private static final @NotNull List<@NotNull Locale> SUPPORTED_LOCALES = List.of(Locale.ENGLISH, Locale.GERMAN);

    private final @NotNull Locale locale;
    private final transient @NotNull ResourceBundle resourceBundle;

    public ApplicationI18NProvider(final @NotNull AppConfig appConfig) {
        locale = appConfig.locale();
        if (SUPPORTED_LOCALES.contains(locale)) {
            resourceBundle = ResourceBundle.getBundle("i18n/translations", locale);
        } else {
            final var language = appConfig.language();
            throw new InvalidI18NConfigurationException("Unsupported locale '%s' for language '%s'!".formatted(locale, language));
        }
    }

    public @NotNull Locale getLocale() {
        return locale;
    }

    @Override
    public @NotNull List<@NotNull Locale> getProvidedLocales() {
        return SUPPORTED_LOCALES;
    }

    @Override
    public @NotNull String getTranslation(final @NotNull String key, final @Nullable Locale ignoreLocale, final @NotNull Object... params) {
        final String message =  resourceBundle.getString(key);
        return params.length > 0 ? MessageFormat.format(message, params) : message;
    }
}
