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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import swiss.fihlon.apus.configuration.AppConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class DefaultBlueSkyLoaderTest {

    @Autowired
    private AppConfig appConfig;

    @RetryingTest(3)
    void getPostsWithHashtag() throws BlueSkyException {
        final JSONArray jsonPosts = new DefaultBlueSkyLoader()
                .getPostsWithHashtag(appConfig.blueSky().instance(), "java", appConfig.blueSky().hashtagUrl(), 30);
        assertNotNull(jsonPosts);
        assertFalse(jsonPosts.isEmpty());
    }

    @Test
    void getPostsWithHashtagShouldThrowException() {
        final var exception = assertThrows(BlueSkyException.class,
                () -> new DefaultBlueSkyLoader()
                        .getPostsWithHashtag("non.existent.server", "java", appConfig.blueSky().hashtagUrl(), 30));
        assertEquals("Unable to load posts with hashtag 'java' from BlueSky instance 'non.existent.server'", exception.getMessage());
    }


    @RetryingTest(3)
    void getPostsWithMention() throws BlueSkyException {
        final JSONArray jsonPosts = new DefaultBlueSkyLoader()
                .getPostsWithMention(appConfig.blueSky().instance(), "jugch.bsky.social", appConfig.blueSky().mentionsUrl(), 30);
        assertNotNull(jsonPosts);
        assertFalse(jsonPosts.isEmpty());
    }

    @Test
    void getPostsWithMentionShouldThrowException() {
        final var exception = assertThrows(BlueSkyException.class,
                () -> new DefaultBlueSkyLoader()
                        .getPostsWithMention("non.existent.server", "jugch.bsky.social", appConfig.blueSky().mentionsUrl(), 30));
        assertEquals("Unable to load posts with profile 'jugch.bsky.social' from BlueSky instance 'non.existent.server'", exception.getMessage());
    }

}
