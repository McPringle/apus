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

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventConfigTest {

    private EventConfig bind(final Map<String, String> properties) {
        final var values = new HashMap<>(properties);
        values.put("apus.event.update-frequency", "5");
        return new Binder(new MapConfigurationPropertySource(values))
                .bind("apus.event", Bindable.of(EventConfig.class)).get();
    }

    @Test
    void bindsAndNormalizesCommaSeparatedRooms() {
        final var config = bind(Map.of("apus.event.excluded-rooms", " Saal Alpha, ,Workshopraum Beta, "));
        assertEquals(List.of("Saal Alpha", "Workshopraum Beta"), config.excludedRooms());
        assertThrows(UnsupportedOperationException.class, () -> config.excludedRooms().add("Room"));
    }

    @Test
    void missingRoomsDisableFiltering() {
        assertEquals(List.of(), bind(Map.of()).excludedRooms());
    }

    @Test
    void emptyRoomsDisableFiltering() {
        assertEquals(List.of(), bind(Map.of("apus.event.excluded-rooms", "")).excludedRooms());
        assertEquals(List.of(), bind(Map.of("apus.event.excluded-rooms", " , , ")).excludedRooms());
    }
}
