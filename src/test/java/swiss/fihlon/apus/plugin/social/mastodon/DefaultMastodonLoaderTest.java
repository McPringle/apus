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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultMastodonLoaderTest {

    @RetryingTest(3)
    void getStatuses() throws MastodonException {
        final JSONArray posts = new DefaultMastodonLoader().getPostsWithHashtag("ijug.social", "java",
                "https://${instance}/api/v1/timelines/tag/${hashtag}?limit=${limit}", 1);
        assertNotNull(posts);
        assertFalse(posts.isEmpty());
    }

    @Test
    void throwException() {
        final var exception = assertThrows(MastodonException.class,
                () -> new DefaultMastodonLoader().getPostsWithHashtag("non.existent.server", "java",
                        "https://${instance}/api/v1/timelines/tag/${hashtag}?limit=${limit}", 1));
        assertEquals("Unable to load posts with hashtag 'java' from Mastodon instance 'non.existent.server'", exception.getMessage());
    }

    @Test
    void getStatusWithMention() throws MastodonException {
        final JSONArray posts = new DefaultMastodonLoader().getMentions("fosstodon.org", "PggcEcIBzo8vfyZMFj25UogbwtkmVYKaMbmQS8a1gCo", "https://${instance}/api/v1/notifications?limit=${limit}", 1  );

        assertNotNull(posts);
        assertFalse(posts.isEmpty());
    }


}
