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
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import swiss.fihlon.apus.service.SocialService;
import swiss.fihlon.apus.social.Message;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

@Component
@Scope("prototype")
public final class SocialView extends Div {

    private static final Duration UPDATE_FREQUENCY = Duration.ofMinutes(1);

    private final transient SocialService socialService;
    private final Div messageContainer = new Div();

    public SocialView(@NotNull final SocialService socialService,
                      @NotNull final TaskScheduler taskScheduler) {
        this.socialService = socialService;
        setId("social-view");
        add(new H2("#JavaLand on Mastodon"));
        add(messageContainer);
        messageContainer.addClassName("masonry");
        updateMessages();
        final ScheduledFuture<?> updateScheduler = taskScheduler.scheduleAtFixedRate(this::updateScheduler, UPDATE_FREQUENCY);
        addDetachListener(event -> updateScheduler.cancel(true));
    }

    private void updateScheduler() {
        getUI().ifPresent(ui -> ui.access(this::updateMessages));
    }

    private void updateMessages() {
        messageContainer.removeAll();
        for (final Message message : socialService.getMessages(30)) {
            messageContainer.add(new MessageView(message));
        }
    }
}
