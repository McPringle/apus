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
import swiss.fihlon.apus.conference.Session;
import swiss.fihlon.apus.service.ConferenceService;

import java.util.concurrent.atomic.AtomicInteger;

@CssImport(value = "./themes/apus/views/conference-view.css")
public final class ConferenceView extends Div {

    private static final int MAX_SESSIONS_IN_VIEW = 15;

    public ConferenceView(@NotNull final ConferenceService conferenceService) {
        setId("conference-view");
        final var sessionContainer = new Div();
        var sessionCounter = new AtomicInteger(0);

        final var runningSessions = conferenceService.getRunningSessions();
        add(new H2("Agenda"));
        for (final Session session : runningSessions) {
            final var sessionView = new SessionView(session);
            sessionView.addClassName("running-session");
            sessionContainer.add(sessionView);
            if (sessionCounter.incrementAndGet() >= MAX_SESSIONS_IN_VIEW) {
                break;
            }
        }

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

        add(sessionContainer);
    }

}
