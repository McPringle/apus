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
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.ocpsoft.prettytime.PrettyTime;
import swiss.fihlon.apus.social.Message;

@CssImport(value = "./themes/apus/views/message-view.css")
public final class MessageView extends Div {

    private static final int MAX_LENGTH = 500;
    private static final String TRUNC_INDICATOR = " […]";

    public MessageView(@NotNull final Message message) {
        addClassName("message-view");
        add(createTextComponent(message));
        add(createImageComponents(message));
        add(createDateTimeComponent(message));
    }

    @NotNull
    private Component createTextComponent(@NotNull final Message message) {
        final String messageText = Jsoup.parse(message.html()).text();
        return new Html(String.format("<div>%s</div>",
                messageText.length() > MAX_LENGTH ? truncateMessageText(messageText) : message.html()
        ));
    }

    @NotNull
    private String truncateMessageText(@NotNull final String messageText) {
        return "<p>" + messageText.substring(0, MAX_LENGTH) + TRUNC_INDICATOR + "</p>";
    }

    @NotNull
    private Component[] createImageComponents(@NotNull final Message message) {
        return message.images().stream()
                .map(image -> new Image(image, image))
                .toArray(Image[]::new);
    }

    @NotNull
    private Component createDateTimeComponent(@NotNull final Message message) {
        final var dateTimeComponent = new Div();
        dateTimeComponent.addClassName("datetime");
        final var prettyTime = new PrettyTime(UI.getCurrent().getLocale());
        dateTimeComponent.add(new Text(prettyTime.format(message.date())));
        return dateTimeComponent;
    }
}
