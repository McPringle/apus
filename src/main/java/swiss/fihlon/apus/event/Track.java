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
    NONE("""
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                <rect width="100%" height="100%" fill="#FFA500"/>
                <path fill="#FFFFFF" d="M10.62 13.06h2.76l-1.05-2.88q-.08-.19-.16-.46-.09-.26-.17-.57-.08.31-.17.57-.08.27-.16.47l-1.05 2.87Zm2.21-5.4 3.4 8.68h-1.24q-.21 0-.35-.1-.13-.11-.19-.27l-.65-1.77h-3.6l-.64 1.77q-.05.14-.19.26-.14.11-.34.11H7.77l3.41-8.68h1.65Z"/>
            </svg>"""),
    ARCHITECTURE("""
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                <rect width="100%" height="100%" fill="#FFA500"/>
                <path fill="#FFFFFF" d="M10.62 13.06h2.76l-1.05-2.88q-.08-.19-.16-.46-.09-.26-.17-.57-.08.31-.17.57-.08.27-.16.47l-1.05 2.87Zm2.21-5.4 3.4 8.68h-1.24q-.21 0-.35-.1-.13-.11-.19-.27l-.65-1.77h-3.6l-.64 1.77q-.05.14-.19.26-.14.11-.34.11H7.77l3.41-8.68h1.65Z"/>
            </svg>"""),
    JAVA("""
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                <rect width="100%" height="100%" fill="#FFA500"/>
                <path fill="#FFFFFF" d="M14.07 7.61v5.6q0 .73-.18 1.32-.19.58-.54 1-.36.41-.89.63-.54.23-1.25.23-.32 0-.63-.04t-.65-.13q.01-.24.04-.48l.04-.48q.02-.13.1-.21.09-.08.25-.08.11 0 .27.04.17.04.42.04.34 0 .6-.09.27-.1.44-.32.18-.22.27-.56.09-.35.09-.84V7.61h1.62Z"/>
            </svg>"""),
    NETWORK("""
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                <rect width="100%" height="100%" fill="#FFA500"/>
                <path fill="#FFFFFF" d="M14.24 7.66h1.42v8.68h-.83q-.19 0-.32-.06-.13-.07-.25-.22l-4.53-5.78q.03.4.03.73v5.33H8.34V7.66h.85q.1 0 .18.01.07.01.13.04.05.03.11.08.05.05.12.13l4.55 5.81q-.02-.21-.03-.41-.01-.21-.01-.38V7.66Z"/>
            </svg>"""),
    SECURITY("""
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                <rect width="100%" height="100%" fill="#FFA500"/>
                <path fill="#FFFFFF" d="m14.78 8.475-.4.76q-.08.13-.16.18-.08.06-.2.06-.13 0-.27-.09l-.35-.21q-.21-.11-.48-.21-.27-.09-.64-.09-.34 0-.59.08-.26.08-.43.23-.17.14-.25.34-.08.2-.08.44 0 .31.17.51.17.21.45.35.28.15.64.26.36.11.74.24t.74.3q.36.17.64.43.28.27.45.64.17.38.17.92 0 .59-.2 1.1-.2.52-.59.9-.38.38-.94.6-.56.22-1.28.22-.41 0-.82-.09-.4-.08-.77-.23-.37-.15-.69-.36-.32-.21-.57-.46l.47-.78q.06-.08.16-.14.09-.06.2-.06.15 0 .32.13.18.12.42.27.24.15.56.27.32.13.77.13.69 0 1.07-.33.38-.33.38-.94 0-.34-.18-.56-.17-.21-.45-.36t-.64-.25q-.36-.1-.73-.22t-.73-.29q-.36-.17-.65-.44-.28-.27-.45-.67-.17-.41-.17-1 0-.48.19-.93.19-.45.55-.79.36-.35.89-.56.52-.21 1.21-.21.76 0 1.41.24.65.24 1.11.67Z"/>
            </svg>""");

    private final String svgCode;

    Track(@NotNull final String svgCode) {
        this.svgCode = svgCode;
    }

    @NotNull
    public String getSvgCode() {
        return svgCode;
    }
}
