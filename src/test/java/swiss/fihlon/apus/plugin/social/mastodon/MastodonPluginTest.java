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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import social.bigbone.PrecisionDateTime;
import social.bigbone.api.entity.Account;
import social.bigbone.api.entity.MediaAttachment;
import social.bigbone.api.entity.Status;
import swiss.fihlon.apus.MemoryAppender;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.social.Post;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MastodonPluginTest {

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
    void isDisabled(final String instance, final String hashtag) {
        final var configuration = mock(Configuration.class);
        final var mastodonConfig = new MastodonConfig(instance, hashtag, true, 0);
        when(configuration.getMastodon()).thenReturn(mastodonConfig);

        final var mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        assertFalse(mastodonPlugin.isEnabled());
    }

    @Test
    void isEnabled() {
        final var configuration = mock(Configuration.class);
        final var mastodonConfig = new MastodonConfig("localhost", "foobar", true, 0);
        when(configuration.getMastodon()).thenReturn(mastodonConfig);

        final var mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        assertTrue(mastodonPlugin.isEnabled());
    }

    @Test
    void getPostsWithUnlimitedImages() {
        final var configuration = mock(Configuration.class);
        when(configuration.getMastodon()).thenReturn(
                new MastodonConfig("localhost", "foobar,foo,,bar", true, 0));

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
        final var configuration = mock(Configuration.class);
        when(configuration.getMastodon()).thenReturn(
                new MastodonConfig("localhost", "foobar", true, 1));

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
        final var configuration = mock(Configuration.class);
        when(configuration.getMastodon()).thenReturn(
                new MastodonConfig("localhost", "foobar", false, 0));

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
        final var configuration = mock(Configuration.class);
        when(configuration.getMastodon()).thenReturn(
                new MastodonConfig("localhost", "invalidImageType", true, 0));

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
        final var configuration = mock(Configuration.class);
        when(configuration.getMastodon()).thenReturn(
                new MastodonConfig("localhost", "broken", true, 0));

        final MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        @SuppressWarnings("LoggerInitializedWithForeignClass") final Logger logger = (Logger) LoggerFactory.getLogger(MastodonPlugin.class);
        logger.addAppender(memoryAppender);

        memoryAppender.start();
        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        mastodonPlugin.getPosts();
        memoryAppender.stop();

        final int errorCount = memoryAppender.searchMessages("This is an expected exception.", Level.ERROR).size();
        assertEquals(1, errorCount);
    }

    private static final class TestMastodonLoader implements MastodonLoader {

        @Override
        @NotNull public List<Status> getStatuses(@NotNull String instance, @NotNull String hashtag) throws MastodonException {
            if (hashtag.equals("foobar")) {
                return List.of(
                        createStatus(1, false),
                        createStatus(2, false),
                        createStatus(3, false),
                        createStatus(4, false),
                        createStatus(5, false)
                );
            } else if (hashtag.equals("invalidImageType")) {
                return List.of(
                        createStatus(1, true),
                        createStatus(2, true),
                        createStatus(3, true),
                        createStatus(4, true),
                        createStatus(5, true)
                );
            } else if (hashtag.equals("broken")) {
                throw new MastodonException("This is an expected exception.", new RuntimeException("This is a faked cause."));
            }
            return List.of();
        }

        private Status createStatus(final int i, boolean invalidImageType) {
            final Account account = mock(Account.class);
            when(account.getDisplayName()).thenReturn("Display Name " + i);
            when(account.getAvatar()).thenReturn("Avatar " + i);
            when(account.getAcct()).thenReturn("profile" + i);

            final PrecisionDateTime createdAt = mock(PrecisionDateTime.class);
            when(createdAt.mostPreciseOrFallback(any())).thenReturn(Instant.now().minus(i, ChronoUnit.MINUTES));

            final MediaAttachment mediaAttachmentA = mock(MediaAttachment.class);
            when(mediaAttachmentA.getType()).thenReturn(invalidImageType ? MediaAttachment.MediaType.VIDEO : MediaAttachment.MediaType.IMAGE);
            when(mediaAttachmentA.getUrl()).thenReturn("http://localhost/image" + i + "a.webp");
            final MediaAttachment mediaAttachmentB = mock(MediaAttachment.class);
            when(mediaAttachmentB.getType()).thenReturn(MediaAttachment.MediaType.IMAGE);
            when(mediaAttachmentB.getUrl()).thenReturn("http://localhost/image" + i + "b.webp");
            final List<MediaAttachment> mediaAttachments = List.of(mediaAttachmentA, mediaAttachmentB);

            final Status status = mock(Status.class);
            when(status.getId()).thenReturn("ID " + i);
            when(status.getAccount()).thenReturn(i == 1 ? account : null);
            when(status.getCreatedAt()).thenReturn(createdAt);
            when(status.getContent()).thenReturn("Content for post #" + i);
            when(status.getMediaAttachments()).thenReturn(mediaAttachments);
            when(status.getInReplyToId()).thenReturn(i == 1 ? null : " ");
            when(status.isSensitive()).thenReturn(false);
            return status;
        }
    }
}
