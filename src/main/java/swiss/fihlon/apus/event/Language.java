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
    DE("de"),
    EN("en");

    private static final String FILE_NAME_TEMPLATE = "icons/flags/%s.svg";

    private final String languageCode;
    private final String flagFileName;

    public static Language languageWithCode(@NotNull final String languageCode) {
        for (final Language language : values()) {
            if (language.languageCode.equals(languageCode)) {
                return language;
            }
        }
        throw new IllegalArgumentException(String.format("No language constant with language code '%s'!", languageCode));
    }

    Language(@NotNull final String languageCode) {
        this.languageCode = languageCode;
        this.flagFileName = FILE_NAME_TEMPLATE.formatted(languageCode);
    }

    @NotNull
    public String getLanguageCode() {
        return languageCode;
    }

    @NotNull
    public String getFlagFileName() {
        return flagFileName;
    }
}
