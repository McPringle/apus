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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.ocpsoft.prettytime.PrettyTime;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.service.SocialService;
import swiss.fihlon.apus.social.Message;

@CssImport(value = "./themes/apus/views/message-view.css")
public final class MessageView extends Div {

    private static final int MAX_LENGTH = 500;
    private static final String TRUNC_INDICATOR = " […]";
    private final transient SocialService socialService;
    private final transient Configuration configuration;

    public MessageView(@NotNull final Message message,
                       @NotNull final SocialService socialService,
                       @NotNull final Configuration configuration) {
        this.socialService = socialService;
        this.configuration = configuration;
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
        final var avatar = new Avatar(message.author(), message.avatar());
        if (!configuration.getAdmin().password().isBlank()) {
            final var menu = new ContextMenu();
            menu.addItem(getTranslation("social.message.contextmenu.hide"), event -> confirmHideMessage(message));
            menu.setTarget(avatar);
        }
        return avatar;
    }

    private void confirmHideMessage(@NotNull final Message message) {
        final var dialog = new ConfirmDialog();
        dialog.setHeader(getTranslation("social.message.dialog.hide.confirm.title"));
        dialog.setText(getTranslation("social.message.dialog.hide.confirm.text", message.author(), message.date()));
        dialog.setCloseOnEsc(true);

        dialog.setCancelable(true);
        dialog.setCancelButton(getTranslation("social.message.dialog.hide.confirm.cancel"), event -> dialog.close());

        dialog.setConfirmText(getTranslation("social.message.dialog.hide.confirm.button"));
        dialog.addConfirmListener(event -> {
            dialog.close();
            authorizeHideMessage(message);
        });

        dialog.open();
    }

    private void authorizeHideMessage(@NotNull final Message message) {
        final Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("social.message.dialog.hide.authorize.title"));
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        final PasswordField passwordField = new PasswordField();
        passwordField.setPlaceholder(getTranslation("social.message.dialog.hide.authorize.password"));
        passwordField.setRequired(true);
        passwordField.setValueChangeMode(ValueChangeMode.EAGER);

        final Button hideButton = new Button(getTranslation("social.message.dialog.hide.authorize.button"), event -> {
            dialog.close();
            hideMessage(message, passwordField.getValue());
        });
        hideButton.setEnabled(false);
        hideButton.setDisableOnClick(true);
        hideButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final Button cancelButton = new Button(getTranslation("social.message.dialog.hide.authorize.cancel"), event -> dialog.close());
        cancelButton.setDisableOnClick(true);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(hideButton, cancelButton);

        passwordField.addKeyDownListener(event -> hideButton.setEnabled(!passwordField.isEmpty()));
        dialog.add(passwordField);

        dialog.open();
        passwordField.focus();
    }

    private void hideMessage(@NotNull final Message message, @NotNull final String password) {
        if (password.equals(configuration.getAdmin().password())) {
            socialService.hideMessage(message);
            removeFromParent();
            Notification.show(getTranslation("social.message.notification.hide.success", message.author()));
        } else {
            Notification.show(getTranslation("social.message.notification.hide.rejected"));
        }
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
        final var dateTimeComponent = new Footer();
        dateTimeComponent.addClassName("datetime");
        final var prettyTime = new PrettyTime(UI.getCurrent().getLocale());
        dateTimeComponent.add(new Text(prettyTime.format(message.date())));
        return dateTimeComponent;
    }
}
