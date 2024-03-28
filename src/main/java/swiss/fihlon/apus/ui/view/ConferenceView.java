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

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import swiss.fihlon.apus.conference.Session;
import swiss.fihlon.apus.service.ConferenceService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@CssImport(value = "./themes/apus/views/conference-view.css")
public final class ConferenceView extends Div {

    private static final int MAX_ROOMS_IN_VIEW = 12;
    private static final Duration UPDATE_FREQUENCY = Duration.ofMinutes(1);

    private final transient ConferenceService conferenceService;
    private final Div sessionContainer = new Div();

    public ConferenceView(@NotNull final ConferenceService conferenceService,
                          @NotNull final TaskScheduler taskScheduler) {
        this.conferenceService = conferenceService;
        setId("conference-view");
        add(new H2("Agenda"));
        add(sessionContainer);
        updateConferenceSessions();
        final ScheduledFuture<?> updateScheduler = taskScheduler.scheduleAtFixedRate(this::updateScheduler, UPDATE_FREQUENCY);
        addDetachListener(event -> updateScheduler.cancel(true));
    }

    private void updateScheduler() {
        getUI().ifPresent(ui -> ui.access(this::updateConferenceSessions));
    }

    private void updateConferenceSessions() {
        sessionContainer.removeAll();
        final var sessionCounter = new AtomicInteger(0);
        final var today = LocalDate.now();
        for (final Map.Entry<String, List<Session>> stringListEntry : conferenceService.getRoomsWithSessions().entrySet()) {
            if (sessionCounter.get() >= MAX_ROOMS_IN_VIEW) {
                // TODO log error
                break;
            }
            final List<Session> sessions = stringListEntry.getValue();
            if (!sessions.isEmpty()) {
                final Session session = sessions.getFirst();
                if (session.startDate().toLocalDate().isEqual(today)) {
                    final var sessionView = new SessionView(session);
                    sessionView.setId("session-" + sessionCounter.getAndIncrement());
                    sessionContainer.add(sessionView);
                }
            }
        }
    }
}
