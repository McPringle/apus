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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LanguageTest {

    @Test
    void languageWithCode() {
        assertEquals(Language.DE, Language.languageWithCode("de"));
        assertEquals(Language.EN, Language.languageWithCode("en"));
        assertEquals(Language.UNKNOWN, Language.languageWithCode(""));
        assertThrows(IllegalArgumentException.class, () -> Language.languageWithCode("xx"));
    }

    @Test
    void getLanguageCode() {
        assertEquals("de", Language.DE.getLanguageCode());
        assertEquals("en", Language.EN.getLanguageCode());
        assertEquals("", Language.UNKNOWN.getLanguageCode());
    }

    @Test
    void getFlagIcon() {
        for (final Language language : Language.values()) {
            if (language.equals(Language.UNKNOWN)) {
                continue;
            }
            assertFalse(language.getFlagFileName().isBlank());
        }
    }

    @Test
    void values() {
        assertEquals(3, Language.values().length);
    }

    @Test
    void valueOf() {
        assertEquals(Language.DE, Language.valueOf("DE"));
        assertEquals(Language.EN, Language.valueOf("EN"));
        assertEquals(Language.UNKNOWN, Language.valueOf("UNKNOWN"));
    }
}
