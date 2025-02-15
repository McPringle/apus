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
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import org.jetbrains.annotations.NotNull;
import org.ocpsoft.prettytime.PrettyTime;
import swiss.fihlon.apus.social.Post;
import swiss.fihlon.apus.util.HtmlUtil;

import java.util.Locale;

@CssImport(value = "./themes/apus/views/post-view.css")
public final class PostView extends Div {

    @NotNull
    private final Locale locale;

    public PostView(@NotNull final Post post, @NotNull final Locale locale) {
        this.locale = locale;
        setId("post-" + post.id());
        addClassName("post-view");
        add(createHeaderComponent(post));
        add(createTextComponent(post));
        add(createImageComponents(post));
        add(createDateTimeComponent(post));
    }

    @NotNull
    private Component createHeaderComponent(@NotNull final Post post) {
        final var avatar = createAvatarComponent(post);
        final var author = new Div(new Text(post.author()));
        author.addClassName("author");
        final var profile = new Div(new Text(post.profile()));
        profile.addClassName("profile");
        final var authorContainer = new Div(author, profile);
        authorContainer.addClassName("author-container");
        final var header = new Header(avatar, authorContainer);
        header.addClassName("header");
        return header;
    }

    private Component createAvatarComponent(@NotNull final Post post) {
        final var avatar = new Avatar(post.author(), post.avatar());
        avatar.addClassName("avatar");
        return avatar;
    }

    @NotNull
    private Component createTextComponent(@NotNull final Post post) {
        final String unsafeHtml = post.html();
        final String safeHtml = HtmlUtil.sanitize(unsafeHtml);
        return new Html(String.format("<div class=\"content\">%s</div>", safeHtml));
    }

    @NotNull
    private Component[] createImageComponents(@NotNull final Post post) {
        return post.images().stream()
                .map(image -> new Image(image, image))
                .toArray(Image[]::new);
    }

    @NotNull
    private Component createDateTimeComponent(@NotNull final Post post) {
        final var dateTimeComponent = new Footer();
        dateTimeComponent.addClassName("datetime");
        final var prettyTime = new PrettyTime(locale);
        dateTimeComponent.add(new Text(prettyTime.format(post.date())));
        return dateTimeComponent;
    }
}
