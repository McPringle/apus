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

import static org.junit.jupiter.api.Assertions.*;

class RoomStyleTest {

    @Test
    void getCssStyle() {
        for (final RoomStyle roomStyle : RoomStyle.values()) {
            assertNotNull(roomStyle.getCssStyle());
            assertEquals(roomStyle.equals(RoomStyle.NONE), roomStyle.getCssStyle().isBlank());
        }
    }

    @Test
    void getTranslationKey() {
        for (final RoomStyle roomStyle : RoomStyle.values()) {
            assertNotNull(roomStyle.getTranslationKey());
            assertEquals(roomStyle.equals(RoomStyle.NONE), roomStyle.getTranslationKey().isBlank());
        }
    }

    @Test
    void values() {
        assertEquals(4, RoomStyle.values().length);
    }

    @Test
    void valueOf() {
        assertEquals(RoomStyle.NONE, RoomStyle.valueOf("NONE"));
        assertEquals(RoomStyle.RUNNING, RoomStyle.valueOf("RUNNING"));
        assertEquals(RoomStyle.NEXT, RoomStyle.valueOf("NEXT"));
        assertEquals(RoomStyle.EMPTY, RoomStyle.valueOf("EMPTY"));
    }
}
