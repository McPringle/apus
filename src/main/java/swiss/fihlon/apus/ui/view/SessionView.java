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
import com.vaadin.flow.component.html.H3;
import org.jetbrains.annotations.NotNull;
import swiss.fihlon.apus.conference.Session;

import java.time.Duration;
import java.time.LocalDateTime;

@CssImport(value = "./themes/apus/views/session-view.css")
public final class SessionView extends Div {

    public SessionView(@NotNull final Session session) {
        addClassName("session-view");
        add(new H3(session.title()));
        add(new Div(String.format("\uD83D\uDC64 %s", session.speaker())));
        add(new Div(String.format("\uD83D\uDCCD %s", session.room())));

        final var now = LocalDateTime.now();
        if (session.startDate().isBefore(now) && session.endDate().isAfter(now)) { // running session
            final Duration duration = Duration.between(LocalDateTime.now(), session.endDate());
            final long timeLeft = Math.round(duration.getSeconds() / 60f);
            final String timeUnit = timeLeft == 1 ? "minute" : "minutes";
            add(new Div(String.format("⌛ %d %s left", timeLeft, timeUnit)));
            addClassName("running-session");
        } else {
            add(new Div(String.format("⌚ %s - %s", session.startDate().toLocalTime(), session.endDate().toLocalTime())));
            addClassName("next-session");
        }
    }

}
