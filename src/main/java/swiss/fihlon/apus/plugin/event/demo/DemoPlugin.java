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

import net.datafaker.Faker;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.Speaker;
import swiss.fihlon.apus.event.Track;
import swiss.fihlon.apus.plugin.event.EventPlugin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
public final class DemoPlugin implements EventPlugin {

    private static final Locale LOCALE = Locale.getDefault();
    private static final Random RANDOM = new Random();
    private static final int AROUND_THE_CLOCK = 24;

    private final int roomCount;
    private final List<Session> sessions;

    public DemoPlugin(@NotNull final Configuration configuration) {
        roomCount = configuration.getDemo().roomCount();
        sessions = roomCount > 0 ? createFakeSessions() : List.of();
    }

    @Override
    public boolean isEnabled() {
        return roomCount > 0;
    }

    @Override
    public @NotNull List<Session> getSessions() {
        return List.copyOf(sessions);
    }

    private @NotNull List<Session> createFakeSessions() {
        final List<Room> fakeRooms = createFakeRooms();
        final List<Session> fakeSessions = new ArrayList<>(AROUND_THE_CLOCK * roomCount);

        final var todayAtMidnight = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0));
        final Faker faker = new Faker(LOCALE, RANDOM);
        for (int hourCount = 0; hourCount < AROUND_THE_CLOCK; hourCount++) {
            final var startDateTime = todayAtMidnight.plusHours(hourCount);
            final var endDateTime = startDateTime.plusMinutes(50);
            for (int numberOfRoom = 0; numberOfRoom < roomCount; numberOfRoom++) {
                if (numberOfRoom > roomCount / 2 && hourCount % 2 == 0) {
                    continue;
                }
                final Room fakeRoom = fakeRooms.get(numberOfRoom);
                final Speaker fakeSpeaker = new Speaker(faker.name().fullName());
                final Session fakeSession = new Session(String.format("DEMO-%d-%d", hourCount, numberOfRoom),
                        startDateTime, endDateTime, fakeRoom, faker.lorem().sentence(8), List.of(fakeSpeaker),
                        getRandomLanguage(), getRandomTrack());
                fakeSessions.add(fakeSession);
            }
        }
        return fakeSessions;
    }

    private @NotNull List<Room> createFakeRooms() {
        final HashSet<Room> rooms = HashSet.newHashSet(roomCount);
        final Faker faker = new Faker(LOCALE, RANDOM);
        while (rooms.size() < roomCount) {
            rooms.add(new Room(faker.address().cityName()));
        }
        return List.copyOf(rooms);
    }

    private @NotNull Language getRandomLanguage() {
        final var values = Arrays.asList(Language.values());
        final int size = values.size();

        Language language = Language.UNKNOWN;
        while (language == Language.UNKNOWN) {
            language = values.get(RANDOM.nextInt(size));
        }

        return language;
    }

    private @NotNull Track getRandomTrack() {
        Track track = Track.NONE;
        while (track == Track.NONE) {
            final var values = Arrays.asList(Track.values());
            final int size = values.size();
            track = values.get(RANDOM.nextInt(size));
        }
        return track;
    }
}
