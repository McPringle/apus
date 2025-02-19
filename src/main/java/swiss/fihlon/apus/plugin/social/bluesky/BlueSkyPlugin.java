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
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.plugin.social.SocialPlugin;
import swiss.fihlon.apus.social.Post;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public final class BlueSkyPlugin implements SocialPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlueSkyPlugin.class);

    private final BlueSkyLoader blueSkyLoader;
    private final String instance;
    private final String postAPI;
    private final int postLimit;

    public BlueSkyPlugin(@NotNull final BlueSkyLoader blueSkyLoader,
                         @NotNull final AppConfig appConfig) {
        this.blueSkyLoader = blueSkyLoader;
        final var blueSkyConfig = appConfig.blueSky();
        this.instance = blueSkyConfig.instance();
        this.postAPI = blueSkyConfig.postAPI();
        this.postLimit = blueSkyConfig.postLimit();
    }

    @Override
    @NotNull
    public String getServiceName() {
        return "BlueSky";
    }

    @Override
    public boolean isEnabled() {
        final var instanceOk = instance != null && !instance.isBlank();
        final var postAPIOk = postAPI != null && !postAPI.isBlank();
        return instanceOk && postAPIOk;
    }

    @Override
    @NotNull
    public Stream<Post> getPosts(@NotNull final List<String> hashtags) {
        try {
            final var posts = new ArrayList<Post>();

            for (final String hashtag : hashtags) {
                LOGGER.info("Starting download of posts with hashtag '{}' from instance '{}'", hashtag, instance);
                final var jsonPosts = blueSkyLoader.getPosts(instance, hashtag.trim(), postAPI, postLimit);
                LOGGER.info("Successfully downloaded {} posts with hashtag '{}' from instance '{}'", jsonPosts.length(), hashtag, instance);

                for (var i = 0; i < jsonPosts.length(); i++) {
                    final var post = jsonPosts.getJSONObject(i);
                    if (hasTag(post, hashtag)) {
                        posts.add(createPost(post));
                    }
                }
            }

            return posts.stream();
        } catch (final BlueSkyException e) {
            LOGGER.error(e.getMessage(), e);
            return Stream.of();
        }
    }

    private boolean hasTag(final @NotNull JSONObject post, @NotNull final String hashtag) {
        final var postRecord = post.getJSONObject("record");
        if (postRecord.has("facets")) {
            final var facets = postRecord.getJSONArray("facets");
            for (var i = 0; i < facets.length(); i++) {
                final var facet = facets.getJSONObject(i);
                final var features = facet.getJSONArray("features");
                for (var j = 0; j < features.length(); j++) {
                    final var feature = features.getJSONObject(j);
                    final var type = feature.getString("$type");
                    if (type.equals("app.bsky.richtext.facet#tag")) {
                        final var tag = feature.getString("tag");
                        if (tag.equalsIgnoreCase(hashtag)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @NotNull
    private Post createPost(final @NotNull JSONObject post) {
        final var id = post.getString("uri");

        final var author = post.getJSONObject("author");
        final var displayName = author.getString("displayName");
        final var avatar = author.getString("avatar");
        final var handle = author.getString("handle");

        final var postRecord = post.getJSONObject("record");
        final var text = postRecord.getString("text");
        final var isReply = postRecord.has("reply");
        final var date = ZonedDateTime.parse(postRecord.getString("createdAt")).toLocalDateTime();

        final var imageLinks = new ArrayList<String>();
        if (post.has("embed")) {
            final var embed = post.getJSONObject("embed");
            final var type = embed.getString("$type");
            if (type.equals("app.bsky.embed.images#view")) {
                final var images = embed.getJSONArray("images");
                for (var i = 0; i < images.length(); i++) {
                    final var image = images.getJSONObject(i);
                    imageLinks.add(image.getString("thumb"));
                }
            }
        }

        return new Post(id, date, displayName, avatar, handle, text, imageLinks, isReply, false);
    }
}
