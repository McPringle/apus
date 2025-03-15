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
package swiss.fihlon.apus.ui.view;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.Speaker;
import swiss.fihlon.apus.event.Track;
import swiss.fihlon.apus.util.TestUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static swiss.fihlon.apus.util.TestUtil.getComponentsByClassName;

class RoomViewTest {

    private static final ZoneId TEST_TIMEZONE = ZoneId.of("Europe/Zurich");

    private static void assertTitle(@NotNull final RoomView roomView, @NotNull final String title, @NotNull final Language language) {
        final var components = getComponentsByClassName(roomView, "title");
        assertEquals(1, components.size());

        final var titleComponent = components.getFirst();
        final var titleChildren = titleComponent.getChildren().toList();
        assertEquals(language == Language.UNKNOWN ? 1 : 2, titleChildren.size());

        final var text = titleChildren.getFirst();
        assertEquals(title, text.getElement().getText());

        if (language != Language.UNKNOWN) {
            final var image = titleChildren.getLast();
            assertEquals(language.getFlagFileName(), image.getElement().getAttribute("src"));
        }
    }

    private static void assertSpeakers(@NotNull final RoomView roomView, @NotNull final String speakers) {
        final var components = getComponentsByClassName(roomView, "speaker");
        assertEquals(1, components.size());

        final var speakersComponent = components.getFirst();
        final var speakersChildren = speakersComponent.getChildren().toList();

        if (speakers.isBlank()) {
            assertEquals(1, speakersChildren.size());
            final var text = speakersChildren.getFirst();
            assertEquals("", text.getElement().getText());
        } else {
            assertEquals(2, speakersChildren.size());

            final var icon = speakersChildren.getFirst();
            assertEquals("vaadin:user", icon.getElement().getAttribute("icon"));

            final var text = speakersChildren.getLast();
            assertEquals(speakers, text.getElement().getText());
        }
    }

    private static void assertRoom(@NotNull final RoomView roomView, @NotNull final String roomName) {
        final var components = getComponentsByClassName(roomView, "room");
        assertEquals(1, components.size());

        final var roomComponent = components.getFirst();
        final var roomChildren = roomComponent.getChildren().toList();
        assertEquals(2, roomChildren.size());

        final var icon = roomChildren.getFirst();
        assertEquals("vaadin:location-arrow-circle", icon.getElement().getAttribute("icon"));

        final var text = roomChildren.getLast();
        assertEquals(roomName, text.getElement().getText());
    }

    private static void assertTime(@NotNull final RoomView roomView, @NotNull final String time) {
        final var components = getComponentsByClassName(roomView, "time");
        assertEquals(1, components.size());

        final var timeComponent = components.getFirst();
        final var timeChildren = timeComponent.getChildren().toList();

        if (time.isBlank()) {
            assertEquals(1, timeChildren.size());
            final var text = timeChildren.getFirst();
            assertEquals("", text.getElement().getText().trim());
        } else {
            assertEquals(2, timeChildren.size());

            final var icon = timeChildren.getFirst();
            assertEquals("vaadin:hourglass", icon.getElement().getAttribute("icon"));

            final var text = timeChildren.getLast();
            assertEquals(time, text.getElement().getText());
        }
    }

    private static void assertTrack(@NotNull final RoomView roomView, @Nullable final Track track) {
        final var components = getComponentsByClassName(roomView, "track");
        assertEquals(track == null ? 0 : 1, components.size());

        if (track != null) {
            final var trackComponent = components.getFirst();
            final var trackChildren = trackComponent.getChildren().toList();

            if (track.equals(Track.NONE)) {
                assertEquals(0, trackChildren.size());
            } else {
                assertEquals(1, trackChildren.size());
                final var component = trackChildren.getFirst();
                final var html = component.getElement().getOuterHTML();
                final var svgCode = TestUtil.extractFirstHtmlTag(html, "svg");
                final var expectedSvgCode = TestUtil.extractFirstHtmlTag(track.svgCode(), "svg");
                assertEquals(expectedSvgCode, svgCode);
            }
        }
    }

    private static void assertAvatar(@NotNull final RoomView roomView, @NotNull final List<String> avatarLinks) {
        final var components = getComponentsByClassName(roomView, "avatar");
        assertEquals(1, components.size());

        final var component = components.getFirst();
        final var groups = getComponentsByClassName(component, "avatar-group");
        assertEquals(1, groups.size());

        final var group = groups.getFirst();
        final var avatars = group.getChildren().toList();
        assertEquals(avatarLinks.size(), avatars.size());

        for (int i = 0; i < avatarLinks.size(); i++) {
            final var avatarLink = avatarLinks.get(i);
            final var avatar = avatars.get(i);
            final var element = avatar.getElement();
            assertEquals(avatarLink, element.getAttribute("img"));
        }
    }

    private static Stream<Arguments> provideArgumentsForRoomTest() {
        return Stream.of(
                Arguments.of("Room One"),
                Arguments.of("Room Two"),
                Arguments.of("Room Three")
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForRoomTest")
    void constructorWithRoom(@NotNull final String roomName) {
        final var room = new Room(roomName);
        final var roomView = new RoomView(TEST_TIMEZONE, room);
        assertEquals(5, roomView.getChildren().count());
        assertTitle(roomView, "!{event.room.empty}!", Language.UNKNOWN);
        assertSpeakers(roomView, "");
        assertRoom(roomView, roomName);
        assertTime(roomView, "");
        assertTrack(roomView, Track.NONE);
    }

    @Test
    void constructorWithSession() {
        final var today = LocalDate.now(TEST_TIMEZONE);
        final var session = new Session("42",
                LocalDateTime.of(today, LocalTime.MIDNIGHT).atZone(TEST_TIMEZONE),
                LocalDateTime.of(today, LocalTime.MAX).atZone(TEST_TIMEZONE),
                new Room("My Room"), "My Session",
                List.of(new Speaker("Speaker One"), new Speaker("Speaker Two")),
                Language.EN, Track.CORE);
        final var roomView = new RoomView(TEST_TIMEZONE, session);
        assertEquals(5, roomView.getChildren().count());
        assertTitle(roomView, "My Session", Language.EN);
        assertSpeakers(roomView, "Speaker One, Speaker Two");
        assertRoom(roomView, "My Room");
        assertTime(roomView, "!{event.session.countdown.minutes}!");
        assertTrack(roomView, Track.CORE);
    }

    @Test
    void sessionWithUnknownLanguage() {
        final var today = LocalDate.now(TEST_TIMEZONE);
        final var session = new Session("42",
                LocalDateTime.of(today, LocalTime.MIDNIGHT).atZone(TEST_TIMEZONE),
                LocalDateTime.of(today, LocalTime.MAX).atZone(TEST_TIMEZONE),
                new Room("My Room"), "My Session",
                List.of(new Speaker("Speaker One"), new Speaker("Speaker Two")),
                Language.UNKNOWN, Track.CORE);
        final var roomView = new RoomView(TEST_TIMEZONE, session);
        assertEquals(5, roomView.getChildren().count());
        assertTitle(roomView, "My Session", Language.UNKNOWN);
        assertSpeakers(roomView, "Speaker One, Speaker Two");
        assertRoom(roomView, "My Room");
        assertTime(roomView, "!{event.session.countdown.minutes}!");
        assertTrack(roomView, Track.CORE);
    }

    @Test
    void sessionWithNoTrack() {
        final var today = LocalDate.now(TEST_TIMEZONE);
        final var session = new Session("42",
                LocalDateTime.of(today, LocalTime.MIDNIGHT).atZone(TEST_TIMEZONE),
                LocalDateTime.of(today, LocalTime.MAX).atZone(TEST_TIMEZONE),
                new Room("My Room"), "My Session",
                List.of(new Speaker("Speaker One"), new Speaker("Speaker Two")),
                Language.EN, Track.NONE);
        final var roomView = new RoomView(TEST_TIMEZONE, session);
        assertEquals(5, roomView.getChildren().count());
        assertTitle(roomView, "My Session", Language.EN);
        assertSpeakers(roomView, "Speaker One, Speaker Two");
        assertRoom(roomView, "My Room");
        assertTime(roomView, "!{event.session.countdown.minutes}!");
        assertTrack(roomView, Track.NONE);
    }

    @Test
    void sessionWithSpeakerAvatarAndTrack() {
        final var today = LocalDate.now(TEST_TIMEZONE);
        final var session = new Session("42",
                LocalDateTime.of(today, LocalTime.MIDNIGHT).atZone(TEST_TIMEZONE),
                LocalDateTime.of(today, LocalTime.MAX).atZone(TEST_TIMEZONE),
                new Room("My Room"), "My Session",
                List.of(
                        new Speaker("Speaker One", "https://localhost/avatar1.png"),
                        new Speaker("Speaker Two", "https://localhost/avatar2.svg")),
                Language.EN, Track.CORE);
        final var roomView = new RoomView(TEST_TIMEZONE, session);
        assertEquals(5, roomView.getChildren().count());
        assertTitle(roomView, "My Session", Language.EN);
        assertSpeakers(roomView, "Speaker One, Speaker Two");
        assertRoom(roomView, "My Room");
        assertTime(roomView, "!{event.session.countdown.minutes}!");
        assertTrack(roomView, null);
        assertAvatar(roomView, List.of("https://localhost/avatar1.png", "https://localhost/avatar2.svg"));
    }

    @Test
    void sessionWithSpeakerAvatarWithoutTrack() {
        final var today = LocalDate.now(TEST_TIMEZONE);
        final var session = new Session("42",
                LocalDateTime.of(today, LocalTime.MIDNIGHT).atZone(TEST_TIMEZONE),
                LocalDateTime.of(today, LocalTime.MAX).atZone(TEST_TIMEZONE),
                new Room("My Room"), "My Session",
                List.of(
                        new Speaker("Speaker One", "https://localhost/avatar1.png"),
                        new Speaker("Speaker Two", "https://localhost/avatar2.svg")),
                Language.EN, Track.NONE);
        final var roomView = new RoomView(TEST_TIMEZONE, session);
        assertEquals(5, roomView.getChildren().count());
        assertTitle(roomView, "My Session", Language.EN);
        assertSpeakers(roomView, "Speaker One, Speaker Two");
        assertRoom(roomView, "My Room");
        assertTime(roomView, "!{event.session.countdown.minutes}!");
        assertTrack(roomView, null);
        assertAvatar(roomView, List.of("https://localhost/avatar1.png", "https://localhost/avatar2.svg"));
    }

}
