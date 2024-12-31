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

import com.vaadin.flow.component.HasComponents;
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
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.plugin.social.SocialService;
import swiss.fihlon.apus.social.Post;
import swiss.fihlon.apus.util.PasswordUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@CssImport(value = "./themes/apus/views/social-view.css")
public final class SocialView extends Div {

    private static final Duration UPDATE_FREQUENCY = Duration.ofSeconds(30);

    private final transient SocialService socialService;
    private final transient AppConfig appConfig;
    private final List<Div> postsColumns;
    private final ContextMenu contextMenu;
    private boolean adminModeEnabled = false;

    public SocialView(@NotNull final SocialService socialService,
                      @NotNull final TaskScheduler taskScheduler,
                      @NotNull final AppConfig appConfig) {
        this.socialService = socialService;
        this.appConfig = appConfig;

        setId("social-view");
        if (appConfig.social().headline().isBlank()) {
            add(new H2(getTranslation("social.heading", appConfig.mastodon().hashtags().split(",")[0])));
        } else {
            add(new H2(appConfig.social().headline()));
        }
        var postsColumnsDiv = new Div();
        postsColumnsDiv.addClassName("posts");
        add(postsColumnsDiv);

        final int numberOfColumns = appConfig.social().numberOfColumns();
        postsColumns = new ArrayList<>(numberOfColumns);
        for (int i = 0; i < numberOfColumns; i++) {
            final var postsContainer = new Div();
            postsColumns.add(postsContainer);
            postsContainer.addClassName("column");
            postsColumnsDiv.add(postsContainer);
        }

        if (adminModeEnabled || appConfig.admin().password().isBlank()) {
            contextMenu = null;
        } else {
            contextMenu = new ContextMenu();
            contextMenu.addItem(getTranslation("social.admin.login.menu"), event -> showLoginDialog());
            contextMenu.setTarget(postsColumnsDiv);
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
        if (PasswordUtil.matches(password, appConfig.admin().password())) {
            adminModeEnabled = true;
            contextMenu.setTarget(null);
            updatePosts();
            Notification.show(getTranslation("social.admin.login.successful"));
        } else {
            Notification.show(getTranslation("social.admin.login.rejected"));
        }
    }

    private void updateScheduler() {
        getUI().ifPresent(ui -> ui.access(this::updatePosts));
    }

    private void updatePosts() {
        postsColumns.forEach(HasComponents::removeAll);
        int i = 0;
        for (final Post post : socialService.getPosts(30)) {
            final var postView = new PostView(post);
            if (adminModeEnabled) {
                final var postMenu = new ContextMenu();
                postMenu.addItem(getTranslation("social.post.contextmenu.hide.post"), event -> hidePost(post));
                postMenu.addItem(getTranslation("social.post.contextmenu.block.profile"), event -> blockProfile(post));
                postMenu.setTarget(postView);
            }
            postsColumns.get(i++ % postsColumns.size()).add(postView);
        }
    }

    private void hidePost(@NotNull final Post post) {
        socialService.hidePost(post);
        Notification.show(getTranslation("social.post.contextmenu.hide.post.done"));
        updatePosts();
    }

    private void blockProfile(@NotNull final Post post) {
        socialService.blockProfile(post);
        Notification.show(getTranslation("social.post.contextmenu.block.profile.done"));
        updatePosts();
    }
}
