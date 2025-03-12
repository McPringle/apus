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
package swiss.fihlon.apus.plugin.event.demo;

import org.junit.jupiter.api.Test;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.event.Session;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventDemoPluginTest {

    @Test
    void isEnabled() {
        final var appConfig = mock(AppConfig.class);
        when(appConfig.demoMode()).thenReturn(true);
        when(appConfig.timezone()).thenReturn(ZoneId.of("Europe/Zurich"));

        final var demoEventPlugin = new EventDemoPlugin(appConfig);
        assertTrue(demoEventPlugin.isEnabled());
    }

    @Test
    void isDisabled() {
        final var appConfig = mock(AppConfig.class);
        when(appConfig.demoMode()).thenReturn(false);

        final var demoEventPlugin = new EventDemoPlugin(appConfig);
        assertFalse(demoEventPlugin.isEnabled());
    }

    /**
     * The plugin creates fake data for four rooms. Each room has 24 sessions.
     */
    @Test
    void getSessions() {
        final var appConfig = mock(AppConfig.class);
        when(appConfig.demoMode()).thenReturn(true);
        when(appConfig.timezone()).thenReturn(ZoneId.of("Europe/Zurich"));

        final var demoEventPlugin = new EventDemoPlugin(appConfig);
        final var sessions = demoEventPlugin.getSessions().toList();
        assertEquals(96, sessions.size());
        assertEquals(96, sessions.stream().map(Session::id).distinct().count());
        assertEquals(96, sessions.stream().flatMap(session -> session.speakers().stream()).distinct().count());
        assertEquals(4, sessions.stream().map(Session::room).distinct().count());
    }
}
