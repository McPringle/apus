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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import swiss.fihlon.apus.conference.Session;
import swiss.fihlon.apus.conference.Speaker;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@CssImport(value = "./themes/apus/views/session-view.css")
public final class SessionView extends Div {

    private final String room;
    private final String title;
    private final transient List<Speaker> speakers;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public SessionView(@NotNull final String room) {
        this(room, null, List.of(), null, null);
    }

    public SessionView(@NotNull final Session session) {
        this(
                session.room(),
                session.title(),
                session.speakers(),
                session.startDate().toLocalTime(),
                session.endDate().toLocalTime()
        );
    }

    public SessionView(@NotNull final String room,
                       @Nullable final String title,
                       @NotNull final List<Speaker> speakers,
                       @Nullable final LocalTime startTime,
                       @Nullable final LocalTime endTime) {
        this.room = room;
        this.title = title;
        this.speakers = speakers;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    protected void onAttach(@NotNull final AttachEvent attachEvent) {
        addClassName("session-view");
        add(createTitleComponent());
        add(createSpeakersComponent());
        add(createRoomComponent());
        add(createTimeComponent());
    }

    @NotNull
    private Component createTitleComponent() {
         return new H3(new Text(title == null ? "CLOSED" : title));
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
        return new Div(new Text(String.format("\uD83D\uDCCD %s", room)));
    }

    @NotNull
    private Component createTimeComponent() {
        final var timeComponent = new Div();
        final var now = LocalTime.now();
        if (startTime == null || endTime == null) { // empty session
            timeComponent.add(nbsp());
            addClassName("empty-session");
        } else if (startTime.isBefore(now) && endTime.isAfter(now)) { // running session
            final Duration duration = Duration.between(now, endTime);
            final long timeLeft = Math.round(duration.getSeconds() / 60f);
            final String timeUnit = timeLeft == 1 ? "minute" : "minutes";
            timeComponent.add(new Text(String.format("⌛ %d %s left", timeLeft, timeUnit)));
            addClassName("running-session");
        } else { // next session
            timeComponent.add(new Text(String.format("⌚ %s - %s", startTime, endTime)));
            addClassName("next-session");
        }
        return timeComponent;
    }

    @NotNull
    private static Component nbsp() {
        return new Html("<span>&nbsp;</span>");
    }

}
