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

import com.vaadin.flow.component.Component;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class TestUtil {

    public static List<Component> getComponentsByClassName(@NotNull final Component component, @NotNull final String className) {
        return component.getChildren()
                .filter(c -> c.getElement().getAttribute("class") != null && c.getElement().getAttribute("class").contains(className))
                .toList();
    }

    public static List<Component> getComponentsByTagName(@NotNull final Component component, @NotNull final String tagName) {
        return component.getChildren()
                .filter(c -> c.getElement().getTag().equals(tagName))
                .toList();
    }

    @NotNull
    public static String extractFirstHtmlTag(@NotNull final String html, @NotNull final String tagName) {
        final var document = Jsoup.parse(html);
        final var element = document.selectFirst(tagName);
        assertNotNull(element);
        return element.html();
    }

    private TestUtil() {
        throw new IllegalStateException("Utility classes can't be instantiated!");
    }

}
