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
package swiss.fihlon.apus.plugin.social;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import net.datafaker.Faker;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.support.NoOpTaskScheduler;
import swiss.fihlon.apus.MemoryAppender;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.configuration.SocialConfig;
import swiss.fihlon.apus.social.Post;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class SocialServiceTest {

    private static Path getConfigDir() {
        return Path.of(System.getProperty("user.home"), ".apus");
    }

    @BeforeEach
    @AfterEach
    void cleanUp() {
        for (String fileName : List.of("hiddenPosts", "blockedProfiles")) {
            final var file = getConfigDir().resolve(fileName).toFile();
            if (file.exists() && !file.delete()) {
                fail("Could not delete " + file);
            }
        }
    }

    @Autowired
    private AppConfig appConfig;

    @Test
    void getPostsWithoutLimit() {
        final SocialService socialService = new SocialService(new NoOpTaskScheduler(), appConfig, List.of(new TestSocialPlugin()));
        final List<Post> posts = socialService.getPosts(0);
        assertEquals(10, posts.size());
    }

    @Test
    void getPostsWithNegativeLimit() {
        final SocialService socialService = new SocialService(new NoOpTaskScheduler(), appConfig, List.of(new TestSocialPlugin()));
        final List<Post> posts = socialService.getPosts(-1);
        assertEquals(10, posts.size());
    }

    @Test
    void getPostsWithLimit() {
        final SocialService socialService = new SocialService(new NoOpTaskScheduler(), appConfig, List.of(new TestSocialPlugin()));
        final List<Post> posts = socialService.getPosts(5);
        assertEquals(5, posts.size());
        assertEquals("P1", posts.get(0).id());
        assertEquals("P2", posts.get(1).id());
        assertEquals("P3", posts.get(2).id());
        assertEquals("P4", posts.get(3).id());
        assertEquals("P5", posts.get(4).id());
    }

    @Test
    void getEmptyPosts() {
        final SocialService socialService = new SocialService(new NoOpTaskScheduler(), appConfig, List.of(new EmptySocialPlugin()));
        final List<Post> posts = socialService.getPosts(10);
        assertTrue(posts.isEmpty());
    }

    @Test
    void hidePost() {
        final SocialService socialService = new SocialService(new NoOpTaskScheduler(), appConfig, List.of(new TestSocialPlugin()));
        final List<Post> postsBefore = socialService.getPosts(10);
        assertEquals(10, postsBefore.size());

        socialService.hidePost(postsBefore.get(3));
        socialService.hidePost(postsBefore.get(7));
        final List<Post> postsAfter = socialService.getPosts(10);
        assertEquals(8, postsAfter.size());
    }

    @Test
    void blockProfile() {
        final SocialService socialService = new SocialService(new NoOpTaskScheduler(), appConfig, List.of(new TestSocialPlugin()));
        final List<Post> postsBefore = socialService.getPosts(10);
        assertEquals(10, postsBefore.size());

        socialService.blockProfile(postsBefore.get(5));
        final List<Post> postsAfter = socialService.getPosts(10);
        assertEquals(5, postsAfter.size());
    }

    @Test
    void loadHiddenPosts() throws IOException {
        final var filePath = getConfigDir().resolve("hiddenPosts");
        Files.writeString(filePath, "P5\nP6");

        final SocialService socialService = new SocialService(new NoOpTaskScheduler(), appConfig, List.of(new TestSocialPlugin()));
        final List<Post> posts = socialService.getPosts(0);
        final List<String> ids = posts.stream().map(Post::id).distinct().toList();
        assertFalse(ids.contains("P5"));
        assertFalse(ids.contains("P6"));
    }

    @Test
    void loadBlockedProfiles() throws IOException {
        final var filePath = getConfigDir().resolve("blockedProfiles");
        Files.writeString(filePath, "profile1@localhost");

        final SocialService socialService = new SocialService(new NoOpTaskScheduler(), appConfig, List.of(new TestSocialPlugin()));
        final List<Post> posts = socialService.getPosts(0);
        final List<String> profiles = posts.stream().map(Post::profile).distinct().toList();
        assertFalse(profiles.contains("profile1@localhost"));
    }

    @Test
    void getErrorWithoutPlugin() {
        final MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        final Logger logger = (Logger) LoggerFactory.getLogger(SocialService.class);
        logger.addAppender(memoryAppender);

        memoryAppender.start();
        final SocialService socialService = new SocialService(new NoOpTaskScheduler(), appConfig, List.of());
        final List<Post> posts = socialService.getPosts(0);
        assertEquals(0, posts.size());
        memoryAppender.stop();

        final int errorCount = memoryAppender.searchMessages("No social plugin is enabled. No posts will be displayed.", Level.WARN).size();
        assertEquals(1, errorCount);
    }

    private AppConfig createModifiedImageConfig(final boolean imagesEnabled, final int imageLimit) {
        final var newSocialConfig = new SocialConfig(appConfig.social().hashtags(), appConfig.social().headline(),
                appConfig.social().numberOfColumns(), imagesEnabled, imageLimit);
        return new AppConfig(appConfig.version(), appConfig.language(), appConfig.styles(), appConfig.demoMode(),
                appConfig.admin(), appConfig.event(), newSocialConfig, appConfig.filter(),
                appConfig.devoxx(), appConfig.doag(), appConfig.jfs(), appConfig.sessionize(),
                appConfig.blueSky(), appConfig.mastodon());
    }

    @Test
    void getPostsWithImagesDisabled() {
        final var testConfig = createModifiedImageConfig(false, 0);
        final SocialService socialService = new SocialService(new NoOpTaskScheduler(), testConfig, List.of(new TestSocialPlugin()));
        final List<Post> posts = socialService.getPosts(0);
        for (final var post : posts) {
            assertTrue(post.images().isEmpty());
        }
    }

    @Test
    void getPostsWithMaxOneImage() {
        final var testConfig = createModifiedImageConfig(true, 1);
        final SocialService socialService = new SocialService(new NoOpTaskScheduler(), testConfig, List.of(new TestSocialPlugin()));
        final List<Post> posts = socialService.getPosts(0);
        for (final var post : posts) {
            assertEquals(1, post.images().size());
        }
    }

    @Test
    void getPostsWithUnlimitedImages() {
        final var testConfig = createModifiedImageConfig(true, 0);
        final SocialService socialService = new SocialService(new NoOpTaskScheduler(), testConfig, List.of(new TestSocialPlugin()));
        final List<Post> posts = socialService.getPosts(0);
        for (final var post : posts) {
            final var id = post.id();
            final var number = Integer.parseInt(id.substring(1));
            assertEquals(number, post.images().size());
        }
    }

    private static final class TestSocialPlugin implements SocialPlugin {

        @Override
        @NotNull
        public String getServiceName() {
            return "Test";
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        @NotNull
        public Stream<Post> getPosts(@NotNull final List<String> hashtags) {
            final Faker faker = new Faker();
            final var now = ZonedDateTime.now();
            final List<Post> posts = new ArrayList<>();
            for (int i = 10; i > 0; i--) {
                final var images = new ArrayList<String>();
                for (int j = 0; j < i; j++) {
                    images.add(faker.file().fileName());
                }
                posts.add(new Post("P" + i, now.minusHours(i), "Author " + (i % 2), "Avatar " + i,
                        "profile" + (i % 2) + "@localhost","<p>Content of post #1</p>",
                        images, false, false, ""));
            }
            posts.add(new Post("PX", now, "Troll", "","troll@localhost",
                    "<p>This post is foobar!</p>", List.of(), false, false, ""));
            posts.add(new Post("PL", now, "Writer", "","writer@localhost",
                    "<p>" + faker.lorem().characters(501) + "</p>", List.of(),
                    false, false, ""));
            Collections.shuffle(posts);
            return posts.stream();
        }
    }

    private static final class EmptySocialPlugin implements SocialPlugin {

        @Override
        @NotNull
        public String getServiceName() {
            return "Empty";
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        @NotNull
        public Stream<Post> getPosts(@NotNull final List<String> hashtags) {
            return Stream.<Post>of();
        }
    }
}
