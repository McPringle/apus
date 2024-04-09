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

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.TaskScheduler;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.service.SocialService;
import swiss.fihlon.apus.social.Message;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

@CssImport(value = "./themes/apus/views/social-view.css")
public final class SocialView extends Div {

    private static final Duration UPDATE_FREQUENCY = Duration.ofSeconds(30);

    private final transient SocialService socialService;
    private final transient Configuration configuration;
    private final Div messageContainer = new Div();
    private final ContextMenu contextMenu;
    private boolean adminModeEnabled = false;

    public SocialView(@NotNull final SocialService socialService,
                      @NotNull final TaskScheduler taskScheduler,
                      @NotNull final Configuration configuration) {
        this.socialService = socialService;
        this.configuration = configuration;

        setId("social-view");
        add(new H2(getTranslation("social.heading", configuration.getMastodon().hashtag())));
        add(messageContainer);
        messageContainer.addClassName("masonry");

        if (adminModeEnabled || configuration.getAdmin().password().isBlank()) {
            contextMenu = null;
        } else {
            contextMenu = new ContextMenu();
            contextMenu.addItem(getTranslation("social.admin.login.menu"), event -> showLoginDialog());
            contextMenu.setTarget(messageContainer);
        }

        final ScheduledFuture<?> updateScheduler = taskScheduler.scheduleAtFixedRate(
                this::updateScheduler, Instant.now().plusSeconds(1), UPDATE_FREQUENCY);
        addDetachListener(event -> updateScheduler.cancel(true));
    }

    private void showLoginDialog() {
        final Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("social.admin.login.title"));
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        final PasswordField passwordField = new PasswordField();
        passwordField.setPlaceholder(getTranslation("social.admin.login.password"));
        passwordField.setRequired(true);
        passwordField.setValueChangeMode(ValueChangeMode.EAGER);

        final Button loginButton = new Button(getTranslation("social.admin.login.button"), event -> {
            handleLogin(passwordField.getValue());
            dialog.close();
        });
        loginButton.setEnabled(false);
        loginButton.setDisableOnClick(true);
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final Button cancelButton = new Button(getTranslation("social.admin.login.cancel"), event -> dialog.close());
        cancelButton.setDisableOnClick(true);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(loginButton, cancelButton);

        passwordField.addKeyDownListener(event -> {
            loginButton.setEnabled(!passwordField.isEmpty());
            if (event.getKey().equals(Key.ENTER)) {
                handleLogin(passwordField.getValue());
                dialog.close();
            }
        });
        dialog.add(passwordField);

        dialog.open();
        passwordField.focus();
    }

    private void handleLogin(@NotNull final String password) {
        if (configuration.getAdmin().password().equals(password)) {
            adminModeEnabled = true;
            contextMenu.setTarget(null);
            updateMessages();
            Notification.show(getTranslation("social.admin.login.successful"));
        } else {
            Notification.show(getTranslation("social.admin.login.rejected"));
        }
    }

    private void updateScheduler() {
        getUI().ifPresent(ui -> ui.access(this::updateMessages));
    }

    private void updateMessages() {
        messageContainer.removeAll();
        for (final Message message : socialService.getMessages(30)) {
            final var messageView = new MessageView(message);
            if (adminModeEnabled) {
                final var messageMenu = new ContextMenu();
                messageMenu.addItem(getTranslation("social.message.contextmenu.hide.message"), event -> hideMessage(message));
                messageMenu.addItem(getTranslation("social.message.contextmenu.hide.profile"), event -> blockProfile(message));
                messageMenu.setTarget(messageView);
            }
            messageContainer.add(messageView);
        }
    }

    private void hideMessage(@NotNull final Message message) {
        socialService.hideMessage(message);
        Notification.show(getTranslation("social.message.contextmenu.hide.message.done"));
        updateMessages();
    }

    private void blockProfile(@NotNull final Message message) {
        socialService.hideProfile(message);
        Notification.show(getTranslation("social.message.contextmenu.hide.profile.done"));
        updateMessages();
    }
}
