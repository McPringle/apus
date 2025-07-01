package swiss.fihlon.apus.plugin.event.jfs;

import org.junit.jupiter.api.Test;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.SessionImportException;
import swiss.fihlon.apus.event.Speaker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JavaForumStuttgartPluginTest {

    private static final ZoneId TEST_TIMEZONE = ZoneId.of("Europe/Berlin");

    @Test
    void isEnabled() {
        final var appConfig = mock(AppConfig.class);
        final var jfsConfig = new JavaForumStuttgartConfig("test");
        when(appConfig.jfs()).thenReturn(jfsConfig);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        assertTrue(jfsPlugin.isEnabled());
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
        final var jfsConfig = new JavaForumStuttgartConfig("file:src/test/resources/testdata/jfs-talks.json");
        when(appConfig.jfs()).thenReturn(jfsConfig);
        when(appConfig.timezone()).thenReturn(TEST_TIMEZONE);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        final var sessions = jfsPlugin.getSessions().toList();
        assertEquals(8, sessions.size());

        // full check of session with ID "JFS:5"
        final var session = sessions.get(4);
        final var today = LocalDate.now(TEST_TIMEZONE);
        assertEquals("JFS:5", session.id());
        assertEquals(ZonedDateTime.of(today, LocalTime.of(19, 00), TEST_TIMEZONE), session.startDate());
        assertEquals(ZonedDateTime.of(today, LocalTime.of(19, 45), TEST_TIMEZONE), session.endDate());
        assertEquals(new Room("Room A"), session.room());
        assertEquals("Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat", session.title());
        assertEquals(2, session.speakers().size());
        assertEquals(new Speaker("Saul Goodman", null), session.speakers().getFirst());
        assertEquals(new Speaker("Mike Ehrmantraut", null), session.speakers().get(1));
        assertEquals(Language.DE, session.language());
    }

    @Test
    void throwsExceptionWithNonExistingJson() {
        final var appConfig = mock(AppConfig.class);
        final var jfsConfig = new JavaForumStuttgartConfig("file:src/test/resources/testdata/non-existing.json");
        when(appConfig.jfs()).thenReturn(jfsConfig);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        final var exception = assertThrows(SessionImportException.class, jfsPlugin::getSessions);
        final var message = exception.getMessage();
        assertNotNull(message);
        assertTrue(message.startsWith("Error downloading JSON file from 'file:src/test/resources/testdata/non-existing.json': "));
    }


    @Test
    void throwsExceptionWithEmptyDatabase() {
        final var appConfig = mock(AppConfig.class);
        final var jfsConfig = new JavaForumStuttgartConfig("file:src/test/resources/testdata/jfs-empty.json");
        when(appConfig.jfs()).thenReturn(jfsConfig);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        final var exception = assertThrows(SessionImportException.class, jfsPlugin::getSessions);
        final var message = exception.getMessage();
        assertNotNull(message);
        assertTrue(message.startsWith("Error importing session data for Java Forum Stuttgart: "));
    }

}
