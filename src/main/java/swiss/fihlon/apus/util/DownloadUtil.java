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
package swiss.fihlon.apus.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class DownloadUtil {

    public static String getString(final String location)
            throws IOException, URISyntaxException {
        final var connection = new URI(location).toURL().openConnection();
        if (connection instanceof HttpURLConnection http && http.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
            try {
                throw forbiddenResponse(http);
            } finally {
                http.disconnect();
            }
        }
        try (InputStream in = connection.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static HttpDownloadException forbiddenResponse(final HttpURLConnection connection) {
        final var details = new StringBuilder();
        for (final var name : List.of("Server", "Date", "Content-Type", "Retry-After", "RateLimit-Limit",
                "RateLimit-Remaining", "RateLimit-Reset", "X-RateLimit-Limit", "X-RateLimit-Remaining",
                "X-RateLimit-Reset", "X-Request-ID", "CDN-RequestId", "CF-Ray")) {
            final var value = connection.getHeaderField(name);
            if (value != null) {
                details.append(name).append('=').append(compact(value)).append("; ");
            }
        }
        try (InputStream error = connection.getErrorStream()) {
            if (error == null) {
                details.append("body=<empty>");
            } else {
                final var bytes = error.readNBytes(2049);
                final var body = new String(bytes, 0, Math.min(bytes.length, 2048), StandardCharsets.UTF_8);
                details.append("body=").append(compact(body + (bytes.length > 2048 ? " [truncated]" : "")));
            }
        } catch (final IOException e) {
            details.append("body=<unavailable: ").append(e.getClass().getSimpleName()).append('>');
        }
        return new HttpDownloadException(HttpURLConnection.HTTP_FORBIDDEN, connection.getURL().toString(), details.toString());
    }

    private static String compact(final String text) {
        final var singleLine = text.replaceAll("[\\p{Cntrl}\\s]+", " ").trim();
        return singleLine.length() > 512 ? singleLine.substring(0, 512) + " [truncated]" : singleLine;
    }

    public static String getString(final String location, final String accessToken)
            throws IOException, InterruptedException {
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .uri(URI.create(location))
                    .GET()
                    .build();
            var  response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            return response.body();
        }
    }

    private DownloadUtil() {
        throw new IllegalStateException("Utility classes can't be instantiated!");
    }

}
