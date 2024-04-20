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

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import social.bigbone.PrecisionDateTime;
import social.bigbone.api.entity.Account;
import social.bigbone.api.entity.MediaAttachment;
import social.bigbone.api.entity.Status;
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
                Arguments.of("", ""),
                Arguments.of(" ", " "),
                Arguments.of("", "foobar"),
                Arguments.of(" ", "foobar"),
                Arguments.of("localhost", ""),
                Arguments.of("localhost", " ")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForDisabledTest")
    void isDisabled(final String instance, final String hashtag) {
        final var configuration = mock(Configuration.class);
        final var mastodonConfig = mock(MastodonConfig.class);
        when(configuration.getMastodon()).thenReturn(mastodonConfig);
        when(mastodonConfig.instance()).thenReturn(instance);
        when(mastodonConfig.hashtag()).thenReturn(hashtag);

        final var mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        assertFalse(mastodonPlugin.isEnabled());
    }

    @Test
    void isEnabled() {
        final var configuration = mock(Configuration.class);
        final var mastodonConfig = mock(MastodonConfig.class);
        when(configuration.getMastodon()).thenReturn(mastodonConfig);
        when(mastodonConfig.instance()).thenReturn("localhost");
        when(mastodonConfig.hashtag()).thenReturn("foobar");

        final var mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        assertTrue(mastodonPlugin.isEnabled());
    }

    @Test
    void getPosts() {
        final var configuration = mock(Configuration.class);
        when(configuration.getMastodon()).thenReturn(
                new MastodonConfig("localhost", "foobar", true, 0));

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(new TestMastodonLoader(), configuration);
        final List<Post> posts = mastodonPlugin.getPosts();

        assertNotNull(posts);
        assertEquals(5, posts.size());

        final var firstPost = posts.getFirst();
        assertEquals("ID 1", firstPost.id());
        assertEquals("Display Name 1", firstPost.author());
        assertEquals("Avatar 1", firstPost.avatar());
        assertEquals("profile1@localhost", firstPost.profile());
        assertEquals(1, firstPost.images().size());
        assertEquals("http://localhost/image1.webp", firstPost.images().getFirst());
    }

    private static final class TestMastodonLoader implements MastodonLoader {

        @Override
        @NotNull public List<Status> getStatuses(@NotNull String instance, @NotNull String hashtag) {
            return List.of(
                    createStatus(1),
                    createStatus(2),
                    createStatus(3),
                    createStatus(4),
                    createStatus(5)
            );
        }

        private Status createStatus(final int i) {
            final Account account = mock(Account.class);
            when(account.getDisplayName()).thenReturn("Display Name " + i);
            when(account.getAvatar()).thenReturn("Avatar " + i);
            when(account.getAcct()).thenReturn("profile" + i);

            final PrecisionDateTime createdAt = mock(PrecisionDateTime.class);
            when(createdAt.mostPreciseOrFallback(any())).thenReturn(Instant.now().minus(i, ChronoUnit.MINUTES));

            final MediaAttachment mediaAttachment = mock(MediaAttachment.class);
            when(mediaAttachment.getType()).thenReturn(MediaAttachment.MediaType.IMAGE);
            when(mediaAttachment.getUrl()).thenReturn("http://localhost/image" + i + ".webp");
            final List<MediaAttachment> mediaAttachments = List.of(mediaAttachment);

            final Status status = mock(Status.class);
            when(status.getId()).thenReturn("ID " + i);
            when(status.getAccount()).thenReturn(account);
            when(status.getCreatedAt()).thenReturn(createdAt);
            when(status.getContent()).thenReturn("Content for post #" + i);
            when(status.getMediaAttachments()).thenReturn(mediaAttachments);
            when(status.getInReplyToId()).thenReturn(null);
            when(status.isSensitive()).thenReturn(false);
            return status;
        }
    }
}
