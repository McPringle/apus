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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HtmlUtilTest {

    @Test
    void sanitize() {
        assertEquals("<p>AB<strong>CD</strong></p>",
                HtmlUtil.sanitize("<p>A<span>B<strong>C<a href=\"http://example.com\">D</a></span></p>"));
        assertEquals("Test",
                HtmlUtil.sanitize("<a href=\"http://example.com\" onclick=\"alert('Test');\">Test</a>"));
        assertEquals("Test  Test",
                HtmlUtil.sanitize("Test <script>alert('Test');</script> Test"));
        assertEquals("Test  Test",
                HtmlUtil.sanitize("Test <style>body {background-color: black; }</style> Test"));
        assertEquals("<p>Test</p>",
                HtmlUtil.sanitize("<p style=\"color: red;\">Test</p>"));
        assertEquals("Test &gt;&gt;&gt;Test&lt;&lt;&lt;<p> Test</p>",
                HtmlUtil.sanitize("Test </p>>>>Test<<<<p> Test"));
    }

    @Test
    void extractText() {
        assertEquals("ABCD",
                HtmlUtil.extractText("<p>A<span>B<strong>C<a href=\"http://example.com\">D</a></span></p>"));
        assertEquals("Test",
                HtmlUtil.extractText("<a href=\"http://example.com\" onclick=\"alert('Test');\">Test</a>"));
        assertEquals("Test Test",
                HtmlUtil.extractText("Test <script>alert('Test');</script> Test"));
        assertEquals("Test Test",
                HtmlUtil.extractText("Test <style>body {background-color: black; }</style> Test"));
        assertEquals("Test",
                HtmlUtil.extractText("<p style=\"color: red;\">Test</p>"));
        assertEquals("Test >>>Test<<< Test",
                HtmlUtil.extractText("Test </p>>>>Test<<<<p> Test"));
    }

    @Test
    void expectExceptionCallingConstructor() throws Exception {
        final Constructor<HtmlUtil> constructor = HtmlUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        final var exception = assertThrows(InvocationTargetException.class, constructor::newInstance).getTargetException();
        assertEquals(IllegalStateException.class, exception.getClass());
        assertEquals("Utility classes can't be instantiated!", exception.getMessage());
    }

}
