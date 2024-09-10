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
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.plugin.event.EventService;
import swiss.fihlon.apus.plugin.social.SocialService;

import java.util.Arrays;

@Route("")
@CssImport(value = "./themes/apus/views/social-wall-view.css")
public final class SocialWallView extends Div {

    public static final Logger LOGGER = LoggerFactory.getLogger(SocialWallView.class);

    public SocialWallView(@NotNull final EventService eventService,
                          @NotNull final SocialService socialService,
                          @NotNull final TaskScheduler taskScheduler,
                          @NotNull final Configuration configuration) {
        setId("social-wall-view");
        addDynamicStyles(configuration, eventService);
        addCustomStyles(configuration);
        if (eventService.isEnabled()) {
            add(new EventView(eventService, taskScheduler, configuration));
        }
        add(new SocialView(socialService, taskScheduler, configuration));
    }

    private static void addDynamicStyles(@NotNull final Configuration configuration, @NotNull EventService eventService) {
        final var currentStyle = UI.getCurrent().getElement().getStyle();
        currentStyle.set("--social-post-column-count", Integer.toString(configuration.getSocial().numberOfColumns()));
        if (eventService.isEnabled()) {
            final int roomCount = eventService.getRoomsWithSessions().size();
            final int columnCount = Math.ceilDiv(roomCount, 6);
            final int eventWidth = 5 + (435 * columnCount);
            final String eventWidthCSS = eventWidth + "px";
            currentStyle.set("--event-width", eventWidthCSS);
            LOGGER.info("Room count of {} will lead to column count of {} and an event width of {}", roomCount, columnCount, eventWidthCSS);
        } else {
            currentStyle.set("--event-width", "0");
        }
    }

    private static void addCustomStyles(@NotNull final Configuration configuration) {
        final String customStyles = configuration.getCustom().styles();
        if (!customStyles.isBlank()) {
            final var currentStyle = UI.getCurrent().getElement().getStyle();
            Arrays.stream(customStyles.split(";"))
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
