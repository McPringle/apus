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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void positiveTests() {
        final String rawPassword = "password";

        // let's try it five times
        final String hashedPassword1 = PasswordUtil.hashPassword(rawPassword);
        final String hashedPassword2 = PasswordUtil.hashPassword(rawPassword);
        final String hashedPassword3 = PasswordUtil.hashPassword(rawPassword);
        final String hashedPassword4 = PasswordUtil.hashPassword(rawPassword);
        final String hashedPassword5 = PasswordUtil.hashPassword(rawPassword);

        // hashing the same password multiple times produces different hashes
        assertNotEquals(hashedPassword1, hashedPassword2);
        assertNotEquals(hashedPassword1, hashedPassword3);
        assertNotEquals(hashedPassword1, hashedPassword4);
        assertNotEquals(hashedPassword1, hashedPassword5);
        assertNotEquals(hashedPassword2, hashedPassword1);
        assertNotEquals(hashedPassword2, hashedPassword3);
        assertNotEquals(hashedPassword2, hashedPassword4);
        assertNotEquals(hashedPassword2, hashedPassword5);
        assertNotEquals(hashedPassword3, hashedPassword1);
        assertNotEquals(hashedPassword3, hashedPassword2);
        assertNotEquals(hashedPassword3, hashedPassword4);
        assertNotEquals(hashedPassword3, hashedPassword5);
        assertNotEquals(hashedPassword4, hashedPassword1);
        assertNotEquals(hashedPassword4, hashedPassword2);
        assertNotEquals(hashedPassword4, hashedPassword3);
        assertNotEquals(hashedPassword4, hashedPassword5);
        assertNotEquals(hashedPassword5, hashedPassword1);
        assertNotEquals(hashedPassword5, hashedPassword2);
        assertNotEquals(hashedPassword5, hashedPassword3);
        assertNotEquals(hashedPassword5, hashedPassword4);

        // every hash should match the password
        assertTrue(PasswordUtil.matches(rawPassword, hashedPassword1));
        assertTrue(PasswordUtil.matches(rawPassword, hashedPassword2));
        assertTrue(PasswordUtil.matches(rawPassword, hashedPassword3));
        assertTrue(PasswordUtil.matches(rawPassword, hashedPassword4));
        assertTrue(PasswordUtil.matches(rawPassword, hashedPassword5));
    }

}
