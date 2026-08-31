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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultBlueSkyLoaderTest {

    private static final String HASHTAG_URL = "http://${instance}/posts?q=%23${hashtag}&limit=${limit}";
    private static final String MENTIONS_URL = "http://${instance}/posts?q=%40${profile}&limit=${limit}";
    private static HttpServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/posts", DefaultBlueSkyLoaderTest::sendPosts);
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void getPostsWithHashtag() throws BlueSkyException {
        final var instance = "localhost:" + server.getAddress().getPort();
        final JSONArray jsonPosts = new DefaultBlueSkyLoader()
                .getPostsWithHashtag(instance, "java", HASHTAG_URL, 30);
        assertNotNull(jsonPosts);
        assertFalse(jsonPosts.isEmpty());
    }

    @Test
    void getPostsWithHashtagShouldThrowException() {
        final var exception = assertThrows(BlueSkyException.class,
                () -> new DefaultBlueSkyLoader()
                        .getPostsWithHashtag("non.existent.server", "java", HASHTAG_URL, 30));
        assertEquals("Unable to load posts with hashtag 'java' from BlueSky instance 'non.existent.server'", exception.getMessage());
    }

    @Test
    void getPostsWithMention() throws BlueSkyException {
        final var instance = "localhost:" + server.getAddress().getPort();
        final JSONArray jsonPosts = new DefaultBlueSkyLoader()
                .getPostsWithMention(instance, "jugch.bsky.social", MENTIONS_URL, 30);
        assertNotNull(jsonPosts);
        assertFalse(jsonPosts.isEmpty());
    }

    @Test
    void getPostsWithMentionShouldThrowException() {
        final var exception = assertThrows(BlueSkyException.class,
                () -> new DefaultBlueSkyLoader()
                        .getPostsWithMention("non.existent.server", "jugch.bsky.social", MENTIONS_URL, 30));
        assertEquals("Unable to load posts with profile 'jugch.bsky.social' from BlueSky instance 'non.existent.server'", exception.getMessage());
    }

    private static void sendPosts(final HttpExchange exchange) throws IOException {
        final byte[] response = "{\"posts\":[{}]}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (var responseBody = exchange.getResponseBody()) {
            responseBody.write(response);
        }
    }

}
