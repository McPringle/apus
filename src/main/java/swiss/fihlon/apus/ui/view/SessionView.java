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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.jetbrains.annotations.NotNull;
import swiss.fihlon.apus.conference.Session;

import java.time.Duration;
import java.time.LocalDateTime;

public final class SessionView extends VerticalLayout {

    public SessionView(@NotNull final Session session) {
        setPadding(false);
        addClassName("session-view");
        addClassName("card");
        addAndExpand(new VerticalLayout(new H3(session.title())));
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("card-footer");
        layout.setWidthFull();
        layout.add(new Div("\uD83D\uDC64 " + session.speaker()));
        layout.add(new Div("\uD83D\uDCCD " + String.format("Room %s", session.room())));

        final var now = LocalDateTime.now();
        if (session.startDate().isBefore(now) && session.endDate().isAfter(now)) { // running session
            final Duration duration = Duration.between(LocalDateTime.now(), session.endDate());
            final long timeLeft = Math.round(duration.getSeconds() / 60f);
            final String timeUnit = timeLeft == 1 ? "minute" : "minutes";
            layout.add(new Div("⌛ " + String.format("%d %s left", timeLeft, timeUnit)));
        } else {
            layout.add(new Div("⌚ " + session.startDate().toLocalTime() + " - " + session.endDate().toLocalTime()));
        }
        add(layout);
    }

}
