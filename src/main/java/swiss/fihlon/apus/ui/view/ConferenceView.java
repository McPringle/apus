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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@CssImport(value = "./themes/apus/views/conference-view.css")
public final class ConferenceView extends Div {

    private static final int MAX_SESSIONS_IN_VIEW = 15;
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
        var sessionCounter = new AtomicInteger(0);
        addRunningSessions(sessionCounter);
        addNextSessions(sessionCounter);
    }

    private void addRunningSessions(@NotNull final AtomicInteger sessionCounter) {
        final var runningSessions = conferenceService.getRunningSessions();
        for (final Session session : runningSessions) {
            final var sessionView = new SessionView(session);
            sessionView.addClassName("running-session");
            sessionContainer.add(sessionView);
            if (sessionCounter.incrementAndGet() >= MAX_SESSIONS_IN_VIEW) {
                break;
            }
        }
    }

    private void addNextSessions(@NotNull final AtomicInteger sessionCounter) {
        // There is space for 15 sessions on the screen, 5 rows with 3 sessions each.
        // Fill up free space with next sessions.
        if (sessionCounter.get() < MAX_SESSIONS_IN_VIEW) {
            final var nextSessions = conferenceService.getNextSessions();
            for (final Session session : nextSessions) {
                final var sessionView = new SessionView(session);
                sessionView.addClassName("next-session");
                sessionContainer.add(sessionView);
                if (sessionCounter.incrementAndGet() >= MAX_SESSIONS_IN_VIEW) {
                    break;
                }
            }
        }
    }
}
