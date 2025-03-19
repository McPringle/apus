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
package swiss.fihlon.apus.plugin.social.mastodon;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.util.DownloadUtil;
import swiss.fihlon.apus.util.TemplateUtil;

import java.util.Map;

@Service
public final class DefaultMastodonLoader implements MastodonLoader {

    @Override
    public @NotNull JSONArray getPosts(final @NotNull String instance,
                                       final @NotNull String hashtag,
                                       final @NotNull String postAPI,
                                       final int limit)
            throws MastodonException {
        try {
            final var url = TemplateUtil.replaceVariables(
                    postAPI, Map.of("instance", instance, "hashtag", hashtag, "limit", Integer.toString(limit)));
            final var json = DownloadUtil.getString(url);
            return new JSONArray(json);
        } catch (final Exception e) {
            throw new MastodonException(String.format("Unable to load posts with hashtag '%s' from Mastodon instance '%s'", hashtag, instance), e);
        }
    }

    @Override
    @SuppressWarnings("java:S2142")
    public @NotNull JSONArray getNotifications(final @NotNull String instance,
                                               final @NotNull String notificationAPI,
                                               final @NotNull String accessToken,
                                               final int limit)
            throws MastodonException {
        try {
            final var url = TemplateUtil.replaceVariables(
                    notificationAPI, Map.of("instance", instance, "limit", Integer.toString(limit)));
            final var json = DownloadUtil.getString(url, accessToken);
            return new JSONArray(json);
        } catch (final Exception e) {
            throw new MastodonException(String.format("Unable to load notifications from Mastodon instance '%s'",  instance), e);
        }
    }

}
