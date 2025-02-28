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

    @SuppressWarnings("LineLength")
    private static final String BLUESKY_LOGO = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512">
                <path d="M111.8 62.2C170.2 105.9 233 194.7 256 242.4c23-47.6 85.8-136.4 144.2-180.2c42.1-31.6 110.3-56 110.3 21.8c0 15.5-8.9 130.5-14.1 149.2C478.2 298 412 314.6 353.1 304.5c102.9 17.5 129.1 75.5 72.5 133.5c-107.4 110.2-154.3-27.6-166.3-62.9l0 0c-1.7-4.9-2.6-7.8-3.3-7.8s-1.6 3-3.3 7.8l0 0c-12 35.3-59 173.1-166.3 62.9c-56.5-58-30.4-116 72.5-133.5C100 314.6 33.8 298 15.7 233.1C10.4 214.4 1.5 99.4 1.5 83.9c0-77.8 68.2-53.4 110.3-21.8z"/>
            </svg>""";

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
        return hashtags.parallelStream()
                .filter(hashtag -> !hashtag.isBlank())
                .flatMap(this::getPosts);
    }

    @NotNull
    public Stream<Post> getPosts(@NotNull final String hashtag) {
        try {
            final var posts = new ArrayList<Post>();

            LOGGER.info("Starting download of posts with hashtag '{}' from instance '{}'", hashtag, instance);
            final var jsonPosts = blueSkyLoader.getPosts(instance, hashtag.trim(), postAPI, postLimit);
            LOGGER.info("Successfully downloaded {} posts with hashtag '{}' from instance '{}'", jsonPosts.length(), hashtag, instance);

            for (var i = 0; i < jsonPosts.length(); i++) {
                final var post = jsonPosts.getJSONObject(i);
                if (hasTag(post, hashtag)) {
                    posts.add(createPost(post));
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
        final var date = ZonedDateTime.parse(postRecord.getString("createdAt"));

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

        return new Post(id, date, displayName, avatar, handle, text, imageLinks, isReply, false, BLUESKY_LOGO);
    }
}
