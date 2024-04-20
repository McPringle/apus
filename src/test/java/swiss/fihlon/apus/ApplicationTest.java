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
package swiss.fihlon.apus;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void testShortPasswordOption() throws ParseException {
        final String[] args = new String[] { "-p", "12345" };
        Application.main(args);

        final String out = outputStreamCaptor.toString();
        assertTrue(out.contains("Hashed password for environment variable:"));
        assertTrue(out.contains("Hashed password for Docker Compose file:"));
    }

    @Test
    void testLongPasswordOption() throws ParseException {
        final String[] args = new String[] { "--password", "12345" };
        Application.main(args);

        final String out = outputStreamCaptor.toString();
        assertTrue(out.contains("Hashed password for environment variable:"));
        assertTrue(out.contains("Hashed password for Docker Compose file:"));
    }

    @Test
    void testShortHelpOption() throws ParseException {
        final String[] args = new String[] { "-h" };
        Application.main(args);

        final String out = outputStreamCaptor.toString();
        assertTrue(out.startsWith("usage:"));
        assertTrue(out.contains("Show help and exit"));
        assertTrue(out.contains("Hash password and exit"));
    }

    @Test
    void testLongHelpOption() throws ParseException {
        final String[] args = new String[] { "--help" };
        Application.main(args);

        final String out = outputStreamCaptor.toString();
        assertTrue(out.startsWith("usage:"));
        assertTrue(out.contains("Show help and exit"));
        assertTrue(out.contains("Hash password and exit"));
    }

}
