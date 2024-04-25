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
    DE("de", """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480">
              <path fill="#fc0" d="M0 320h640v160H0z"/>
              <path fill="#000001" d="M0 0h640v160H0z"/>
              <path fill="red" d="M0 160h640v160H0z"/>
            </svg>"""),
    EN("en", """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480">
              <path fill="#012169" d="M0 0h640v480H0z"/>
              <path fill="#FFF" d="m75 0 244 181L562 0h78v62L400 241l240 178v61h-80L320 301 81 480H0v-60l239-178L0 64V0z"/>
              <path fill="#C8102E" d="m424 281 216 159v40L369 281zm-184 20 6 35L54 480H0zM640 0v3L391 191l2-44L590 0zM0 0l239 176h-60L0 42z"/>
              <path fill="#FFF" d="M241 0v480h160V0zM0 160v160h640V160z"/>
              <path fill="#C8102E" d="M0 193v96h640v-96zM273 0v480h96V0z"/>
            </svg>""");

    private final String languageCode;
    private final String svgCode;

    public static Language languageWithCode(@NotNull final String languageCode) {
        for (final Language language : values()) {
            if (language.languageCode.equals(languageCode)) {
                return language;
            }
        }
        throw new IllegalArgumentException(String.format("No language constant with language code '%s'!", languageCode));
    }

    Language(@NotNull final String languageCode, @NotNull final String svgCode) {
        this.languageCode = languageCode;
        this.svgCode = svgCode;
    }

    @NotNull
    public String getLanguageCode() {
        return languageCode;
    }

    @NotNull
    public String getSvgCode() {
        return svgCode;
    }
}
