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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonUtilTest {

    private static Stream<Arguments> provideDataForTest() {
        return Stream.of(
                Arguments.of("bar", new JSONObject().put("foo", "bar"), "foo", "foo"),
                Arguments.of("foo", new JSONObject().put("foo", "   "), "foo", "foo"),
                Arguments.of("foo", new JSONObject().put("foo", " "), "foo", "foo"),
                Arguments.of("foo", new JSONObject().put("foo", (String) null), "foo", "foo"),
                Arguments.of("foo", new JSONObject(), "foo", "foo")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForTest")
    void getStringOrDefault(final @NotNull String expected,
                            final @NotNull JSONObject jsonObject,
                            final @NotNull String key,
                            final @NotNull String defaultValue) {
        assertEquals(expected, JsonUtil.getStringOrDefault(jsonObject, key, defaultValue));
    }

    @Test
    void expectExceptionCallingConstructor() throws Exception {
        final Constructor<JsonUtil> constructor = JsonUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        final var exception = assertThrows(InvocationTargetException.class, constructor::newInstance).getTargetException();
        assertEquals(IllegalStateException.class, exception.getClass());
        assertEquals("Utility classes can't be instantiated!", exception.getMessage());
    }

}
