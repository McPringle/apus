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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TemplateUtilTest {

    @Test
    void replaceVariables() {
        final var template = "Hello, ${name}!";
        final var variables = Map.of("name", "World");
        final var expected = "Hello, World!";
        final var actual = TemplateUtil.replaceVariables(template, variables);
        assertEquals(expected, actual);
    }

    @Test
    void replaceEmpty() {
        final var template = "Hello, ${name}!";
        final var expected = "Hello, ${name}!";
        final var actual = TemplateUtil.replaceVariables(template, Map.of());
        assertEquals(expected, actual);
    }

    @Test
    void replaceWithNull() {
        final var template = "Hello, ${name}!";
        final var expected = "Hello, ${name}!";
        final var actual = TemplateUtil.replaceVariables(template, null);
        assertEquals(expected, actual);
    }

    @Test
    void expectExceptionCallingConstructor() throws Exception {
        final Constructor<TemplateUtil> constructor = TemplateUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        final var exception = assertThrows(InvocationTargetException.class, constructor::newInstance).getTargetException();
        assertEquals(IllegalStateException.class, exception.getClass());
        assertEquals("Utility classes can't be instantiated!", exception.getMessage());
    }

}
