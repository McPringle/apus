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

public enum TrackInfo {

    // Important: When adding a new trackInfo, modify the test accordingly!
    NONE("", """
            """),
    ARCH("arch", """
            architecture"""),
    NETWORK("network", """
            network""");

    private final String trackInfoCode;
    private final String svgCode;

    public static TrackInfo trackInfoWithCode(@NotNull final String trackInfoCode) {
        for (final TrackInfo trackInfo : values()) {
            if (trackInfo.trackInfoCode.equals(trackInfoCode)) {
                return trackInfo;
            }
        }
        throw new IllegalArgumentException(String.format("No trackInfo constant with trackInfo code '%s'!", trackInfoCode));
    }

    TrackInfo(@NotNull final String trackInfoCode, @NotNull final String svgCode) {
        this.trackInfoCode = trackInfoCode;
        this.svgCode = svgCode;
    }

    @NotNull
    public String getLanguageCode() {
        return trackInfoCode;
    }

    @NotNull
    public String getSvgCode() {
        return svgCode;
    }
}
