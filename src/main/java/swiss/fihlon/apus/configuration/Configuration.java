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
import swiss.fihlon.apus.plugin.event.EventConfig;
import swiss.fihlon.apus.plugin.event.demo.DemoConfig;
import swiss.fihlon.apus.plugin.event.doag.DoagConfig;
import swiss.fihlon.apus.plugin.event.sessionize.SessionizeConfig;
import swiss.fihlon.apus.plugin.social.mastodon.MastodonConfig;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "apus")
@EnableConfigurationProperties
@SuppressWarnings("checkstyle:DesignForExtension") // Spring configurations can be subclassed by the Spring Framework
public class Configuration {

    // Core Configs
    private String version;
    private AdminConfig admin;
    private EventConfig event;
    private FilterConfig filter;

    // Event Plugin Configs
    private DemoConfig demo;
    private DoagConfig doag;
    private SessionizeConfig sessionize;

    // Social Plugin Configs
    private MastodonConfig mastodon;

    //////////////////
    // Core Configs //
    //////////////////

    public String getVersion() {
        return version;
    }

    public void setVersion(@NotNull final String version) {
        this.version = version;
    }

    public AdminConfig getAdmin() {
        return admin;
    }

    public void setAdmin(@NotNull final AdminConfig admin) {
        this.admin = admin;
    }

    public FilterConfig getFilter() {
        return filter;
    }

    public void setFilter(@NotNull final FilterConfig filter) {
        this.filter = filter;
    }

    public EventConfig getEvent() {
        return this.event;
    }

    public void setEvent(@NotNull final EventConfig event) {
        this.event = event;
    }

    //////////////////////////
    // Event Plugin Configs //
    //////////////////////////

    public DemoConfig getDemo() {
        return demo;
    }

    public void setDemo(@NotNull final DemoConfig demo) {
        this.demo = demo;
    }

    public DoagConfig getDoag() {
        return doag;
    }

    public void setDoag(@NotNull final DoagConfig doag) {
        this.doag = doag;
    }

    public SessionizeConfig getSessionize() {
        return sessionize;
    }

    public void setSessionize(@NotNull final SessionizeConfig sessionize) {
        this.sessionize = sessionize;
    }

    ///////////////////////////
    // Social Plugin Configs //
    ///////////////////////////

    public MastodonConfig getMastodon() {
        return mastodon;
    }

    public void setMastodon(@NotNull final MastodonConfig mastodon) {
        this.mastodon = mastodon;
    }

}
