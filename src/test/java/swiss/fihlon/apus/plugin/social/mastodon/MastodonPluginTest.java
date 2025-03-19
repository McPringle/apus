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
package swiss.fihlon.apus.plugin.social.mastodon;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import swiss.fihlon.apus.MemoryAppender;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.social.Post;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class MastodonPluginTest {

    @Autowired
    private AppConfig appConfig;

    @Test
    void getServiceName() {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("", "", "", "", 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final var mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        assertEquals("Mastodon", mastodonPlugin.getServiceName());
    }

    private static Stream<Arguments> provideDataForDisabledTest() {
        return Stream.of(
                Arguments.of("", "api"),
                Arguments.of(" ", "api"),
                Arguments.of("localhost", ""),
                Arguments.of("localhost", " ")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForDisabledTest")
    void isDisabled(@NotNull final String instance, @NotNull final String postApi) {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig(instance, "", "", postApi, 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final var mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        assertFalse(mastodonPlugin.isEnabled());
    }

    @Test
    void isEnabled() {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "", "", "foobar", 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final var mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        assertTrue(mastodonPlugin.isEnabled());
    }

    private static Stream<Arguments> provideDataForHashtagsTest() {
        return Stream.of(
                Arguments.of(List.of(""), 0),
                Arguments.of(List.of(" "), 0),
                Arguments.of(List.of("foobar"), 5),
                Arguments.of(List.of("foo"), 2),
                Arguments.of(List.of("bar"), 3),
                Arguments.of(List.of("foobar", "foo"), 7),
                Arguments.of(List.of("foobar", "bar"), 8),
                Arguments.of(List.of("foo", "bar"), 5),
                Arguments.of(List.of("foobar", "", "bar"), 8),
                Arguments.of(List.of("foobar", " ", "bar"), 8)
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForHashtagsTest")
    void getPostsWithHashtags(@NotNull final List<String> hashtags, final int expectedNumberOfPosts) {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "", "", appConfig.mastodon().postAPI(), 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        final List<Post> posts = mastodonPlugin.getPosts(hashtags).toList();

        assertNotNull(posts);
        assertEquals(expectedNumberOfPosts, posts.size());
    }

    @Test
    void getPostsWithUnlimitedImages() {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "", "", appConfig.mastodon().postAPI(), 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        final List<Post> posts = mastodonPlugin.getPosts(List.of("foobar")).toList();

        assertNotNull(posts);
        assertEquals(5, posts.size());

        final var firstPost = posts.getFirst();
        assertEquals("ID 1", firstPost.id());
        assertEquals("Display Name 1", firstPost.author());
        assertEquals("Avatar 1", firstPost.avatar());
        assertEquals("profile1@localhost", firstPost.profile());
        assertEquals("http://localhost/image1a.webp", firstPost.images().getFirst());
        assertEquals("http://localhost/image1b.webp", firstPost.images().get(1));

        for (final Post post : posts) {
            assertEquals(2, post.images().size());
        }
    }

    @Test
    void getPostsWithInvalidImageTypes() {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "", "", appConfig.mastodon().postAPI(), 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        final List<Post> posts = mastodonPlugin.getPosts(List.of("invalidImageType")).toList();

        assertNotNull(posts);
        assertEquals(5, posts.size());

        final var firstPost = posts.getFirst();
        assertEquals("http://localhost/image1b.webp", firstPost.images().getFirst());

        for (final Post post : posts) {
            assertEquals(1, post.images().size());
        }
    }

    @Test
    void getPostsCatchesException() {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "", "", appConfig.mastodon().postAPI(), 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        final Logger logger = (Logger) LoggerFactory.getLogger(MastodonPlugin.class);
        logger.addAppender(memoryAppender);

        memoryAppender.start();
        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        final var posts = mastodonPlugin.getPosts(List.of("broken")).toList();
        memoryAppender.stop();

        final int errorCount = memoryAppender.searchMessages("This is an expected exception getting posts.", Level.ERROR).size();
        assertEquals(1, errorCount);
        assertEquals(0, posts.size());
    }

    @Test
    void testReplyConversion() {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "", "", appConfig.mastodon().postAPI(), 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        final List<Post> posts = mastodonPlugin.getPosts(List.of("foobar")).toList();

        assertNotNull(posts);
        assertEquals(5, posts.size());

        for (int i = 0; i < posts.size() - 1; i++) {
            final Post post = posts.get(i);
            assertFalse(post.isReply(), "Post with " + post.id());
        }

        assertTrue(posts.getLast().isReply());
    }

    @Test
    void getNotifications() {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "testToken",
                appConfig.mastodon().notificationAPI(), appConfig.mastodon().postAPI(), 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        final List<Post> posts = mastodonPlugin.getPosts(List.of("empty")).toList();

        assertNotNull(posts);
        assertEquals(3, posts.size());
    }

    @Test
    void getNotificationsWithEmptyAPI() {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "testToken",
                "", appConfig.mastodon().postAPI(), 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        final List<Post> posts = mastodonPlugin.getPosts(List.of("empty")).toList();

        assertNotNull(posts);
        assertTrue(posts.isEmpty());
    }

    @Test
    void getNotificationsWithBlankAPI() {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "testToken",
                "   ", appConfig.mastodon().postAPI(), 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        final List<Post> posts = mastodonPlugin.getPosts(List.of("empty")).toList();

        assertNotNull(posts);
        assertTrue(posts.isEmpty());
    }

    @Test
    void getNotificationsWithEmptyAccessToken() {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "",
                appConfig.mastodon().notificationAPI(), appConfig.mastodon().postAPI(), 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        final List<Post> posts = mastodonPlugin.getPosts(List.of("empty")).toList();

        assertNotNull(posts);
        assertTrue(posts.isEmpty());
    }

    @Test
    void getNotificationsWithBlankAccessToken() {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "   ",
                appConfig.mastodon().notificationAPI(), appConfig.mastodon().postAPI(), 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        final List<Post> posts = mastodonPlugin.getPosts(List.of("empty")).toList();

        assertNotNull(posts);
        assertTrue(posts.isEmpty());
    }

    @Test
    void getNotificationsCatchesException() {
        final var mockAppConfig = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "broken",
                appConfig.mastodon().notificationAPI(), appConfig.mastodon().postAPI(), 0);
        when(mockAppConfig.mastodon()).thenReturn(mastodonConfig);

        final MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        final Logger logger = (Logger) LoggerFactory.getLogger(MastodonPlugin.class);
        logger.addAppender(memoryAppender);

        memoryAppender.start();
        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), mockAppConfig);
        final var posts = mastodonPlugin.getPosts(List.of("empty")).toList();
        memoryAppender.stop();

        final int errorCount = memoryAppender.searchMessages("This is an expected exception getting notifications.", Level.ERROR).size();
        assertEquals(1, errorCount);
        assertEquals(0, posts.size());
    }

    private static final class TestMastodonLoader implements MastodonLoader {

        @Override
        @NotNull
        public JSONArray getPosts(@NotNull final String instance,
                                  @NotNull final String hashtag,
                                  @NotNull final String postAPI,
                                  final int postLimit)
                throws MastodonException {
            final var posts = new JSONArray();
            posts.putAll(switch (hashtag) {
                case "foobar" -> List.of(
                        createPost(1, false),
                        createPost(2, false),
                        createPost(3, false),
                        createPost(4, false),
                        createPost(5, false)
                );
                case "foo" -> List.of(
                        createPost(6, false),
                        createPost(7, false)
                );
                case "bar" -> List.of(
                        createPost(8, false),
                        createPost(9, false),
                        createPost(10, false)
                );
                case "invalidImageType" -> List.of(
                        createPost(1, true),
                        createPost(2, true),
                        createPost(3, true),
                        createPost(4, true),
                        createPost(5, true)
                );
                case "broken" -> throw new MastodonException("This is an expected exception getting posts.",
                        new RuntimeException("This is a faked cause."));
                default -> List.of();
            });
            return posts;
        }

        @Override
        public @NotNull JSONArray getNotifications(@NotNull String instance, @NotNull String notificationAPI, @NotNull String accessToken, int postLimit)
                throws MastodonException {
            if (accessToken.equalsIgnoreCase("broken")) {
                throw new MastodonException("This is an expected exception getting notifications.",
                        new RuntimeException("This is a faked cause."));
            }
            final var notifications = new JSONArray();
            notifications.putAll(List.of(
                        createNotification(101),
                        createNotification(102),
                        createNotification(103),
                        createNotification(104),
                        createNotification(105)
                )
            );
            return notifications;
        }

        private JSONObject createNotification(final int index) {
            final var notification = new JSONObject();
            notification.put("status", createPost(index, false));
            return notification;
        }

        @SuppressWarnings("ZoneIdOfZ") // because that is what the JSON interface in production uses
        private JSONObject createPost(final int index, boolean invalidImageType) {
            final var post = new JSONObject();
            post.put("id", "ID " + index);
            post.put("in_reply_to_id", index == 1 ? null : index == 5 ? "ID 4" : " ");
            post.put("sensitive", false);
            post.put("content", "Content for post #" + index);
            if (index >= 100 && index % 2 == 0) {
                post.put("visibility", "private");
            } else {
                post.put("visibility", "public");
            }

            final var account = new JSONObject();
            account.put("display_name", "Display Name " + index);
            account.put("avatar", "Avatar " + index);
            account.put("acct", "profile" + index + "@localhost");
            post.put("account", account);

            final var createdAt = ZonedDateTime.ofInstant(Instant.now().minus(index, ChronoUnit.MINUTES), ZoneId.of("Z"));
            post.put("created_at", createdAt.format(DateTimeFormatter.ISO_INSTANT));

            final var mediaAttachments = getMediaAttachments(index, invalidImageType);
            post.put("media_attachments", mediaAttachments);

            return post;
        }

        private static @NotNull JSONArray getMediaAttachments(final int index, final boolean invalidImageType) {
            final var mediaAttachments = new JSONArray();
            final var mediaAttachmentA = new JSONObject();
            mediaAttachmentA.put("type", invalidImageType ? "video" : "image");
            mediaAttachmentA.put("preview_url", "http://localhost/image" + index + "a.webp");
            mediaAttachments.put(mediaAttachmentA);
            final var mediaAttachmentB = new JSONObject();
            mediaAttachmentB.put("type", "image");
            mediaAttachmentB.put("preview_url", "http://localhost/image" + index + "b.webp");
            mediaAttachments.put(mediaAttachmentB);
            return mediaAttachments;
        }
    }
}
