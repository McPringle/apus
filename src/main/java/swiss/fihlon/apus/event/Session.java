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
package swiss.fihlon.apus.event;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.List;

public record Session(@NotNull String id, @NotNull ZonedDateTime startDate, @NotNull ZonedDateTime endDate,
                      @NotNull Room room, @NotNull String title, @NotNull List<@NotNull Speaker> speakers,
                      @NotNull Language language, @NotNull Track track)
        implements Comparable<Session> {

    @Override
    public int compareTo(final @NotNull Session other) {
        final var dateCompareResult = startDate.compareTo(other.startDate);
        if (dateCompareResult == 0) {
            return room.compareTo(other.room);
        }
        return dateCompareResult;
    }
}
