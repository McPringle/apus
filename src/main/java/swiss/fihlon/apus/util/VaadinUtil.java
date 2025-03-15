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
package swiss.fihlon.apus.util;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Command;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class VaadinUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(VaadinUtil.class);

    @SuppressWarnings("java:S2142") // logging the exceptions is enough
    public static void updateUI(final @NotNull UI ui, final @NotNull Command command) {
        CompletableFuture.supplyAsync(() -> {
            try {
                var future = ui.access(command);
                if (future != null) {
                    future.get();
                }
            } catch (final InterruptedException | ExecutionException e) {
                LOGGER.error("Exception thrown while updating the UI: {}", e.getMessage(), e);
            }
            return null; // return type is Void
        }).exceptionally(throwable -> {
            LOGGER.error("Exception thrown while updating the UI: {}", throwable.getMessage(), throwable);
            return null; // return type is Void
        });
    }

    private VaadinUtil() {
        throw new IllegalStateException("Utility classes can't be instantiated!");
    }

}
