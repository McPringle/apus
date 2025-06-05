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

    private static final ZoneId TEST_TIMEZONE = ZoneId.of("Europe/Zurich");

    @Test
    void isEnabled() {
        final var appConfig = mock(AppConfig.class);
        final var jfsConfig = new JavaForumStuttgartConfig("test", "");
        when(appConfig.jfs()).thenReturn(jfsConfig);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        assertTrue(jfsPlugin.isEnabled());
    }

    @Test
    void isDisabledBecauseEmpty() {
        final var appConfig = mock(AppConfig.class);
        final var jfsConfig = new JavaForumStuttgartConfig("", "");
        when(appConfig.jfs()).thenReturn(jfsConfig);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        assertFalse(jfsPlugin.isEnabled());
    }

    @Test
    void getSessions() {
        final var appConfig = mock(AppConfig.class);
        final var jfsConfig = new JavaForumStuttgartConfig(
                "file:src/test/resources/testdata/jfs-talks.json",
        "https://jfspwa.java-forum-stuttgart.de/jfsdata/jfs_resources.zip"

        );
        when(appConfig.jfs()).thenReturn(jfsConfig);
        when(appConfig.timezone()).thenReturn(TEST_TIMEZONE);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        final var sessions = jfsPlugin.getSessions().toList();
        assertEquals(49, sessions.size());

        // full check of session with ID "JFS:5"
        final var session = sessions.get(5);
        final var today = LocalDate.now(TEST_TIMEZONE);
        assertEquals("JFS:180560128", session.id());
        assertEquals(ZonedDateTime.of(today, LocalTime.of(15, 35), TEST_TIMEZONE), session.startDate());
        assertEquals(ZonedDateTime.of(today, LocalTime.of(16, 20), TEST_TIMEZONE), session.endDate());
        assertEquals(new Room("Track A"), session.room());
        assertEquals("Decoupled by Default", session.title());
        assertEquals(1, session.speakers().size());
        assertEquals(new Speaker("Markus Schlegel", "images/2025__1805601280__Schlegel__Markus.jpeg"), session.speakers().get(0));
        assertEquals(Language.UNKNOWN, session.language());
    }

    @Test
    void throwsExceptionWithNonExistingJson() {
        final var appConfig = mock(AppConfig.class);
        final var jfsConfig = new JavaForumStuttgartConfig("file:src/test/resources/testdata/non-existing.json", "");
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
        final var jfsConfig = new JavaForumStuttgartConfig("file:src/test/resources/testdata/jfs-empty.json", "");
        when(appConfig.jfs()).thenReturn(jfsConfig);

        final var jfsPlugin = new JavaForumStuttgartPlugin(appConfig);
        final var exception = assertThrows(SessionImportException.class, jfsPlugin::getSessions);
        final var message = exception.getMessage();
        assertNotNull(message);
        assertTrue(message.startsWith("Error importing session data for Java Forum Stuttgart: "));
    }

}
