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
package swiss.fihlon.apus.plugin.social.demo;

import net.datafaker.Faker;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.plugin.social.SocialPlugin;
import swiss.fihlon.apus.social.Post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Stream;

@Service
public final class SocialDemoPlugin implements SocialPlugin {

    private static final Locale LOCALE = Locale.getDefault();
    private static final Random RANDOM = new Random();

    private final int postCount;

    public SocialDemoPlugin(@NotNull final AppConfig appConfig) {
        this.postCount = appConfig.social().demoPostCount();
    }

    @Override
    @NotNull
    public String getServiceName() {
        return "Demo";
    }

    @Override
    public boolean isEnabled() {
        return postCount > 0;
    }

    @Override
    @NotNull
    public Stream<Post> getPosts(@NotNull final List<String> hashtags) {
        final Faker faker = new Faker(LOCALE, RANDOM);
        final var posts = new ArrayList<Post>();
        for (int number = 1; number <= postCount; number++) {
            posts.add(
                    new Post(generateId(number),
                            getRandomDateTime(),
                            getRandomAuthor(faker),
                            getRandomAvatar(faker),
                            getRandomProfile(faker),
                            getRandomHtml(faker),
                            getRandomImage(faker),
                            false,
                            false)
            );
        }
        return posts.stream();
    }

    private @NotNull List<String> getRandomImage(@NotNull final Faker faker) {
        return List.of(faker.image().base64SVG());
    }

    private @NotNull String getRandomHtml(@NotNull final Faker faker) {
        return faker.lorem().sentence(10);
    }

    private @NotNull String getRandomProfile(@NotNull final Faker faker) {
        return faker.internet().emailAddress();
    }

    private @NotNull String getRandomAvatar(@NotNull final Faker faker) {
        return faker.avatar().image();
    }

    private static @NotNull String getRandomAuthor(@NotNull final Faker faker) {
        return faker.name().fullName();
    }

    private static @NotNull LocalDateTime getRandomDateTime() {
        return LocalDateTime.now().minusMinutes(RANDOM.nextLong(10_000));
    }

    private static @NotNull String generateId(final int number) {
        return String.format("SOCIAL-DEMO-ID-%d", number);
    }
}
