package swiss.fihlon.apus.plugin.social.demo;

import net.datafaker.Faker;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.plugin.social.SocialPlugin;
import swiss.fihlon.apus.social.Post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class SocialDemoPlugin implements SocialPlugin {

    private static final Locale LOCALE = Locale.getDefault();
    private static final Random RANDOM = new Random();

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Stream<Post> getPosts() {
        final Faker faker = new Faker(LOCALE, RANDOM);
        final var posts = new ArrayList<Post>();
        for(int i = 0;i<30;i++){
            posts.add(
                    new Post(generateId(),
                            getRandomDateTime(),
                            getRandomAutor(faker),
                            getRandomAvatar(faker),
                            getRandomProfile(faker),
                            getRandomHtml(faker),
                            getRandomImage(faker),
                            false,
                            false )
            );
        }
        return posts.stream();
    }

    private @NotNull List<String> getRandomImage(Faker faker) {
        return List.of(faker.image().base64SVG());
    }

    private @NotNull String getRandomHtml(Faker faker) {
        return faker.lorem().sentence(10);
    }

    private @NotNull String getRandomProfile(Faker faker) {
        return faker.internet().emailAddress();
    }

    private @NotNull String getRandomAvatar(Faker faker) {
        return faker.avatar().image();
    }

    private static @NotNull String getRandomAutor(Faker faker) {
        return faker.name().fullName();
    }

    private static @NotNull LocalDateTime getRandomDateTime() {
        return LocalDateTime.now().minusMinutes(RANDOM.nextLong(10_000));
    }

    private static @NotNull String generateId() {
        return UUID.randomUUID().toString();
    }
}
