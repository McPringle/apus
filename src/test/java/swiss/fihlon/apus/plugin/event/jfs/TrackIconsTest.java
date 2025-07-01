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

class TrackIconsTest {

    @Test
    void getSvgCode() {
        for (final TrackIcons trackIcon : TrackIcons.values()) {
            assertTrue(trackIcon.getSvgCode().startsWith("<?xml version=\"1.0\" encoding=\"utf-8\"?>"));
            assertTrue(trackIcon.getSvgCode().contains("<svg version=\"1.1\""));
            assertTrue(trackIcon.getSvgCode().endsWith("</svg>"));
        }
    }

    @Test
    void values() {
        assertEquals(12, TrackIcons.values().length);
    }

    @Test
    void valueOf() {
        assertEquals(TrackIcons.ARCHITECTURE_SECURITY, TrackIcons.valueOf("ARCHITECTURE_SECURITY"));
        assertEquals(TrackIcons.CLOUD, TrackIcons.valueOf("CLOUD"));
        assertEquals(TrackIcons.CORE_JAVA, TrackIcons.valueOf("CORE_JAVA"));
        assertEquals(TrackIcons.ENTERPRISE_FRAMEWORKS, TrackIcons.valueOf("ENTERPRISE_FRAMEWORKS"));
        assertEquals(TrackIcons.FRONTEND, TrackIcons.valueOf("FRONTEND"));
        assertEquals(TrackIcons.IDE_TOOLS, TrackIcons.valueOf("IDE_TOOLS"));
        assertEquals(TrackIcons.IOT_MOBILE, TrackIcons.valueOf("IOT_MOBILE"));
        assertEquals(TrackIcons.METHODS_PRACTICE, TrackIcons.valueOf("METHODS_PRACTICE"));
        assertEquals(TrackIcons.PECHA_KUCHA, TrackIcons.valueOf("PECHA_KUCHA"));
        assertEquals(TrackIcons.OPENSOURCE, TrackIcons.valueOf("OPENSOURCE"));
        assertEquals(TrackIcons.TEST_BETRIEB, TrackIcons.valueOf("TEST_BETRIEB"));
        assertEquals(TrackIcons.TRENDS, TrackIcons.valueOf("TRENDS"));
    }
}
