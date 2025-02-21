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
package swiss.fihlon.apus.social;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record Post(@NotNull String id, @NotNull LocalDateTime date,
                   @NotNull String author, @NotNull String avatar, @NotNull String profile,
                   @NotNull String html, @NotNull List<String> images,
                   boolean isReply, boolean isSensitive, @NotNull String sourceLogo)
        implements Comparable<Post> {

    @Override
    public int compareTo(@NotNull final Post other) {
        final int dateCompareResult = other.date.compareTo(date);
        return dateCompareResult == 0 ? id.compareTo(other.id) : dateCompareResult;
    }

    public Post withImages(@NotNull final List<String> newImages) {
        return new Post(id, date, author, avatar, profile, html, List.copyOf(newImages), isReply, isSensitive, sourceLogo);
    }

}
