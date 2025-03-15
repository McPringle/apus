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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Command;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VaadinUtilTest {

    @Test
    void testUpdateUI() throws InterruptedException {
        // Arrange
        final var mockUI = mock(UI.class);
        final var mockCommand = mock(Command.class);
        final var latch = new CountDownLatch(1);

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockUI).access(any(Command.class));

        VaadinUtil.updateUI(mockUI, mockCommand);

        if (latch.await(5, TimeUnit.SECONDS)) {
            verify(mockUI, Mockito.times(1)).access(any(Command.class));
        } else {
            fail("The UI access method was not called within the timeout.");
        }
    }

    @Test
    void expectExceptionCallingConstructor() throws Exception {
        final Constructor<VaadinUtil> constructor = VaadinUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        final var exception = assertThrows(InvocationTargetException.class, constructor::newInstance).getTargetException();
        assertEquals(IllegalStateException.class, exception.getClass());
        assertEquals("Utility classes can't be instantiated!", exception.getMessage());
    }

}
