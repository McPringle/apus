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
package swiss.fihlon.apus.agenda;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LanguageTest {

    @Test
    void languageWithCode() {
        assertEquals(Language.DE, Language.languageWithCode("de"));
        assertEquals(Language.EN, Language.languageWithCode("en"));
    }

    @Test
    void getLanguageCode() {
        assertEquals("de", Language.DE.getLanguageCode());
        assertEquals("en", Language.EN.getLanguageCode());
    }

    @Test
    void getFlagEmoji() {
        for (final Language language : Language.values()) {
            assertNotNull(language.getFlagEmoji());
            assertFalse(language.getFlagEmoji().trim().isEmpty());
        }
    }

    @Test
    void values() {
        assertEquals(2, Language.values().length);
    }

    @Test
    void valueOf() {
        assertEquals(Language.DE, Language.valueOf("DE"));
        assertEquals(Language.EN, Language.valueOf("EN"));
    }
}
