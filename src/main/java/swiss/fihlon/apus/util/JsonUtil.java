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

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public final class JsonUtil {

    public static @NotNull String getStringOrDefault(final @NotNull JSONObject jsonObject,
                                                     final @NotNull String key,
                                                     final @NotNull String defaultValue) {
        if (jsonObject.has(key) && !jsonObject.isNull(key)) {
            final var value = jsonObject.getString(key);
            if (!value.isBlank()) {
                return value;
            }
        }
        return defaultValue;
    }

    private JsonUtil() {
        throw new IllegalStateException("Utility classes can't be instantiated!");
    }

}
