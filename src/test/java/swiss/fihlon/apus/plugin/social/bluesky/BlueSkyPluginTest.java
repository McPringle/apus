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
package swiss.fihlon.apus.plugin.social.bluesky;

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
import swiss.fihlon.apus.MemoryAppender;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.social.Post;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlueSkyPluginTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private static final ZoneId TEST_TIMEZONE = ZoneId.of("Europe/Zurich");

    @Test
    void getServiceName() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "", "", "", 0);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final var blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), appConfig);
        assertEquals("BlueSky", blueSkyPlugin.getServiceName());
    }

    private static Stream<Arguments> provideDataForDisabledTest() {
        return Stream.of(
                Arguments.of("", ""),
                Arguments.of(" ", " "),
                Arguments.of("localhost", ""),
                Arguments.of("localhost", " ")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForDisabledTest")
    void isDisabled(final @NotNull String instance, final @NotNull String hashtagUrl) {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig(instance, hashtagUrl, "", "", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final var blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), appConfig);
        assertFalse(blueSkyPlugin.isEnabled());
    }

    @Test
    void isEnabled() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "test", "", "", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final var blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), appConfig);
        assertTrue(blueSkyPlugin.isEnabled());
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
    void getPostsWithHashtags(final @NotNull List<String> hashtags, final int expectedNumberOfPosts) {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", "", "", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), appConfig);
        final List<Post> posts = blueSkyPlugin.getPosts(hashtags).toList();

        assertNotNull(posts);
        assertEquals(expectedNumberOfPosts, posts.size());
    }

    @Test
    void getPostsWithUnlimitedImages() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", "", "", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), appConfig);
        final List<Post> posts = blueSkyPlugin.getPosts(List.of("foobar")).toList();

        assertNotNull(posts);
        assertEquals(5, posts.size());

        final var firstPost = posts.getFirst();
        assertEquals("ID 1", firstPost.id());
        assertEquals("Display Name 1", firstPost.author());
        assertEquals("Avatar 1", firstPost.avatar());
        assertEquals("profile1", firstPost.profile());
        assertEquals("http://localhost/image1a.webp", firstPost.images().getFirst());
        assertEquals("http://localhost/image1b.webp", firstPost.images().get(1));

        for (final Post post : posts) {
            assertEquals(2, post.images().size());
        }
    }

    @Test
    void getPostsWithVideos() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", "", "", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), appConfig);
        final List<Post> posts = blueSkyPlugin.getPosts(List.of("videos")).toList();

        assertNotNull(posts);
        assertEquals(3, posts.size());

        final var firstPost = posts.getFirst();
        assertEquals("ID 11", firstPost.id());
        assertEquals("Display Name 11", firstPost.author());
        assertEquals("Avatar 11", firstPost.avatar());
        assertEquals("profile11", firstPost.profile());

        for (final Post post : posts) {
            assertTrue(post.images().isEmpty());
        }
    }

    @Test
    void getPostsCatchesException() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", "", "", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        final Logger logger = (Logger) LoggerFactory.getLogger(BlueSkyPlugin.class);
        logger.addAppender(memoryAppender);

        memoryAppender.start();
        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), appConfig);
        final var posts = blueSkyPlugin.getPosts(List.of("broken")).toList();
        memoryAppender.stop();

        final int errorCount = memoryAppender.searchMessages("This is an expected exception.", Level.ERROR).size();
        assertEquals(1, errorCount);
        assertEquals(0, posts.size());
    }

    @Test
    void testReplyConversion() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", "", "", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), appConfig);
        final List<Post> posts = blueSkyPlugin.getPosts(List.of("foobar")).toList();

        assertNotNull(posts);
        assertEquals(5, posts.size());

        for (int i = 0; i < posts.size() - 1; i++) {
            final Post post = posts.get(i);
            assertFalse(post.isReply(), "Post with " + post.id());
        }

        assertTrue(posts.getLast().isReply());
    }

    @Test
    void testMentions() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "", "https://%s/q=%s&limit=%d",
                "foobar.bsky.social", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), appConfig);
        final List<Post> posts = blueSkyPlugin.getPosts(List.of("empty")).toList();

        assertNotNull(posts);
        assertEquals(5, posts.size());
        assertEquals(5, posts.stream()
                .map(Post::html)
                .filter(html -> html.contains("which mentions @foobar.bsky.social"))
                .count());
    }

    @Test
    void testMentionsNoUrl() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "", "", "foobar.bsky.social", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), appConfig);
        final List<Post> posts = blueSkyPlugin.getPosts(List.of("empty")).toList();

        assertNotNull(posts);
        assertEquals(0, posts.size());
    }

    @Test
    void testMentionsNoProfile() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "", "https://%s/q=%s&limit=%d", "", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), appConfig);
        final List<Post> posts = blueSkyPlugin.getPosts(List.of("empty")).toList();

        assertNotNull(posts);
        assertEquals(0, posts.size());
    }

    @Test
    void getMentionsCatchesException() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "", "https://%s/q=%s&limit=%d", "broken", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        final Logger logger = (Logger) LoggerFactory.getLogger(BlueSkyPlugin.class);
        logger.addAppender(memoryAppender);

        memoryAppender.start();
        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), appConfig);
        final var posts = blueSkyPlugin.getPosts(List.of("foobar")).toList();
        memoryAppender.stop();

        final int errorCount = memoryAppender.searchMessages("This is an expected exception.", Level.ERROR).size();
        assertEquals(1, errorCount);
        assertEquals(5, posts.size());
    }

    private static final class TestBlueSkyLoader implements BlueSkyLoader {

        @Override
        @NotNull
        public JSONArray getPostsWithHashtag(final @NotNull String instance,
                                             final @NotNull String hashtag,
                                             final @NotNull String postAPI,
                                             final int postLimit)
                throws BlueSkyException {
            return switch (hashtag) {
                case "foobar" -> new JSONArray(List.of(
                        createPost(1, hashtag, false),
                        createPost(2, hashtag, false),
                        createPost(3, hashtag, false),
                        createPost(4, hashtag, false),
                        createPost(5, hashtag, false)
                ));
                case "foo" -> new JSONArray(List.of(
                        createPost(6, hashtag, false),
                        createPost(7, hashtag, false)
                ));
                case "bar" -> new JSONArray(List.of(
                        createPost(8, hashtag, false),
                        createPost(9, hashtag, false),
                        createPost(10, hashtag, false)
                ));
                case "videos" -> new JSONArray(List.of(
                        createPost(11, hashtag, true),
                        createPost(12, hashtag, true),
                        createPost(13, hashtag, true)
                ));
                case "broken" -> throw new BlueSkyException("This is an expected exception.", new RuntimeException("This is a faked cause."));
                default -> new JSONArray(List.of());
            };
        }

        @Override
        @NotNull
        public JSONArray getPostsWithMention(final @NotNull String instance,
                                             final @NotNull String profile,
                                             final @NotNull String mentionsUrl,
                                             final int postLimit)
                throws BlueSkyException {
            return switch (profile) {
                case "foobar.bsky.social" -> new JSONArray(List.of(
                        createPostWithMention(100, profile),
                        createPostWithMention(101, profile),
                        createPostWithMention(102, profile),
                        createPostWithMention(103, profile),
                        createPostWithMention(104, profile)
                ));
                case "broken" -> throw new BlueSkyException("This is an expected exception.", new RuntimeException("This is a faked cause."));
                default -> new JSONArray(List.of());
            };
        }

        private JSONObject createPost(final int i, final @NotNull String hashtag, boolean withVideo) {
            final var createdAt = ZonedDateTime.of(LocalDateTime.now(TEST_TIMEZONE).minusMinutes(i), TEST_TIMEZONE);
            final var fakeReply = """
                    "reply": {
                      "parent": {
                        "uri": "ID 1"
                      },
                      "root": {
                        "uri": "ID 1"
                      }
                    },
                """;

            final var postJSON = """
                {
                  "uri": "ID ${i}",
                  "author": {
                    "handle": "profile${i}",
                    "displayName": "Display Name ${i}",
                    "avatar": "Avatar ${i}"
                  },
                  "record": {
                    "createdAt": "${createdAt}",
                    ${reply}
                    "text": "Content for post #${i}",
                    "facets": [
                      {
                        "features": [
                          {
                            "$type": "app.bsky.richtext.facet#tag",
                            "tag": "${tag}"
                          }
                        ]
                      },
                    ]
                  },
                  "embed": {
                    "$type": "${embedType}",
                    "images": [
                      {
                        "thumb": "http://localhost/image${i}a.webp"
                      },
                      {
                        "thumb": "http://localhost/image${i}b.webp"
                      }
                    ]
                  }
                }
                """
                .replaceAll(Pattern.quote("${i}"), Integer.toString(i))
                .replaceAll(Pattern.quote("${tag}"), hashtag)
                .replaceAll(Pattern.quote("${createdAt}"), createdAt.format(DATE_TIME_FORMATTER))
                .replaceAll(Pattern.quote("${reply}"), i == 5 ? fakeReply : "")
                .replaceAll(Pattern.quote("${embedType}"), withVideo ? "app.bsky.embed.video#view" : "app.bsky.embed.images#view");

            return new JSONObject(postJSON);
        }

        private JSONObject createPostWithMention(final int i, final @NotNull String profile) {
            final var createdAt = ZonedDateTime.of(LocalDateTime.now(TEST_TIMEZONE).minusMinutes(i), TEST_TIMEZONE);
            final var postJSON = """
                {
                  "uri": "ID ${i}",
                  "author": {
                    "handle": "profile${i}",
                    "displayName": "Display Name ${i}",
                    "avatar": "Avatar ${i}"
                  },
                  "record": {
                    "createdAt": "${createdAt}",
                    "text": "Content for post #${i} which mentions @${profile}",
                    "facets": [
                      {
                        "features": [
                          {
                            "$type": "app.bsky.richtext.facet#tag",
                            "tag": "test"
                          }
                        ]
                      },
                      {
                        "features": [
                          {
                            "$type": "app.bsky.richtext.facet#mention"
                          }
                        ]
                      }
                    ]
                  }
                }
                """
                    .replaceAll(Pattern.quote("${i}"), Integer.toString(i))
                    .replaceAll(Pattern.quote("${createdAt}"), createdAt.format(DATE_TIME_FORMATTER))
                    .replaceAll(Pattern.quote("${profile}"), profile);

            return new JSONObject(postJSON);
        }
    }

    @Test
    void getPostsWithoutEmbed() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", "", "", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new NoEmbedBlueSkyLoader(), appConfig);
        final List<Post> posts = blueSkyPlugin.getPosts(List.of("foobar")).toList();

        assertNotNull(posts);
        assertEquals(3, posts.size());

        for (int i = 0; i < posts.size() - 1; i++) {
            final Post post = posts.get(i);
            assertTrue(post.images().isEmpty());
        }
    }

    private static final class NoEmbedBlueSkyLoader implements BlueSkyLoader {

        @Override
        @NotNull public JSONArray getPostsWithHashtag(final @NotNull String instance,
                                                      final @NotNull String hashtag,
                                                      final @NotNull String postAPI,
                                                      final int postLimit) {
            return new JSONArray(List.of(
                    createPost(1, hashtag),
                    createPost(2, hashtag),
                    createPost(3, hashtag)
            ));
        }

        @Override
        @NotNull
        public JSONArray getPostsWithMention(final @NotNull String instance,
                                             final @NotNull String profile,
                                             final @NotNull String mentionsUrl,
                                             final int postLimit) {
            return new JSONArray();
        }

        private JSONObject createPost(final int i, final @NotNull String hashtag) {
            final var createdAt = ZonedDateTime.of(LocalDateTime.now(TEST_TIMEZONE).minusMinutes(i), TEST_TIMEZONE);
            final var postJSON = """
                {
                  "uri": "ID ${i}",
                  "author": {
                    "handle": "profile${i}",
                    "displayName": "Display Name ${i}",
                    "avatar": "Avatar ${i}"
                  },
                  "record": {
                    "createdAt": "${createdAt}",
                    "text": "Content for post #${i}",
                    "facets": [
                      {
                        "features": [
                          {
                            "$type": "app.bsky.richtext.facet#tag",
                            "tag": "${tag}"
                          }
                        ]
                      }
                    ]
                  }
                }
                """
                    .replaceAll(Pattern.quote("${i}"), Integer.toString(i))
                    .replaceAll(Pattern.quote("${tag}"), hashtag)
                    .replaceAll(Pattern.quote("${createdAt}"), createdAt.format(DATE_TIME_FORMATTER));

            return new JSONObject(postJSON);
        }
    }

    @Test
    void getPostsWithOptionalDataBlank() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", "", "", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new OptionalDataBlankBlueSkyLoader(), appConfig);
        final List<Post> posts = blueSkyPlugin.getPosts(List.of("foobar")).toList();

        assertNotNull(posts);
        assertEquals(3, posts.size());

        for (int i = 0; i < posts.size() - 1; i++) {
            final Post post = posts.get(i);
            assertEquals(post.profile(), post.author());
            assertTrue(post.avatar().isBlank());
        }
    }

    private static final class OptionalDataBlankBlueSkyLoader implements BlueSkyLoader {

        @Override
        @NotNull public JSONArray getPostsWithHashtag(final @NotNull String instance,
                                                      final @NotNull String hashtag,
                                                      final @NotNull String postAPI,
                                                      final int postLimit) {
            return new JSONArray(List.of(
                    createPost(1, hashtag),
                    createPost(2, hashtag),
                    createPost(3, hashtag)
            ));
        }

        @Override
        @NotNull
        public JSONArray getPostsWithMention(final @NotNull String instance,
                                             final @NotNull String profile,
                                             final @NotNull String mentionsUrl,
                                             final int postLimit) {
            return new JSONArray();
        }

        private JSONObject createPost(final int i, final @NotNull String hashtag) {
            final var createdAt = ZonedDateTime.of(LocalDateTime.now(TEST_TIMEZONE).minusMinutes(i), TEST_TIMEZONE);
            final var postJSON = """
                {
                  "uri": "ID ${i}",
                  "author": {
                    "handle": "profile${i}",
                    "displayName": "",
                    "avatar": ""
                  },
                  "record": {
                    "createdAt": "${createdAt}",
                    "text": "Content for post #${i}",
                    "facets": [
                      {
                        "features": [
                          {
                            "$type": "app.bsky.richtext.facet#tag",
                            "tag": "${tag}"
                          }
                        ]
                      }
                    ]
                  }
                }
                """
                    .replaceAll(Pattern.quote("${i}"), Integer.toString(i))
                    .replaceAll(Pattern.quote("${tag}"), hashtag)
                    .replaceAll(Pattern.quote("${createdAt}"), createdAt.format(DATE_TIME_FORMATTER));

            return new JSONObject(postJSON);
        }
    }

    @Test
    void getPostsWithOptionalDataNull() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", "", "", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new OptionalDataNullBlueSkyLoader(), appConfig);
        final List<Post> posts = blueSkyPlugin.getPosts(List.of("foobar")).toList();

        assertNotNull(posts);
        assertEquals(3, posts.size());

        for (int i = 0; i < posts.size() - 1; i++) {
            final Post post = posts.get(i);
            assertEquals(post.profile(), post.author());
            assertTrue(post.avatar().isBlank());
        }
    }

    private static final class OptionalDataNullBlueSkyLoader implements BlueSkyLoader {

        @Override
        @NotNull public JSONArray getPostsWithHashtag(final @NotNull String instance,
                                                      final @NotNull String hashtag,
                                                      final @NotNull String postAPI,
                                                      final int postLimit) {
            return new JSONArray(List.of(
                    createPost(1, hashtag),
                    createPost(2, hashtag),
                    createPost(3, hashtag)
            ));
        }

        @Override
        @NotNull
        public JSONArray getPostsWithMention(final @NotNull String instance,
                                             final @NotNull String profile,
                                             final @NotNull String mentionsUrl,
                                             final int postLimit) {
            return new JSONArray();
        }

        private JSONObject createPost(final int i, final @NotNull String hashtag) {
            final var createdAt = ZonedDateTime.of(LocalDateTime.now(TEST_TIMEZONE).minusMinutes(i), TEST_TIMEZONE);
            final var postJSON = """
                {
                  "uri": "ID ${i}",
                  "author": {
                    "handle": "profile${i}",
                    "displayName": null,
                    "avatar": null
                  },
                  "record": {
                    "createdAt": "${createdAt}",
                    "text": "Content for post #${i}",
                    "facets": [
                      {
                        "features": [
                          {
                            "$type": "app.bsky.richtext.facet#tag",
                            "tag": "${tag}"
                          }
                        ]
                      }
                    ]
                  }
                }
                """
                    .replaceAll(Pattern.quote("${i}"), Integer.toString(i))
                    .replaceAll(Pattern.quote("${tag}"), hashtag)
                    .replaceAll(Pattern.quote("${createdAt}"), createdAt.format(DATE_TIME_FORMATTER));

            return new JSONObject(postJSON);
        }
    }

    @Test
    void getPostsWithOptionalDataMissing() {
        final var appConfig = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", "", "", 30);
        when(appConfig.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new OptionalDataMissingBlueSkyLoader(), appConfig);
        final List<Post> posts = blueSkyPlugin.getPosts(List.of("foobar")).toList();

        assertNotNull(posts);
        assertEquals(3, posts.size());

        for (int i = 0; i < posts.size() - 1; i++) {
            final Post post = posts.get(i);
            assertEquals(post.profile(), post.author());
            assertTrue(post.avatar().isBlank());
        }
    }

    private static final class OptionalDataMissingBlueSkyLoader implements BlueSkyLoader {

        @Override
        @NotNull public JSONArray getPostsWithHashtag(final @NotNull String instance,
                                                      final @NotNull String hashtag,
                                                      final @NotNull String postAPI,
                                                      final int postLimit) {
            return new JSONArray(List.of(
                    createPost(1, hashtag),
                    createPost(2, hashtag),
                    createPost(3, hashtag)
            ));
        }

        @Override
        @NotNull
        public JSONArray getPostsWithMention(final @NotNull String instance,
                                             final @NotNull String profile,
                                             final @NotNull String mentionsUrl,
                                             final int postLimit) {
            return new JSONArray();
        }

        private JSONObject createPost(final int i, final @NotNull String hashtag) {
            final var createdAt = ZonedDateTime.of(LocalDateTime.now(TEST_TIMEZONE).minusMinutes(i), TEST_TIMEZONE);
            final var postJSON = """
                {
                  "uri": "ID ${i}",
                  "author": {
                    "handle": "profile${i}"
                  },
                  "record": {
                    "createdAt": "${createdAt}",
                    "text": "Content for post #${i}",
                    "facets": [
                      {
                        "features": [
                          {
                            "$type": "app.bsky.richtext.facet#tag",
                            "tag": "${tag}"
                          }
                        ]
                      }
                    ]
                  }
                }
                """
                    .replaceAll(Pattern.quote("${i}"), Integer.toString(i))
                    .replaceAll(Pattern.quote("${tag}"), hashtag)
                    .replaceAll(Pattern.quote("${createdAt}"), createdAt.format(DATE_TIME_FORMATTER));

            return new JSONObject(postJSON);
        }
    }
}
