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

import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultBlueSkyLoaderTest {

    @RetryingTest(3)
    void getPosts() throws BlueSkyException {
        final JSONArray jsonPosts = new DefaultBlueSkyLoader()
                .getPosts("api.bsky.app", "java", "https://%s/xrpc/app.bsky.feed.searchPosts?q=%s&limit=%d", 30);
        assertNotNull(jsonPosts);
        assertFalse(jsonPosts.isEmpty());
    }

    @Test
    void throwException() {
        final var exception = assertThrows(BlueSkyException.class,
                () -> new DefaultBlueSkyLoader()
                        .getPosts("non.existent.server", "java", "https://%s/xrpc/app.bsky.feed.searchPosts?q=%s&limit=%d", 30));
        assertEquals("Unable to load posts with hashtag 'java' from BlueSky instance 'non.existent.server'", exception.getMessage());
    }

}
