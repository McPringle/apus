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
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import org.jetbrains.annotations.NotNull;
import org.ocpsoft.prettytime.PrettyTime;
import swiss.fihlon.apus.social.Message;
import swiss.fihlon.apus.util.HtmlUtil;

@CssImport(value = "./themes/apus/views/message-view.css")
public final class MessageView extends Div {

    public MessageView(@NotNull final Message message) {
        setId("message-" + message.id());
        addClassName("message-view");
        add(createHeaderComponent(message));
        add(createTextComponent(message));
        add(createImageComponents(message));
        add(createDateTimeComponent(message));
    }

    @NotNull Component createHeaderComponent(@NotNull final Message message) {
        final var avatar = createAvatarComponent(message);
        final var author = new Div(new Text(message.author()));
        author.addClassName("author");
        final var profile = new Div(new Text(message.profile()));
        profile.addClassName("profile");
        final var authorContainer = new Div(author, profile);
        authorContainer.addClassName("author-container");
        return new Header(avatar, authorContainer);
    }

    private Component createAvatarComponent(@NotNull final Message message) {
        return new Avatar(message.author(), message.avatar());
    }

    @NotNull
    private Component createTextComponent(@NotNull final Message message) {
        final String unsafeHtml = message.html();
        final String saveHtml = HtmlUtil.sanitize(unsafeHtml);
        return new Html(String.format("<div class=\"content\">%s</div>", saveHtml));
    }

    @NotNull
    private Component[] createImageComponents(@NotNull final Message message) {
        return message.images().stream()
                .map(image -> new Image(image, image))
                .toArray(Image[]::new);
    }

    @NotNull
    private Component createDateTimeComponent(@NotNull final Message message) {
        final var dateTimeComponent = new Footer();
        dateTimeComponent.addClassName("datetime");
        final var prettyTime = new PrettyTime(UI.getCurrent().getLocale());
        dateTimeComponent.add(new Text(prettyTime.format(message.date())));
        return dateTimeComponent;
    }
}
