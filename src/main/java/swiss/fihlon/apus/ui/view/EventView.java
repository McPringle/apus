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
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.TaskScheduler;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.RoomStyle;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.plugin.event.EventService;
import swiss.fihlon.apus.util.VaadinUtil;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

@CssImport(value = "./themes/apus/views/event-view.css")
public final class EventView extends Div {

    public static final @NotNull String LABEL_THEME = "badge";
    private static final @NotNull Duration UPDATE_FREQUENCY = Duration.ofMinutes(1);

    private final transient @NotNull EventService eventService;
    private final @NotNull Duration nextSessionTimeout;
    private final boolean showLegend;
    private final boolean showEmptyRooms;
    private final @NotNull Div roomContainer = new Div();
    private final @NotNull Span legend = new Span();
    private final @NotNull ZoneId timezone;

    public EventView(final @NotNull EventService eventService,
                     final @NotNull TaskScheduler taskScheduler,
                     final @NotNull AppConfig appConfig) {
        this.eventService = eventService;
        this.timezone = appConfig.timezone();
        this.nextSessionTimeout = Duration.ofMinutes(appConfig.event().nextSessionTimeout());
        this.showLegend = appConfig.event().showLegend();
        this.showEmptyRooms = appConfig.event().showEmptyRooms();
        setId("event-view");
        add(createTitle());
        if (showLegend) {
            add(createLegend());
        }
        add(roomContainer);
        roomContainer.addClassName("room-container");
        final ScheduledFuture<?> updateScheduler = taskScheduler.scheduleAtFixedRate(
                () -> getUI().ifPresent(ui -> VaadinUtil.updateUI(ui, this::updateConferenceSessions)),
                Instant.now().plusSeconds(1), UPDATE_FREQUENCY);
        addDetachListener(event -> updateScheduler.cancel(true));

        final var imageUrl = appConfig.event().image();
        if (!imageUrl.isBlank()) {
            add(createImage(imageUrl));
        }
    }

    private void updateConferenceSessions() {
        roomContainer.removeAll();
        final var roomsWithSessions = eventService.getRoomsWithSessions().entrySet();
        if (roomsWithSessions.isEmpty()) {
            Notification.show(getTranslation("event.error.nosessions"));
        }
        final var roomStylesInUse = new HashSet<RoomStyle>();
        for (final Map.Entry<Room, List<Session>> roomWithSessions : roomsWithSessions) {
            final RoomView roomView = createRoomView(roomWithSessions);
            if (!showEmptyRooms && RoomStyle.EMPTY.equals(roomView.getRoomStyle())) {
                continue; // don't show empty rooms when configured to do so
            }
            roomStylesInUse.add(roomView.getRoomStyle());
            roomContainer.add(roomView);
        }
        if (showLegend) {
            updateLegend(roomStylesInUse);
        }
    }

    private @NotNull H2 createTitle() {
        return new H2(getTranslation("event.heading"));
    }

    private @NotNull Component createLegend() {
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
    private void updateLegend(final @NotNull Set<@NotNull RoomStyle> roomStylesInUse) {
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

    private @NotNull RoomView createRoomView(final @NotNull Map.Entry<@NotNull Room, @NotNull List<@NotNull Session>> roomWithSessions) {
        final LocalDate today = LocalDate.now(timezone);
        final ZonedDateTime timeLimitNextSession = ZonedDateTime.now(timezone).plus(nextSessionTimeout);
        final Room room = roomWithSessions.getKey();
        final List<Session> sessions = roomWithSessions.getValue();
        final ZonedDateTime now = ZonedDateTime.now(timezone).withSecond(59).withNano(999);
        final Session session = sessions.isEmpty() ? null : sessions.stream()
                .filter(s -> s.endDate().isAfter(now))
                .findFirst()
                .orElse(null);
        final RoomView roomView;
        if (session != null
                && session.startDate().toLocalDate().isEqual(today)
                && session.startDate().isBefore(timeLimitNextSession)) {
            roomView = new RoomView(timezone, session);
        } else {
            roomView = new RoomView(timezone, room);
        }
        return roomView;
    }

    private @NotNull Component createImage(final @NotNull String imageUrl) {
        final var image = new Image(imageUrl, "Event Image");
        image.setId("event-image");
        return image;
    }
}
