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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.util.HttpDownloadException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultBlueSkyLoaderTest {

    private static final String HASHTAG_URL = "http://${instance}/posts?q=%23${hashtag}&limit=${limit}";
    private static final String MENTIONS_URL = "http://${instance}/posts?q=%40${profile}&limit=${limit}";
    private static HttpServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/posts", DefaultBlueSkyLoaderTest::sendPosts);
        server.createContext("/forbidden", exchange -> {
            exchange.getResponseHeaders().set("Server", "test-proxy");
            exchange.getResponseHeaders().set("Retry-After", "120");
            exchange.getResponseHeaders().set("CDN-RequestId", "request-123");
            exchange.getResponseHeaders().set("Set-Cookie", "secret-cookie");
            final var body = ("Forbidden\nby administrative rules. " + "x".repeat(3000)).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(403, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.createContext("/empty-forbidden", exchange -> {
            exchange.sendResponseHeaders(403, -1);
            exchange.close();
        });
        server.createContext("/failure", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
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

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void forbiddenResponseLogsDiagnosticsWithoutStackTrace(final boolean mentions) {
        final var events = downloadAndCaptureErrors("/forbidden", mentions);
        assertEquals(1, events.size());
        final var event = events.getFirst();
        assertNull(event.getThrowableProxy());
        final var message = event.getFormattedMessage();
        assertTrue(message.contains("HTTP 403"));
        assertTrue(message.contains("http://localhost:" + server.getAddress().getPort() + "/forbidden"));
        assertTrue(message.contains(mentions ? "profile 'jugch.bsky.social'" : "hashtag 'java'"));
        assertTrue(message.contains("Server=test-proxy"));
        assertTrue(message.contains("Retry-After=120"));
        assertTrue(message.contains("CDN-RequestId=request-123"));
        assertTrue(message.contains("Forbidden by administrative rules."));
        assertTrue(message.contains("[truncated]"));
        assertFalse(message.contains("secret-cookie"));
        assertFalse(message.contains("\n"));
        assertTrue(message.length() < 1500);
    }

    @Test
    void forbiddenWithoutBodyPreservesStatus() {
        final var exception = assertThrows(BlueSkyException.class, () -> new DefaultBlueSkyLoader()
                .getPostsWithHashtag("localhost", "java",
                        "http://localhost:" + server.getAddress().getPort() + "/empty-forbidden", 30));
        assertTrue(exception.getCause() instanceof HttpDownloadException);
        assertEquals(403, ((HttpDownloadException) exception.getCause()).getStatusCode());
    }

    @Test
    void otherHttpErrorsKeepStackTrace() {
        final var events = downloadAndCaptureErrors("/failure", false);
        assertEquals(1, events.size());
        assertNotNull(events.getFirst().getThrowableProxy());
    }

    private List<ILoggingEvent> downloadAndCaptureErrors(final String path, final boolean mentions) {
        final var config = mock(AppConfig.class);
        final var url = "http://localhost:" + server.getAddress().getPort() + path;
        when(config.blueSky()).thenReturn(new BlueSkyConfig("localhost", url, mentions ? url : "",
                mentions ? "jugch.bsky.social" : "", 30));
        final var logger = (Logger) LoggerFactory.getLogger(BlueSkyPlugin.class);
        final var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try {
            final var plugin = new BlueSkyPlugin(new DefaultBlueSkyLoader(), config);
            assertTrue(plugin.getPosts(mentions ? List.of() : List.of("java")).toList().isEmpty());
            return appender.list.stream().filter(event -> event.getLevel() == Level.ERROR).toList();
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
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
