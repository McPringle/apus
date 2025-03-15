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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionTest {

    private static final ZoneId TEST_TIMEZONE = ZoneId.of("Europe/Zurich");

    @Test
    void compareTo() {
        final var now = ZonedDateTime.now(TEST_TIMEZONE);
        final var roomA = new Room("Room A");
        final var roomB = new Room("Room B");

        final var sessionOne = new Session("S1", now, now.plusHours(1), roomA, "Session One",
                List.of(new Speaker("Speaker 1")), Language.EN, Track.NONE);
        final var sessionTwo = new Session("S2", now, now.plusHours(1), roomB, "Session Two",
                List.of(new Speaker("Speaker 1")), Language.EN, Track.NONE);
        final var sessionThree = new Session("S3", now.plusHours(1), now.plusHours(2), roomA, "Session Three",
                List.of(new Speaker("Speaker 1")), Language.EN, Track.NONE);
        final var sessionFour = new Session("S4", now.plusHours(1), now.plusHours(2), roomB, "Session Four",
                List.of(new Speaker("Speaker 1")), Language.EN, Track.NONE);
        final var sessionFive = new Session("S5", now.plusHours(2), now.plusHours(3), roomA, "Session Five",
                List.of(new Speaker("Speaker 1")), Language.EN, Track.NONE);
        final var sessionSix = new Session("S6", now.plusHours(2), now.plusHours(3), roomB, "Session Six",
                List.of(new Speaker("Speaker 1")), Language.EN, Track.NONE);

        final var unsortedSessions = new ArrayList<>(List.of(sessionTwo, sessionSix, sessionThree, sessionFour, sessionFive, sessionOne));
        Collections.shuffle(unsortedSessions);
        final var sortedSessions = unsortedSessions.stream().sorted().toList();

        assertEquals(sessionOne, sortedSessions.get(0));
        assertEquals(sessionTwo, sortedSessions.get(1));
        assertEquals(sessionThree, sortedSessions.get(2));
        assertEquals(sessionFour, sortedSessions.get(3));
        assertEquals(sessionFive, sortedSessions.get(4));
        assertEquals(sessionSix, sortedSessions.get(5));
    }

    @Test
    void create() {
        final var id = "S1";
        final var startDate = ZonedDateTime.now(TEST_TIMEZONE);
        final var endDate = startDate.plusHours(1);
        final var room = new Room("Room A");
        final var title = "Test Session A";
        final var speakers = List.of(new Speaker("Speaker 1"), new Speaker("Speaker 2"));
        final var language = Language.EN;
        final var trackInfo = Track.NONE;

        final Session session = new Session(id, startDate, endDate, room, title, speakers, language, trackInfo);
        assertEquals(id, session.id());
        assertEquals(startDate, session.startDate());
        assertEquals(endDate, session.endDate());
        assertEquals(room, session.room());
        assertEquals(title, session.title());
        assertEquals(speakers, session.speakers());
        assertEquals(2, session.speakers().size());
        assertEquals(language, session.language());
        assertEquals(trackInfo, session.track());
    }
}
