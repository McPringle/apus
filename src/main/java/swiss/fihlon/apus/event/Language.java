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
package swiss.fihlon.apus.event;

import org.jetbrains.annotations.NotNull;

public enum Language {

    // Important: When adding a new language, modify the test accordingly!
    DE("de", "\uD83C\uDDE9\uD83C\uDDEA"),
    EN("en", "\uD83C\uDDEC\uD83C\uDDE7");

    private final String languageCode;
    private final String flagEmoji;

    public static Language languageWithCode(@NotNull final String languageCode) {
        for (final Language language : values()) {
            if (language.languageCode.equals(languageCode)) {
                return language;
            }
        }
        throw new IllegalArgumentException(String.format("No language constant with language code '%s'!", languageCode));
    }

    Language(@NotNull final String languageCode, @NotNull final String flagEmoji) {
        this.languageCode = languageCode;
        this.flagEmoji = flagEmoji;
    }

    @NotNull
    public String getLanguageCode() {
        return languageCode;
    }

    @NotNull
    public String getFlagEmoji() {
        return flagEmoji;
    }
}
