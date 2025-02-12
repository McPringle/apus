package swiss.fihlon.apus.plugin.event.jfs;

import org.junit.jupiter.api.Test;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.SessionImportException;
import swiss.fihlon.apus.event.Speaker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JavaForumStuttgartPluginTest {

    @Test
    void isEnabled() {
        final var appConfig = mock(AppConfig.class);
        final var jfsConfig = new JavaForumStuttgartConfig("test");
        when(appConfig.jfs()).thenReturn(jfsConfig);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        assertTrue(jfsPlugin.isEnabled());
    }

    @Test
    void isDisabledBecauseNull() {
        final var appConfig = mock(AppConfig.class);
        final var jfsConfig = new JavaForumStuttgartConfig(null);
        when(appConfig.jfs()).thenReturn(jfsConfig);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        assertFalse(jfsPlugin.isEnabled());
    }

    @Test
    void isDisabledBecauseEmpty() {
        final var appConfig = mock(AppConfig.class);
        final var jfsConfig = new JavaForumStuttgartConfig("");
        when(appConfig.jfs()).thenReturn(jfsConfig);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        assertFalse(jfsPlugin.isEnabled());
    }

    @Test
    void getSessions() {
        final var appConfig = mock(AppConfig.class);
        final var jfsConfig = new JavaForumStuttgartConfig("file:src/test/resources/testdata/jfs.db");
        when(appConfig.jfs()).thenReturn(jfsConfig);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        final var sessions = jfsPlugin.getSessions().toList();
        assertEquals(8, sessions.size());

        final var sessionIds = sessions.stream().map(Session::id).toList();
        for (String id : List.of("A1", "A2", "A3", "B4", "A5", "B6", "A7", "A8")) {
            final var sessionId = String.format("JFS:%s", id);
            assertTrue(sessionIds.contains(sessionId));
        }

        // full check of session with ID "JFS:5"
        final var session = sessions.get(5);
        final var today = LocalDate.now();
        assertEquals("JFS:A5", session.id());
        assertEquals(LocalDateTime.of(today, LocalTime.of(16, 0)), session.startDate());
        assertEquals(LocalDateTime.of(today, LocalTime.of(16, 45)), session.endDate());
        assertEquals(new Room("Room A"), session.room());
        assertEquals("Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat", session.title());
        assertEquals(2, session.speakers().size());
        assertEquals(new Speaker("Saul Goodman"), session.speakers().get(0));
        assertEquals(new Speaker("Mike Ehrmantraut"), session.speakers().get(1));
        assertEquals(Language.UNKNOWN, session.language());
    }

    @Test
    void throwsExceptionWithNonExistingDatabase() {
        final var appConfig = mock(AppConfig.class);
        final var jfsConfig = new JavaForumStuttgartConfig("file:src/test/resources/testdata/non-existing.db");
        when(appConfig.jfs()).thenReturn(jfsConfig);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        final var exception = assertThrows(SessionImportException.class, jfsPlugin::getSessions);
        assertTrue(exception.getMessage().startsWith("Error downloading database file from 'file:src/test/resources/testdata/non-existing.db': "));
    }


    @Test
    void throwsExceptionWithEmptyDatabase() {
        final var appConfig = mock(AppConfig.class);
        final var jfsConfig = new JavaForumStuttgartConfig("file:src/test/resources/testdata/jfs-empty.db");
        when(appConfig.jfs()).thenReturn(jfsConfig);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        final var exception = assertThrows(SessionImportException.class, jfsPlugin::getSessions);
        assertTrue(exception.getMessage().startsWith("Error importing session data for Java Forum Stuttgart: "));
    }

}
