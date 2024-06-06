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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Svg;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.RoomStyle;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.Speaker;
import swiss.fihlon.apus.event.Track;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@CssImport(value = "./themes/apus/views/room-view.css")
public final class RoomView extends Div {

    private final transient Room room;
    private final String title;
    private final transient List<Speaker> speakers;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Language language;
    private final Track track;

    private RoomStyle roomStyle = RoomStyle.NONE;

    public RoomView(@NotNull final Room room) {
        this(room, null, List.of(), null, null, null, null);
    }

    public RoomView(@NotNull final Session session) {
        this(
                session.room(),
                session.title(),
                session.speakers(),
                session.startDate().toLocalTime(),
                session.endDate().toLocalTime(),
                session.language(),
                session.track()
        );
    }

    public RoomView(@NotNull final Room room,
                    @Nullable final String title,
                    @NotNull final List<Speaker> speakers,
                    @Nullable final LocalTime startTime,
                    @Nullable final LocalTime endTime,
                    @Nullable final Language language,
                    @Nullable final Track track) {
        this.room = room;
        this.title = title;
        this.speakers = speakers;
        this.startTime = startTime;
        this.endTime = endTime;
        this.language = language;
        this.track = track;

        addClassName("room-view");
        add(createTitleComponent());
        add(createSpeakersComponent());
        add(createRoomComponent());
        add(createTimeComponent());
        addClassName(roomStyle.getCssStyle());
    }

    @NotNull
    private Component createTitleComponent() {
        final var titleComponent = new Div();
        titleComponent.addClassName("title");
        titleComponent.add(new H3(new Text(title == null ? getTranslation("event.room.empty") : title)));
        if (language != null) {
            final var flagComponent = new Svg(language.getSvgCode());
            flagComponent.addClassName("language");
            titleComponent.add(flagComponent);
        }
        if (track != null) {
            final var trackComponent = new Svg(track.getSvgCode());
            trackComponent.addClassName("track");
            titleComponent.add(trackComponent);
        }
        return titleComponent;
    }

    @NotNull
    private Component createSpeakersComponent() {
        final var speakersComponent = new Div();
        if (speakers.isEmpty()) {
            speakersComponent.add(nbsp());
        } else {
            final var joinedSpeakers = speakers.stream()
                    .map(Speaker::fullName)
                    .collect(Collectors.joining(", "));
            speakersComponent.add(new Text(String.format("\uD83D\uDC64 %s", joinedSpeakers)));
        }
        return speakersComponent;
    }

    @NotNull
    private Component createRoomComponent() {
        return new Div(new Text(String.format("\uD83D\uDCCD %s", room.name())));
    }

    @NotNull
    private Component createTimeComponent() {
        final var timeComponent = new Div();
        final var now = LocalTime.now();
        if (startTime == null || endTime == null) { // empty session
            timeComponent.add(nbsp());
            roomStyle = RoomStyle.EMPTY;
        } else if (startTime.isBefore(now) && endTime.isAfter(now)) { // running session
            final Duration duration = Duration.between(now, endTime);
            final long timeLeft = Math.round(duration.getSeconds() / 60f);
            timeComponent.add(new Text("⌛ " + getTranslation(timeLeft == 1
                            ? "event.session.countdown.singular" : "event.session.countdown.plural",
                            timeLeft)));
            roomStyle = RoomStyle.RUNNING;
        } else { // next session
            timeComponent.add(new Text(String.format("⌚ %s - %s", startTime, endTime)));
            roomStyle = RoomStyle.NEXT;
        }
        return timeComponent;
    }

    @NotNull
    private static Component nbsp() {
        return new Html("<span>&nbsp;</span>");
    }

    @NotNull
    public RoomStyle getRoomStyle() {
        return roomStyle;
    }
}
