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
package swiss.fihlon.apus;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class ApplicationServiceInitListener implements VaadinServiceInitListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceInitListener.class);

    @Override
    public void serviceInit(@NotNull final ServiceInitEvent serviceInitEvent) {
        serviceInitEvent.addRequestHandler((session, request, response) -> {
            LOGGER.info("Request handled with 'Accept-Language' header set to '{}'.", request.getHeader("Accept-Language"));
            return false;
        });
    }

}
