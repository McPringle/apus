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
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.event.Language;
import swiss.fihlon.apus.event.Room;
import swiss.fihlon.apus.event.Session;
import swiss.fihlon.apus.event.Speaker;
import swiss.fihlon.apus.event.Track;
import swiss.fihlon.apus.plugin.event.EventPlugin;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Stream;

@Service
public final class EventDemoPlugin implements EventPlugin {

    private static final @NotNull Random RANDOM = new Random();
    private static final int AROUND_THE_CLOCK = 24;
    private static final int ROOM_COUNT = 4;
    private static final @NotNull List<@NotNull Track> DEFAULT_TRACKS =
            List.of(Track.ARCHITECTURE, Track.CLOUD, Track.CORE, Track.INFRASTRUCTURE, Track.SECURITY, Track.TOOLS);

    private final @NotNull Locale locale;
    private final @NotNull ZoneId timezone;
    private final boolean demoMode;
    private final @NotNull List<@NotNull Session> sessions;

    public EventDemoPlugin(@NotNull final AppConfig appConfig) {
        locale = appConfig.locale();
        timezone = appConfig.timezone();
        demoMode = appConfig.demoMode();
        sessions = demoMode ? createFakeSessions() : List.of();
    }

    @Override
    public boolean isEnabled() {
        return demoMode;
    }

    @Override
    public @NotNull Stream<@NotNull Session> getSessions() {
        return sessions.stream();
    }

    private @NotNull List<@NotNull Session> createFakeSessions() {
        final List<Room> fakeRooms = createFakeRooms();
        final List<Session> fakeSessions = new ArrayList<>(AROUND_THE_CLOCK * ROOM_COUNT);

        final var todayAtMidnight = ZonedDateTime.now(timezone).withHour(0).withMinute(0).withSecond(0).withNano(0);
        final Faker faker = new Faker(locale, RANDOM);
        for (int hourCount = 0; hourCount < AROUND_THE_CLOCK; hourCount++) {
            final var startDateTime = todayAtMidnight.plusHours(hourCount);
            final var endDateTime = startDateTime.plusMinutes(50);
            for (int numberOfRoom = 0; numberOfRoom < ROOM_COUNT; numberOfRoom++) {
                final Room fakeRoom = fakeRooms.get(numberOfRoom);
                final Speaker fakeSpeaker = new Speaker(faker.name().fullName(), faker.avatar().image());
                final Session fakeSession = new Session(String.format("DEMO-%d-%d", hourCount, numberOfRoom),
                        startDateTime, endDateTime, fakeRoom, faker.lorem().sentence(5), List.of(fakeSpeaker),
                        getRandomLanguage(), getRandomTrack());
                fakeSessions.add(fakeSession);
            }
        }
        return fakeSessions;
    }

    private @NotNull List<@NotNull Room> createFakeRooms() {
        final HashSet<Room> rooms = HashSet.newHashSet(ROOM_COUNT);
        final Faker faker = new Faker(locale, RANDOM);
        while (rooms.size() < ROOM_COUNT) {
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
        return DEFAULT_TRACKS.get(RANDOM.nextInt(DEFAULT_TRACKS.size()));
    }
}
