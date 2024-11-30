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

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.util.DownloadUtil;

@Service
public final class DefaultBlueSkyLoader implements BlueSkyLoader {

    @Override
    @NotNull
    public JSONArray getPosts(@NotNull final String instance, @NotNull final String hashtag, @NotNull final String postAPI) throws BlueSkyException {
        try {
            final var url = String.format(postAPI, instance, hashtag);
            final var json = DownloadUtil.getString(url);
            final var jsonPosts = new JSONObject(json).getJSONArray("posts");
            if (jsonPosts != null) {
                return jsonPosts;
            }
            throw new NullPointerException(String.format("No posts with hashtag '%s' from BlueSky instance '%s'", hashtag, instance));
        } catch (final Exception e) {
            throw new BlueSkyException(String.format("Unable to load posts with hashtag '%s' from BlueSky instance '%s'", hashtag, instance), e);
        }
    }

}
