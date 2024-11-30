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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.plugin.social.SocialPlugin;
import swiss.fihlon.apus.social.Post;
import swiss.fihlon.apus.util.DownloadUtil;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;

@Service
public final class BlueSkyPlugin implements SocialPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlueSkyPlugin.class);

    private final String hashtags;
    private final String instance;
    private final String postAPI;

    public BlueSkyPlugin(@NotNull final Configuration configuration) {
        final var blueSkyConfig = configuration.getBlueSky();
        hashtags = blueSkyConfig.hashtags();
        instance = blueSkyConfig.instance();
        postAPI = blueSkyConfig.postAPI();
    }

    @Override
    public boolean isEnabled() {
        return instance != null && !instance.isBlank()
                && hashtags != null && !hashtags.isBlank()
                && postAPI != null && !postAPI.isBlank();
    }

    @Override
    public Stream<Post> getPosts() {
        var url = String.format(postAPI, instance, hashtags);
        ArrayList<Post> posts = new ArrayList<>();
        try {
            var json = DownloadUtil.getString(url);
            var jsonPosts = new JSONObject(json).getJSONArray("posts");
            for (var i = 0; i < jsonPosts.length(); i++) {
                var post = jsonPosts.getJSONObject(i);
                posts.add(createPost(post));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return posts.stream();
    }

    private Post createPost(final JSONObject post) {
        var id = post.getString("uri");

        var author = post.getJSONObject("author");
        var displayName = author.getString("displayName");
        var avatar = author.getString("avatar");
        var handle = author.getString("handle");

        var record = post.getJSONObject("record");
        var text = record.getString("text");
        var date = ZonedDateTime.parse(record.getString("createdAt")).toLocalDateTime();

        var imageLinks = new ArrayList<String>();
        if (post.has("embed")) {
            var embed = post.getJSONObject("embed");
            if (embed.getString("$type").equals("app.bsky.embed.images#view")) {
                var images = embed.getJSONArray("images");
                for (var i = 0; i < images.length(); i++) {
                    imageLinks.add(images.getJSONObject(i).getString("thumb"));
                }
            }
        }
        return new Post(id, date, displayName, avatar, handle, text, imageLinks, false, false);
    }
}
