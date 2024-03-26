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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import swiss.fihlon.apus.service.ConferenceService;
import swiss.fihlon.apus.service.SocialService;

import java.io.Serial;

@Route("")
@CssImport(value = "./themes/apus/views/social-wall.css")
public class SocialWall extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 7909437130138135008L;

    public SocialWall(@NotNull final ConferenceService conferenceService,
                      @NotNull final SocialService socialService) {
        setId("social-wall");
        setSizeFull();

        final var sessionsView = new SessionsView(conferenceService);
        final var postsView = new PostsView(socialService);
        final var layout = new HorizontalLayout(sessionsView, postsView);
        layout.setId("social-wall-container");
        add(layout);
        add(new Div("Footer"));
    }

}
