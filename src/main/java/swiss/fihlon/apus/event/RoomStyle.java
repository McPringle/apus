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

public enum RoomStyle {

    // Important: When adding a new room style, modify the test accordingly!
    NONE("", ""),
    RUNNING("running-session", "event.legend.running-session"),
    NEXT("next-session", "event.legend.next-session"),
    EMPTY("empty-room", "event.legend.empty-room");

    private final String cssStyle;
    private final String translationKey;

    RoomStyle(@NotNull final String cssStyle, @NotNull final String translationKey) {
        this.cssStyle = cssStyle;
        this.translationKey = translationKey;
    }

    public String getCssStyle() {
        return cssStyle;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
