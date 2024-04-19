package swiss.fihlon.apus.agenda;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @Test
    void compareTo() {
        final var now = LocalDateTime.now();
        final var roomA = new Room("Room A");
        final var roomB = new Room("Room B");

        final var sessionOne = new Session("S1", now, now.plusHours(1), roomA, "Session One",
                List.of(new Speaker("Speaker 1")), Language.EN);
        final var sessionTwo = new Session("S2", now, now.plusHours(1), roomB, "Session Two",
                List.of(new Speaker("Speaker 1")), Language.EN);
        final var sessionThree = new Session("S3", now.plusHours(1), now.plusHours(2), roomA, "Session Three",
                List.of(new Speaker("Speaker 1")), Language.EN);
        final var sessionFour = new Session("S4", now.plusHours(1), now.plusHours(2), roomB, "Session Four",
                List.of(new Speaker("Speaker 1")), Language.EN);
        final var sessionFive = new Session("S5", now.plusHours(2), now.plusHours(3), roomA, "Session Five",
                List.of(new Speaker("Speaker 1")), Language.EN);
        final var sessionSix = new Session("S6", now.plusHours(2), now.plusHours(3), roomB, "Session Six",
                List.of(new Speaker("Speaker 1")), Language.EN);

        final var unsortedSessions = List.of(sessionTwo, sessionSix, sessionThree, sessionFour, sessionFive, sessionOne);
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
        final var startDate = LocalDateTime.now();
        final var endDate = startDate.plusHours(1);
        final var room = new Room("Room A");
        final var title = "Test Session A";
        final var speakers = List.of(new Speaker("Speaker 1"), new Speaker("Speaker 2"));
        final var language = Language.EN;

        final Session session = new Session(id, startDate, endDate, room, title, speakers, language);
        assertEquals(id, session.id());
        assertEquals(startDate, session.startDate());
        assertEquals(endDate, session.endDate());
        assertEquals(room, session.room());
        assertEquals(title, session.title());
        assertEquals(speakers, session.speakers());
        assertEquals(2, session.speakers().size());
        assertEquals(language, session.language());
    }
}
