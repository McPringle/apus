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
package swiss.fihlon.apus.configuration;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "apus")
@EnableConfigurationProperties
@SuppressWarnings("checkstyle:DesignForExtension") // Spring configurations can be subclassed by the Spring Framework
public class Configuration {

    private String version;
    private DOAG doag;
    private Mastodon mastodon;

    public String getVersion() {
        return version;
    }

    public void setVersion(@NotNull final String version) {
        this.version = version;
    }

    public DOAG getDoag() {
        return doag;
    }

    public void setDoag(@NotNull final DOAG doag) {
        this.doag = doag;
    }

    public Mastodon getMastodon() {
        return mastodon;
    }

    public void setMastodon(@NotNull final Mastodon mastodon) {
        this.mastodon = mastodon;
    }
}
