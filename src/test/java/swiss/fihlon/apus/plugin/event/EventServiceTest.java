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
package swiss.fihlon.apus.plugin.event;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.support.NoOpTaskScheduler;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.plugin.event.doag.DoagConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private Configuration configuration;

    @Test
    @Disabled // TODO inject test data instead of relying on an external API
    void displaySampleData() {

        when(configuration.getDoag()).thenReturn(new DoagConfig(773, "file:src/test/resources/DOAG.json?eventId=%d"));

        final EventService eventService = new EventService(new NoOpTaskScheduler(), configuration);
        assertEquals(12, eventService.getRoomsWithSessions().size());
    }
}
