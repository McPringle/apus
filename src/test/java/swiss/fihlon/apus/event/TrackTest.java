package swiss.fihlon.apus.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackTest {

    @Test
    void values() {
        assertEquals(5, Track.values().length);
    }

    @Test
    void getTrackIcon() {
        for (final Track track : Track.values()) {
            assertTrue(track.getSvgCode().trim().startsWith("<svg "));
            assertTrue(track.getSvgCode().trim().endsWith("</svg>"));
        }
    }

}
