package swiss.fihlon.apus.event;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackTest {

    @Test
    void noneTrack() {
        final Track testee = new Track(null, null);
        assertEquals(Track.NONE, testee);
        assertNull(testee.name());
        assertNull(testee.svgCode());
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForDefaultTrack")
    void defaultTracks(@NotNull final Track testee, @NotNull final String expectedName) {
        final String message = String.format("Error while testing track '%s'!", expectedName);
        assertNotNull(testee, message);
        assertNotNull(testee.name(), message);
        assertNotNull(testee.svgCode(), message);
        assertEquals(expectedName, testee.name(), message);
        assertTrue(testee.svgCode().startsWith("<?xml"), message);
        assertTrue(testee.svgCode().contains("<svg"), message);
        assertTrue(testee.svgCode().endsWith("</svg>"), message);
    }

    private static Stream<Arguments> provideArgumentsForDefaultTrack() {
        return Stream.of(
                Arguments.of(Track.ARCHITECTURE, "Architecture"),
                Arguments.of(Track.CLOUD, "Cloud"),
                Arguments.of(Track.CORE, "Core"),
                Arguments.of(Track.INFRASTRUCTURE, "Infrastructure"),
                Arguments.of(Track.SECURITY, "Security"),
                Arguments.of(Track.TOOLS, "Tools")
        );
    }

    @Test
    void customTrack() {
        final Track testee = new Track("testName", "testSvgCode");
        assertEquals("testName", testee.name());
        assertEquals("testSvgCode", testee.svgCode());
    }

}
