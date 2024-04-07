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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.TaskScheduler;
import swiss.fihlon.apus.conference.Room;
import swiss.fihlon.apus.conference.RoomStyle;
import swiss.fihlon.apus.conference.Session;
import swiss.fihlon.apus.service.ConferenceService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

@CssImport(value = "./themes/apus/views/conference-view.css")
public final class ConferenceView extends Div {

    public static final String LABEL_THEME = "badge";
    private static final int MAX_ROOMS_IN_VIEW = 12;
    private static final Duration UPDATE_FREQUENCY = Duration.ofMinutes(1);
    private static final Duration TIME_LIMIT_NEXT_SESSION = Duration.ofHours(1);

    private final transient ConferenceService conferenceService;
    private final Div roomContainer = new Div();
    private final Span legend = new Span();

    public ConferenceView(@NotNull final ConferenceService conferenceService,
                          @NotNull final TaskScheduler taskScheduler) {
        this.conferenceService = conferenceService;
        setId("conference-view");
        add(new H2(getTranslation("conference.heading",
                LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, UI.getCurrent().getLocale()))));
        add(createLegend());
        add(roomContainer);
        final ScheduledFuture<?> updateScheduler = taskScheduler.scheduleAtFixedRate(
                this::updateScheduler, Instant.now().plusSeconds(10), UPDATE_FREQUENCY);
        addDetachListener(event -> updateScheduler.cancel(true));
    }

    private void updateScheduler() {
        getUI().ifPresent(ui -> ui.access(this::updateConferenceSessions));
    }

    private void updateConferenceSessions() {
        roomContainer.removeAll();
        final var today = LocalDate.now();
        final var roomCounter = new AtomicInteger(0);
        final var roomsWithSessions = conferenceService.getRoomsWithSessions().entrySet();
        if (roomsWithSessions.isEmpty()) {
            Notification.show(getTranslation("conference.error.nosessions"));
        }
        final var roomStylesInUse = new HashSet<RoomStyle>();
        for (final Map.Entry<Room, List<Session>> roomWithSession : roomsWithSessions) {
            if (roomCounter.get() >= MAX_ROOMS_IN_VIEW) {
                Notification.show(String.format(getTranslation("conference.error.rooms"), roomsWithSessions.size()));
                break;
            }
            final RoomView roomView = createRoomView(roomWithSession, today, roomCounter);
            roomStylesInUse.add(roomView.getRoomStyle());
            roomContainer.add(roomView);
        }
        updateLegend(roomStylesInUse);
    }

    @NotNull
    private Component createLegend() {
        legend.addClassName("legend");
        return legend;
    }

    /**
     * Important implementation detail:
     * The loop iterates over the enum, not the used values, so the order of the
     * items in the legend are predictable (based on the order of the enum values).
     *
     * @param roomStylesInUse a <code>Set</code> of all <code>RoomStyle</code>s in use
     */
    private void updateLegend(@NotNull final Set<RoomStyle> roomStylesInUse) {
        legend.removeAll();
        for (final RoomStyle roomStyle : RoomStyle.values()) {
            if (roomStylesInUse.contains(roomStyle)) {
                final Component legendItem = new Span(getTranslation(roomStyle.getTranslationKey()));
                legendItem.getElement().getThemeList().add(LABEL_THEME);
                legendItem.addClassName(roomStyle.getCssStyle());
                legend.add(legendItem);
            }
        }
    }

    @NotNull
    private static RoomView createRoomView(@NotNull final Map.Entry<Room, List<Session>> roomWithSession,
                                              @NotNull final LocalDate today,
                                              @NotNull final AtomicInteger roomCounter) {
        final LocalDateTime timeLimitNextSession = LocalDateTime.now().plus(TIME_LIMIT_NEXT_SESSION);
        final Room room = roomWithSession.getKey();
        final List<Session> sessions = roomWithSession.getValue();
        final Session session = sessions.isEmpty() ? null : sessions.getFirst();
        final RoomView roomView;
        if (session != null
                && session.startDate().toLocalDate().isEqual(today)
                && session.startDate().isBefore(timeLimitNextSession)) {
            roomView = new RoomView(session);
        } else {
            roomView = new RoomView(room);
        }
        roomView.setId("room-" + roomCounter.getAndIncrement());
        return roomView;
    }
}
