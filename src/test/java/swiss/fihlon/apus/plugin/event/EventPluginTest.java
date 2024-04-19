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
package swiss.fihlon.apus.plugin.event;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.Speaker;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventPluginTest {

    private static final String TEST_ID = "TEST";
    private static final LocalDateTime TEST_START_DATE = LocalDateTime.of(2024, 1, 1, 18, 0);
    private static final LocalDateTime TEST_END_DATE = LocalDateTime.of(2024, 1, 1, 19, 0);
    private static final Room TEST_ROOM = new Room("Test Room");
    private static final String TEST_TITLE = "Test Session";
    private static final List<Speaker> TEST_SPEAKERS = List.of(new Speaker("Speaker 1"), new Speaker("Speaker 2"));
    private static final Language TEST_LANGUAGE = Language.EN;

    @Test
    void isEnabled() {
        final EventPlugin eventPlugin = new TestEventPlugin();
        assertTrue(eventPlugin.isEnabled());
    }

    @Test
    void getSessions() {
        final EventPlugin eventPlugin = new TestEventPlugin();
        final List<Session> sessions = eventPlugin.getSessions();
        assertEquals(1, sessions.size());
        assertEquals(new Session(TEST_ID, TEST_START_DATE, TEST_END_DATE, TEST_ROOM, TEST_TITLE, TEST_SPEAKERS, TEST_LANGUAGE), sessions.getFirst());
    }

    static final class TestEventPlugin implements EventPlugin {

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public @NotNull List<Session> getSessions() {
            return List.of(new Session(TEST_ID, TEST_START_DATE, TEST_END_DATE, TEST_ROOM, TEST_TITLE, TEST_SPEAKERS, TEST_LANGUAGE));
        }

    }
}
