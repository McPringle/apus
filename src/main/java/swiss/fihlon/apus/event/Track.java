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
package swiss.fihlon.apus.event;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public record Track(String name, String svgCode) {

    public static final Logger LOGGER = LoggerFactory.getLogger(Track.class);

    public static final Track NONE = new Track(null, null);
    public static final Track ARCHITECTURE = defaultTrack("Architecture", "architecture.svg");
    public static final Track CLOUD = defaultTrack("Cloud", "cloud.svg");
    public static final Track CORE = defaultTrack("Core", "core.svg");
    public static final Track INFRASTRUCTURE = defaultTrack("Infrastructure", "infrastructure.svg");
    public static final Track SECURITY = defaultTrack("Security", "security.svg");
    public static final Track TOOLS = defaultTrack("Tools", "tools.svg");

    private static final String FILE_NAME_TEMPLATE = "/icons/tracks/%s";

    private static Track defaultTrack(@NotNull final String name, @NotNull final String svgFileName) {
        try {
            final String fileName = FILE_NAME_TEMPLATE.formatted(svgFileName);
            final URL url = Track.class.getResource(fileName);
            final URI uri = Objects.requireNonNull(url).toURI();
            final Path path = Paths.get(uri);
            final String svgCode = Files.readString(path);
            return new Track(name, svgCode.trim());
        } catch (final IOException | NullPointerException | URISyntaxException e) {
            LOGGER.error("Unable to load default track icon '{}': {}", svgFileName, e.getMessage(), e);
        }
        return NONE;
    }
}
