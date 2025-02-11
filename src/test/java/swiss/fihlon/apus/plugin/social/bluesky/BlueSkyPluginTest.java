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

    @Test
    void getServiceName() {
        final var configuration = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", 30);
        when(configuration.blueSky()).thenReturn(blueSkyConfig);

        final var blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), configuration);
        assertEquals("BlueSky", blueSkyPlugin.getServiceName());
    }

    private static Stream<Arguments> provideDataForDisabledTest() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(null, ""),
                Arguments.of("", null),
                Arguments.of("", ""),

                Arguments.of(null, " "),
                Arguments.of(" ", " "),
                Arguments.of(" ", null),

                Arguments.of(null, "test"),

                Arguments.of("test", null),
                Arguments.of("test", ""),
                Arguments.of("test", " ")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForDisabledTest")
    void isDisabled(@Nullable final String instance, @Nullable final String postAPI) {
        final var configuration = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig(instance, postAPI, 30);
        when(configuration.blueSky()).thenReturn(blueSkyConfig);

        final var blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), configuration);
        assertFalse(blueSkyPlugin.isEnabled());
    }

    @Test
    void isEnabled() {
        final var configuration = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "test", 30);
        when(configuration.blueSky()).thenReturn(blueSkyConfig);

        final var blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), configuration);
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
    void getPostsWithHashtags(@NotNull final List<String> hashtags, final int expectedNumberOfPosts) {
        final var configuration = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", 30);
        when(configuration.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), configuration);
        final List<Post> posts = blueSkyPlugin.getPosts(hashtags).toList();

        assertNotNull(posts);
        assertEquals(expectedNumberOfPosts, posts.size());
    }

    @Test
    void getPostsWithUnlimitedImages() {
        final var configuration = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", 30);
        when(configuration.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), configuration);
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
    void getPostsCatchesException() {
        final var configuration = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", 30);
        when(configuration.blueSky()).thenReturn(blueSkyConfig);

        final MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        final Logger logger = (Logger) LoggerFactory.getLogger(BlueSkyPlugin.class);
        logger.addAppender(memoryAppender);

        memoryAppender.start();
        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), configuration);
        blueSkyPlugin.getPosts(List.of("broken"));
        memoryAppender.stop();

        final int errorCount = memoryAppender.searchMessages("This is an expected exception.", Level.ERROR).size();
        assertEquals(1, errorCount);
    }

    @Test
    void testReplyConversion() {
        final var configuration = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", 30);
        when(configuration.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new TestBlueSkyLoader(), configuration);
        final List<Post> posts = blueSkyPlugin.getPosts(List.of("foobar")).toList();

        assertNotNull(posts);
        assertEquals(5, posts.size());

        for (int i = 0; i < posts.size() - 1; i++) {
            final Post post = posts.get(i);
            assertFalse(post.isReply(), "Post with " + post.id());
        }

        assertTrue(posts.getLast().isReply());
    }

    private static final class TestBlueSkyLoader implements BlueSkyLoader {

        @Override
        @NotNull public JSONArray getPosts(@NotNull String instance, @NotNull String hashtag, @NotNull String postAPI, int postLimit)
                throws BlueSkyException {
            return switch (hashtag) {
                case "foobar" -> new JSONArray(List.of(
                        createPost(1, hashtag),
                        createPost(2, hashtag),
                        createPost(3, hashtag),
                        createPost(4, hashtag),
                        createPost(5, hashtag)
                ));
                case "foo" -> new JSONArray(List.of(
                        createPost(6, hashtag),
                        createPost(7, hashtag)
                ));
                case "bar" -> new JSONArray(List.of(
                        createPost(8, hashtag),
                        createPost(9, hashtag),
                        createPost(10, hashtag)
                ));
                case "broken" -> throw new BlueSkyException("This is an expected exception.", new RuntimeException("This is a faked cause."));
                default -> new JSONArray(List.of());
            };
        }

        private JSONObject createPost(final int i, @NotNull final String hashtag) {
            final var createdAt = ZonedDateTime.of(LocalDateTime.now().minusMinutes(i), ZoneId.systemDefault());
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
                    "$type": "app.bsky.embed.images#view",
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
                .replaceAll(Pattern.quote("${reply}"), i == 5 ? fakeReply : "");

            return new JSONObject(postJSON);
        }
    }

    @Test
    void getPostsWithoutEmbed() {
        final var configuration = mock(AppConfig.class);
        final var blueSkyConfig = new BlueSkyConfig("localhost", "https://%s/q=%s&limit=%d", 30);
        when(configuration.blueSky()).thenReturn(blueSkyConfig);

        final BlueSkyPlugin blueSkyPlugin = new BlueSkyPlugin(new NoEmbedBlueSkyLoader(), configuration);
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
        @NotNull public JSONArray getPosts(@NotNull String instance, @NotNull String hashtag, @NotNull String postAPI, int postLimit) {
            return new JSONArray(List.of(
                    createPost(1, hashtag),
                    createPost(2, hashtag),
                    createPost(3, hashtag)
            ));
        }

        private JSONObject createPost(final int i, @NotNull final String hashtag) {
            final var createdAt = ZonedDateTime.of(LocalDateTime.now().minusMinutes(i), ZoneId.systemDefault());
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
}
