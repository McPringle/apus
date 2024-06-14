package swiss.fihlon.apus.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackTest {

    @Test
    void values() {
        assertEquals(14, Track.values().length);
    }

    @Test
    void getTrackIcon() {
        for (final Track track : Track.values()) {
            if (track.equals(Track.NONE)) {
                assertTrue(track.getTrackName().isBlank());
                assertTrue(track.getFileName().isBlank());
            } else {
                assertFalse(track.getTrackName().isBlank());
                assertFalse(track.getFileName().isBlank());
            }
        }
    }

}
