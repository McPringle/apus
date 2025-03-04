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
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadUtilTest {

    @Test
    void getString() throws Exception {
        final String string = DownloadUtil.getString("file:src/test/resources/testdata/DOAG.json").trim();
        assertTrue(string.startsWith("{"));
        assertTrue(string.endsWith("}"));
    }

    @Test
    void expectExceptionCallingConstructor() throws Exception {
        final Constructor<DownloadUtil> constructor = DownloadUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        final var exception = assertThrows(InvocationTargetException.class, constructor::newInstance).getTargetException();
        assertEquals(IllegalStateException.class, exception.getClass());
        assertEquals("Utility classes can't be instantiated!", exception.getMessage());
    }

}
