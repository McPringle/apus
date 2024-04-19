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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.social.Post;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MastodonPluginTest {

    private static Stream<Arguments> provideDisabledData() {
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
    @MethodSource("provideDisabledData")
    void isDisabled(final String instance, final String hashtag) {
        final var configuration = mock(Configuration.class);
        final var mastodonConfig = mock(MastodonConfig.class);
        when(configuration.getMastodon()).thenReturn(mastodonConfig);
        when(mastodonConfig.instance()).thenReturn(instance);
        when(mastodonConfig.hashtag()).thenReturn(hashtag);

        final var mastodonPlugin = new MastodonPlugin(configuration);
        assertFalse(mastodonPlugin.isEnabled());
    }

    @Test
    void isEnabled() {
        final var configuration = mock(Configuration.class);
        final var mastodonConfig = mock(MastodonConfig.class);
        when(configuration.getMastodon()).thenReturn(mastodonConfig);
        when(mastodonConfig.instance()).thenReturn("localhost");
        when(mastodonConfig.hashtag()).thenReturn("foobar");

        final var mastodonPlugin = new MastodonPlugin(configuration);
        assertTrue(mastodonPlugin.isEnabled());
    }

    @Test
    void getPosts() {
        final var configuration = mock(Configuration.class);
        when(configuration.getMastodon()).thenReturn(
                new MastodonConfig("mastodon.social", "java", true, 0));

        final MastodonPlugin mastodonPlugin = new MastodonPlugin(configuration);
        final List<Post> posts = mastodonPlugin.getPosts();

        assertNotNull(posts);
    }
}
