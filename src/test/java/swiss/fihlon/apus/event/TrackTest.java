package swiss.fihlon.apus.event;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void noneTrack() {
        final Track testee = new Track(null);
        assertEquals(Track.NONE, testee);
        assertNull(testee.svgCode());
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForDefaultTrack")
    void defaultTracks(@NotNull final Track testee) {
        final String message = String.format("Error while testing track '%s'!", testee);
        assertNotNull(testee, message);
        assertNotNull(testee.svgCode(), message);
        assertTrue(testee.svgCode().startsWith("<?xml"), message);
        assertTrue(testee.svgCode().contains("<svg"), message);
        assertTrue(testee.svgCode().endsWith("</svg>"), message);
    }

    private static Stream<Arguments> provideArgumentsForDefaultTrack() {
        return Stream.of(
                Arguments.of(Track.ARCHITECTURE),
                Arguments.of(Track.CLOUD),
                Arguments.of(Track.CORE),
                Arguments.of(Track.INFRASTRUCTURE),
                Arguments.of(Track.SECURITY),
                Arguments.of(Track.TOOLS)
        );
    }

    @Test
    void customTrack() {
        final Track testee = new Track("testSvgCode");
        assertEquals("testSvgCode", testee.svgCode());
    }

    @Test
    void customTrackLogException() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method defaultTrackMethod = Track.class.getDeclaredMethod("defaultTrack", String.class);
        defaultTrackMethod.setAccessible(true);
        final var track = defaultTrackMethod.invoke(String.class, "non-existing-file.svg");
        assertEquals(Track.NONE, track);

        final String out = outputStreamCaptor.toString();
        assertTrue(out.contains("Unable to load default track icon 'non-existing-file.svg':"));
    }

}
