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
import org.jsoup.Jsoup;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public final class HtmlUtil {

    private static final PolicyFactory POLICY_FACTORY = new HtmlPolicyBuilder()
            .allowElements("p", "br", "a", "b", "i", "u", "em", "strong", "mark", "code", "img")
            .allowUrlProtocols("https")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src").onElements("img")
            .toFactory();

    public static String sanitize(@NotNull final String html) {
        return POLICY_FACTORY.sanitize(html);
    }

    public static String extractText(@NotNull final String html) {
        return Jsoup.parse(html).text();
    }

    private HtmlUtil() {
        throw new IllegalStateException("Utility class");
    }

}
