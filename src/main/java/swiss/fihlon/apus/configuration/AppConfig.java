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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import swiss.fihlon.apus.plugin.event.EventConfig;
import swiss.fihlon.apus.plugin.event.doag.DoagConfig;
import swiss.fihlon.apus.plugin.event.jfs.JavaForumStuttgartConfig;
import swiss.fihlon.apus.plugin.event.sessionize.SessionizeConfig;
import swiss.fihlon.apus.plugin.social.bluesky.BlueSkyConfig;
import swiss.fihlon.apus.plugin.social.mastodon.MastodonConfig;

@ConfigurationProperties(prefix = "apus")
public record AppConfig(String version, AdminConfig admin, CustomConfig custom, EventConfig event, SocialConfig social, FilterConfig filter,
                        DoagConfig doag, JavaForumStuttgartConfig jfs, SessionizeConfig sessionize,
                        BlueSkyConfig blueSky, MastodonConfig mastodon) {

    @ConstructorBinding
    @SuppressWarnings({"java:S1186", "java:S6207"})
    // needed to add the `@ConstructorBinding` annotation
    public AppConfig { }

}
