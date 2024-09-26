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

import com.vaadin.flow.component.avatar.Avatar;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SpeakerTest {

    @Test
    void fullName() {
        assertEquals("Speaker 1", new Speaker("Speaker 1").fullName());
        assertEquals("Speaker 2", new Speaker("Speaker 2", null).fullName());
    }

    @Test
    void imageUrl() {
        assertNull(new Speaker("").imageUrl());
        assertNull(new Speaker("", null).imageUrl());
        assertEquals("test.png", new Speaker("", "test.png").imageUrl());
        assertEquals("https://foo.bar/test.png", new Speaker("", "https://foo.bar/test.png").imageUrl());
    }

    @Test
    void avatar() {
        final Avatar avatar = new Speaker("Speaker 1", "https://foo.bar/test.png").avatar();
        assertEquals("Speaker 1", avatar.getName());
        assertEquals("https://foo.bar/test.png", avatar.getImage());
    }
}
