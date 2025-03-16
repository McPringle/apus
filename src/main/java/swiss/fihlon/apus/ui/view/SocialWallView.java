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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import swiss.fihlon.apus.ApplicationI18NProvider;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.plugin.event.EventService;
import swiss.fihlon.apus.plugin.social.SocialService;

import java.util.Arrays;

@Route("")
@CssImport(value = "./themes/apus/views/social-wall-view.css")
public final class SocialWallView extends Div {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(SocialWallView.class);

    public SocialWallView(final @NotNull EventService eventService,
                          final @NotNull SocialService socialService,
                          final @NotNull TaskScheduler taskScheduler,
                          final @NotNull AppConfig appConfig,
                          final @NotNull ApplicationI18NProvider i18NProvider) {
        setId("social-wall-view");
        addDynamicStyles(appConfig, eventService);
        addCustomStyles(appConfig);
        if (eventService.isEnabled()) {
            add(new EventView(eventService, taskScheduler, appConfig));
        }
        final var locale = i18NProvider.getLocale();
        add(new SocialView(socialService, taskScheduler, appConfig, locale));
    }

    private static void addDynamicStyles(final @NotNull AppConfig appConfig, final @NotNull EventService eventService) {
        final var currentStyle = UI.getCurrent().getElement().getStyle();
        currentStyle.set("--social-post-column-count", Integer.toString(appConfig.social().numberOfColumns()));
        if (eventService.isEnabled()) {
            final int roomCount = eventService.getRoomsWithSessions().size();
            final int columnCount = Math.ceilDiv(roomCount, 6);
            final int eventWidth = 435 * columnCount + 5;
            final String eventWidthCSS = eventWidth + "px";
            currentStyle.set("--event-width", eventWidthCSS);
            currentStyle.set("--event-grid-template-columns", "auto ".repeat(columnCount));
            LOGGER.info("Room count of {} will lead to column count of {} and an event width of {}", roomCount, columnCount, eventWidthCSS);
        } else {
            currentStyle.set("--event-width", "0");
        }
    }

    private static void addCustomStyles(final @NotNull AppConfig appConfig) {
        final String styles = appConfig.styles();
        if (!styles.isBlank()) {
            final var currentStyle = UI.getCurrent().getElement().getStyle();
            Arrays.stream(styles.split(";"))
                    .forEach(customStyle -> {
                        if (!customStyle.isBlank()) {
                            final var style = customStyle.split(":", 2);
                            if (style.length == 2) {
                                currentStyle.set(style[0].trim(), style[1].trim());
                            } else {
                                LOGGER.warn("Custom style has incorrect format: missing ':' (delimiter between key and value)");
                            }
                        } else {
                            LOGGER.warn("Custom style has incorrect format: empty definition (nothing between ';')");
                        }
                    });
        }
    }
}
