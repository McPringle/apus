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
package swiss.fihlon.apus.social.mastodon;

import org.junit.jupiter.api.Test;
import swiss.fihlon.apus.social.Post;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MastodonAPITest {

    @Test
    void getPosts() {
        final MastodonAPI mastodonAPI = new MastodonAPI("mastodon.social");
        final List<Post> posts = mastodonAPI.getPosts("java");
        assertNotNull(posts);
        for (final Post post : posts) {
            System.out.println(post);
        }
    }
}
