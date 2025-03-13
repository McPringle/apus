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
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TemplateUtil {

    public static String replaceVariables(@NotNull final String text,
                                          @Nullable final Map<String, String> variables) {
        String returnValue = text;
        if (variables != null) {
            for (final var entrySet : variables.entrySet()) {
                final var value = Matcher.quoteReplacement(entrySet.getValue());
                final var regex = Pattern.quote("${%s}".formatted(entrySet.getKey()));
                returnValue = returnValue.replaceAll(regex, value);
            }
        }
        return returnValue;
    }

    private TemplateUtil() {
        throw new IllegalStateException("Utility classes can't be instantiated!");
    }

}
