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
package swiss.fihlon.apus.social;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostTest {

    @Test
    void compareTo() {
        final var now = LocalDateTime.now();

        final var postOne = new Post("P1", now, "", "", "", "", List.of(), false, false);
        final var postTwo = new Post("P2", now.minusHours(1), "", "", "", "", List.of(), false, false);
        final var postThree = new Post("P3", now.minusHours(2), "", "", "", "", List.of(), false, false);
        final var postFour = new Post("P4", now.minusHours(3), "", "", "", "", List.of(), false, false);
        final var postFive = new Post("P5", now.minusHours(4), "", "", "", "", List.of(), false, false);
        final var postSix = new Post("P6", now.minusHours(5), "", "", "", "", List.of(), false, false);
        final var postSeven = new Post("P7", now.minusHours(6), "", "", "", "", List.of(), false, false);
        final var postEight = new Post("P8", now.minusHours(6), "", "", "", "", List.of(), false, false);

        final var unsortedPosts = new ArrayList<>(List.of(postTwo, postSix, postThree, postFour, postFive, postOne));
        Collections.shuffle(unsortedPosts);

        unsortedPosts.add(postEight); // P8 before P7
        unsortedPosts.add(postSeven); // both have the same date and time

        final var sortedPosts = unsortedPosts.stream().sorted().toList();
        assertEquals(postOne, sortedPosts.get(0)); // P1 to P6 ordered by date and time
        assertEquals(postTwo, sortedPosts.get(1));
        assertEquals(postThree, sortedPosts.get(2));
        assertEquals(postFour, sortedPosts.get(3));
        assertEquals(postFive, sortedPosts.get(4));
        assertEquals(postSix, sortedPosts.get(5));
        assertEquals(postSeven, sortedPosts.get(6)); // P7 before P8, order by ID
        assertEquals(postEight, sortedPosts.get(7)); // both have the same date and time
    }

    @Test
    void create() {
        final var id = "P1";
        final var date = LocalDateTime.now();
        final var author = "Test Author";
        final var avatar = "Test Avatar";
        final var profile = "Test Profile";
        final var html = "Test HTML";
        final List<String> images = List.of();
        final var isReply = false;
        final var isSensitive = false;

        final var post = new Post(id, date, author, avatar, profile, html, images, isReply, isSensitive);
        assertEquals(id, post.id());
        assertEquals(date, post.date());
        assertEquals(author, post.author());
        assertEquals(avatar, post.avatar());
        assertEquals(profile, post.profile());
        assertEquals(html, post.html());
        assertEquals(images, post.images());
        assertEquals(isReply, post.isReply());
        assertEquals(isSensitive, post.isSensitive());
    }
}
