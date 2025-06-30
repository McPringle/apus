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
package swiss.fihlon.apus.plugin.event.jfs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TalkTest {

    private final Talk talk = new Talk("id", "title", "room", "topic", "timeSlot", true);

    @Test
    void id() {
        assertEquals("id", talk.id());
    }

    @Test
    void title() {
        assertEquals("title", talk.title());
    }

    @Test
    void room() {
        assertEquals("room", talk.room());
    }

    @Test
    void topic() {
        assertEquals("topic", talk.topic());
    }

    @Test
    void timeSlot() {
        assertEquals("timeSlot", talk.timeSlot());
    }

    @Test
    void isInEnglish() {
        assertTrue(talk.isInEnglish());
    }

}
