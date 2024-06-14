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

@SuppressWarnings("checkstyle:LineLength")
public enum Track {

    // Important: When adding a new track, modify the test accordingly!
    NONE("", ""),
    ARCHITECTURE("Architecture", "java.png"),
    CONTAINER_CLOUD_INFRASTRUCTURE("Container, Cloud & Infrastructure", "java.png"),
    DATA_STREAMING_AI("Data, Streaming & AI", "java.png"),
    ENTERPRISE_JAVA("Enterprise Java", "java.png"),
    JAVA("Core Java & JVM-Technologies", "java.png"),
    LANGUAGES("Programming Languages", "java.png"),
    METHODOLOGY_CULTURE("Methodology & Culture", "java.png"),
    NEXT("What's next", "java.png"),
    QUALITY_TESTING("Quality & Testing", "java.png"),
    SECURITY("Security", "java.png"),
    STUDIO("Studio", "java.png"),
    TOOLS("Tools", "java.png"),
    UI_UX("UI & UX", "java.png");

    private static final String FILE_NAME_TEMPLATE = "icons/tracks/%s";

    private final String trackName;
    private final String fileName;

    Track(@NotNull final String trackName, @NotNull final String fileName) {
        this.trackName = trackName;
        this.fileName = FILE_NAME_TEMPLATE.formatted(fileName);
    }

    @NotNull
    public String getTrackName() {
        return trackName;
    }

    @NotNull
    public String getFileName() {
        return fileName;
    }
}
