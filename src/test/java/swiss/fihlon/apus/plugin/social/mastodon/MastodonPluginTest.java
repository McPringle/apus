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
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
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

class MastodonPluginTest {

    private static final String POST_API = "https://%s/api/v1/timelines/tag/%s?limit=%d";
    private static final int POST_LIMIT = 30;

    private static Stream<Arguments> provideDataForDisabledTest() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(null, ""),
                Arguments.of("", null),
                Arguments.of("", ""),
                Arguments.of(" ", null),
                Arguments.of(null, " "),
                Arguments.of(" ", " "),
                Arguments.of(null, "foobar"),
                Arguments.of("", "foobar"),
                Arguments.of(" ", "foobar"),
                Arguments.of("localhost", null),
                Arguments.of("localhost", ""),
                Arguments.of("localhost", " ")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForDisabledTest")
    void isDisabled(@Nullable final String instance, @Nullable final String hashtag) {
        final var configuration = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig(instance, hashtag, POST_API, POST_LIMIT, true, 0);
        when(configuration.mastodon()).thenReturn(mastodonConfig);

        final var mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        assertFalse(mastodonPlugin.isEnabled());
    }

    @Test
    void isEnabled() {
        final var configuration = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "foobar", POST_API, POST_LIMIT, true, 0);
        when(configuration.mastodon()).thenReturn(mastodonConfig);

        final var mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        assertTrue(mastodonPlugin.isEnabled());
    }

    private static Stream<Arguments> provideDataForHashtagsTest() {
        return Stream.of(
                Arguments.of("", 0),
                Arguments.of(" ", 0),
                Arguments.of("foobar", 5),
                Arguments.of("foo", 2),
                Arguments.of("bar", 3),
                Arguments.of("foobar,foo", 7),
                Arguments.of("foobar,bar", 8),
                Arguments.of("foo,bar", 5),
                Arguments.of("foobar,,bar", 8),
                Arguments.of("foobar, ,bar", 8)
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForHashtagsTest")
    void getPostsWithHashtags(@NotNull final String hashtags, final int expectedNumberOfPosts) {
        final var configuration = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", hashtags, POST_API, POST_LIMIT, true, 0);
        when(configuration.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        final List<Post> posts = mastodonPlugin.getPosts().toList();

        assertNotNull(posts);
        assertEquals(expectedNumberOfPosts, posts.size());
    }

    @Test
    void getPostsWithUnlimitedImages() {
        final var configuration = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "foobar", POST_API, POST_LIMIT, true, 0);
        when(configuration.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        final List<Post> posts = mastodonPlugin.getPosts().toList();

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
    void getPostsWithOneImage() {
        final var configuration = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "foobar", POST_API, POST_LIMIT, true, 1);
        when(configuration.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        final List<Post> posts = mastodonPlugin.getPosts().toList();

        assertNotNull(posts);
        assertEquals(5, posts.size());

        final var firstPost = posts.getFirst();
        assertEquals("http://localhost/image1a.webp", firstPost.images().getFirst());

        for (final Post post : posts) {
            assertEquals(1, post.images().size());
        }
    }

    @Test
    void getPostsWithoutImages() {
        final var configuration = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "foobar", POST_API, POST_LIMIT, false, 0);
        when(configuration.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        final List<Post> posts = mastodonPlugin.getPosts().toList();

        assertNotNull(posts);
        assertEquals(5, posts.size());

        for (final Post post : posts) {
            assertEquals(0, post.images().size());
        }
    }

    @Test
    void getPostsWithInvalidImageTypes() {
        final var configuration = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "invalidImageType", POST_API, POST_LIMIT, true, 0);
        when(configuration.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        final List<Post> posts = mastodonPlugin.getPosts().toList();

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
        final var configuration = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "broken", POST_API, POST_LIMIT, true, 0);
        when(configuration.mastodon()).thenReturn(mastodonConfig);

        final MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        final Logger logger = (Logger) LoggerFactory.getLogger(MastodonPlugin.class);
        logger.addAppender(memoryAppender);

        memoryAppender.start();
        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        mastodonPlugin.getPosts();
        memoryAppender.stop();

        final int errorCount = memoryAppender.searchMessages("This is an expected exception.", Level.ERROR).size();
        assertEquals(1, errorCount);
    }

    @Test
    void testReplyConversion() {
        final var configuration = mock(AppConfig.class);
        final var mastodonConfig = new MastodonConfig("localhost", "foobar", POST_API, POST_LIMIT, true, 1);
        when(configuration.mastodon()).thenReturn(mastodonConfig);

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        final List<Post> posts = mastodonPlugin.getPosts().toList();

        assertNotNull(posts);
        assertEquals(5, posts.size());

        for (int i = 0; i < posts.size() - 1; i++) {
            final Post post = posts.get(i);
            assertFalse(post.isReply(), "Post with " + post.id());
        }

        assertTrue(posts.getLast().isReply());
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
                case "broken" -> throw new MastodonException("This is an expected exception.", new RuntimeException("This is a faked cause."));
                default -> List.of();
            });
            return posts;
        }

        private JSONObject createPost(final int i, boolean invalidImageType) {
            final var post = new JSONObject();
            post.put("id", "ID " + i);
            post.put("in_reply_to_id", i == 1 ? null : i == 5 ? "ID 4" : " ");
            post.put("sensitive", false);
            post.put("content", "Content for post #" + i);

            final var account = new JSONObject();
            account.put("display_name", "Display Name " + i);
            account.put("avatar", "Avatar " + i);
            account.put("acct", "profile" + i + "@localhost");
            post.put("account", account);

            final var createdAt = ZonedDateTime.ofInstant(Instant.now().minus(i, ChronoUnit.MINUTES), ZoneId.of("Z"));
            post.put("created_at", createdAt.format(DateTimeFormatter.ISO_INSTANT));

            final var mediaAttachments = new JSONArray();
            final var mediaAttachmentA = new JSONObject();
            mediaAttachmentA.put("type", invalidImageType ? "video" : "image");
            mediaAttachmentA.put("preview_url", "http://localhost/image" + i + "a.webp");
            mediaAttachments.put(mediaAttachmentA);
            final var mediaAttachmentB = new JSONObject();
            mediaAttachmentB.put("type", "image");
            mediaAttachmentB.put("preview_url", "http://localhost/image" + i + "b.webp");
            mediaAttachments.put(mediaAttachmentB);
            post.put("media_attachments", mediaAttachments);

            return post;
        }
    }
}
