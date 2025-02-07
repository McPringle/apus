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
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
    private final transient Track track;

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
        add(createImageComponent());
        addClassName(roomStyle.getCssStyle());
    }

    @NotNull
    private Component createTitleComponent() {
        final var titleComponent = new Div();
        titleComponent.addClassName("title");
        titleComponent.add(new H3(new Text(title == null ? getTranslation("event.room.empty") : title)));
        if (language != null && language != Language.UNKNOWN) {
            final var flagComponent = new Image(language.getFlagFileName(), language.getLanguageCode());
            flagComponent.addClassName("language");
            titleComponent.add(flagComponent);
        }
        return titleComponent;
    }

    @NotNull
    private Component createSpeakersComponent() {
        final var speakersComponent = new Div();
        speakersComponent.addClassName("speakers");
        if (speakers.isEmpty()) {
            speakersComponent.add(nbsp());
        } else {
            final var joinedSpeakers = speakers.stream()
                    .map(Speaker::fullName)
                    .collect(Collectors.joining(", "));
            speakersComponent.add(
                    new Icon(VaadinIcon.USER),
                    new Text(joinedSpeakers)
            );
        }
        return speakersComponent;
    }

    @NotNull
    private Component createRoomComponent() {
        final var roomComponent = new Div(
                new Icon(VaadinIcon.LOCATION_ARROW_CIRCLE),
                new Text(room.name())
        );
        roomComponent.addClassName("room");
        return roomComponent;
    }

    @NotNull
    private Component createTimeComponent() {
        final var timeComponent = new Div();
        timeComponent.addClassName("time");
        final var now = LocalTime.now().withSecond(59).withNano(999);
        if (startTime == null || endTime == null) { // empty session
            timeComponent.add(nbsp());
            roomStyle = RoomStyle.EMPTY;
        } else if (startTime.isAfter(now)) { // next session
            timeComponent.add(
                    new Icon(VaadinIcon.ALARM),
                    new Text(String.format("%s - %s", startTime, endTime))
            );
            roomStyle = RoomStyle.NEXT;
        } else { // running session
            final Duration duration = Duration.between(now, endTime);
            final int minutesLeft = Math.round(duration.getSeconds() / 60f);
            timeComponent.add(new Icon(VaadinIcon.HOURGLASS));
            if (minutesLeft <= 0) {
                timeComponent.add(new Text(getTranslation("event.session.countdown.now")));
            } else if (minutesLeft == 1) {
                timeComponent.add(new Text(getTranslation("event.session.countdown.one-minute")));
            } else {
                timeComponent.add(new Text(getTranslation("event.session.countdown.minutes", minutesLeft)));
            }
            roomStyle = RoomStyle.RUNNING;
        }
        return timeComponent;
    }

    @NotNull
    private Component createImageComponent() {
        final var speakerAvatars = speakers.stream()
                .filter(speaker -> speaker.imageUrl() != null && !speaker.imageUrl().isBlank())
                .map(speaker -> new Avatar(speaker.fullName(), speaker.imageUrl()))
                .toArray(Avatar[]::new);
        if (speakerAvatars.length == 0) {
            return createTrackComponent();
        }

        final var avatarGroup = new Div();
        avatarGroup.addClassName("avatar-group");
        avatarGroup.add(speakerAvatars);

        final var avatarComponent = new Div();
        avatarComponent.addClassName("avatar");
        avatarComponent.add(avatarGroup);
        return avatarComponent;
    }

    @NotNull
    private Component createTrackComponent() {
        final var trackComponent = new Div();
        trackComponent.addClassName("track");
        if (track != null && track != Track.NONE) {
            final var trackImage = new Svg(track.svgCode());
            trackComponent.add(trackImage);
        }
        return trackComponent;
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
