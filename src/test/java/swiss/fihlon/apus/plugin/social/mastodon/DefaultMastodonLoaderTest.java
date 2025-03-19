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

import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import swiss.fihlon.apus.configuration.AppConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class DefaultMastodonLoaderTest {

    @Autowired
    private AppConfig appConfig;

    @RetryingTest(3)
    void getStatuses() throws MastodonException {
        final var mastodonConfig = appConfig.mastodon();
        final JSONArray posts = new DefaultMastodonLoader().getPosts(
                mastodonConfig.instance(), "java", mastodonConfig.postAPI(), 1);
        assertNotNull(posts);
        assertFalse(posts.isEmpty());
    }

    @RetryingTest(3)
    void getStatusWithMention() throws MastodonException {
        final var mastodonConfig = appConfig.mastodon();
        final JSONArray posts = new DefaultMastodonLoader().getNotifications(
                mastodonConfig.instance(), mastodonConfig.notificationAPI(), mastodonConfig.accessToken(), 1  );
        assertNotNull(posts);
        assertFalse(posts.isEmpty());
    }

    @Test
    void nonExistingServerThrowsExceptionOnGetPost() {
        final var mastodonConfig = appConfig.mastodon();
        final var exception = assertThrows(MastodonException.class,
                () -> new DefaultMastodonLoader().getPosts("non.existent.server", "java",
                        mastodonConfig.postAPI(), 1));
        assertEquals("Unable to load posts with hashtag 'java' from Mastodon instance 'non.existent.server'", exception.getMessage());
    }

    @Test
    void nonExistingServerThrowsExceptionOnGetNotifications() {
        final var mastodonConfig = appConfig.mastodon();
        final var exception = assertThrows(MastodonException.class,
                () -> new DefaultMastodonLoader().getNotifications("non.existent.server",
                        mastodonConfig.notificationAPI(), mastodonConfig.accessToken(), 1));
        assertEquals("Unable to load notifications from Mastodon instance 'non.existent.server'", exception.getMessage());
    }

}
