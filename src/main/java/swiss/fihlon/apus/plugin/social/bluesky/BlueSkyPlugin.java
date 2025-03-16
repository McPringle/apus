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

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(BlueSkyPlugin.class);

    @SuppressWarnings("LineLength")
    private static final @NotNull String BLUESKY_LOGO = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512">
                <path d="M111.8 62.2C170.2 105.9 233 194.7 256 242.4c23-47.6 85.8-136.4 144.2-180.2c42.1-31.6 110.3-56 110.3 21.8c0 15.5-8.9 130.5-14.1 149.2C478.2 298 412 314.6 353.1 304.5c102.9 17.5 129.1 75.5 72.5 133.5c-107.4 110.2-154.3-27.6-166.3-62.9l0 0c-1.7-4.9-2.6-7.8-3.3-7.8s-1.6 3-3.3 7.8l0 0c-12 35.3-59 173.1-166.3 62.9c-56.5-58-30.4-116 72.5-133.5C100 314.6 33.8 298 15.7 233.1C10.4 214.4 1.5 99.4 1.5 83.9c0-77.8 68.2-53.4 110.3-21.8z"/>
            </svg>""";

    private final @NotNull BlueSkyLoader blueSkyLoader;
    private final @NotNull String instance;
    private final @NotNull String hashtagUrl;
    private final @NotNull String mentionsUrl;
    private final @NotNull String profile;
    private final int postLimit;

    public BlueSkyPlugin(@NotNull final BlueSkyLoader blueSkyLoader,
                         @NotNull final AppConfig appConfig) {
        this.blueSkyLoader = blueSkyLoader;
        final var blueSkyConfig = appConfig.blueSky();
        this.instance = blueSkyConfig.instance();
        this.hashtagUrl = blueSkyConfig.hashtagUrl();
        this.mentionsUrl = blueSkyConfig.mentionsUrl();
        this.profile = blueSkyConfig.profile();
        this.postLimit = blueSkyConfig.postLimit();
    }

    @Override
    public @NotNull String getServiceName() {
        return "BlueSky";
    }

    @Override
    public boolean isEnabled() {
        final var instanceOk = !instance.isBlank();
        final var hashtagUrlOk = !hashtagUrl.isBlank();
        return instanceOk && hashtagUrlOk;
    }

    @Override
    public @NotNull Stream<@NotNull Post> getPosts(final @NotNull List<@NotNull String> hashtags) {
        return Stream.concat(
                        hashtags.parallelStream()
                                .filter(hashtag -> !hashtag.isBlank())
                                .flatMap(this::getPostsWithHashtag),
                        getPostsWithMention()
                )
                .distinct();
    }

    private @NotNull Stream<@NotNull Post> getPostsWithHashtag(final @NotNull String hashtag) {
        try {
            final var posts = new ArrayList<Post>();

            LOGGER.info("Starting download of posts with hashtag '{}' from instance '{}'", hashtag, instance);
            final var jsonPosts = blueSkyLoader.getPostsWithHashtag(instance, hashtag, hashtagUrl, postLimit);
            LOGGER.info("Successfully downloaded {} posts with hashtag '{}' from instance '{}'", jsonPosts.length(), hashtag, instance);

            for (var i = 0; i < jsonPosts.length(); i++) {
                final var post = jsonPosts.getJSONObject(i);
                posts.add(createPost(post));
            }

            return posts.stream();
        } catch (final BlueSkyException e) {
            LOGGER.error(e.getMessage(), e);
            return Stream.of();
        }
    }

    private @NotNull Stream<@NotNull Post> getPostsWithMention() {
        if (mentionsUrl.isBlank() || profile.isBlank()) {
            return Stream.of();
        }

        try {
            final var posts = new ArrayList<Post>();

            LOGGER.info("Starting download of posts with mention '{}' from instance '{}'", profile, instance);
            final var jsonPosts = blueSkyLoader.getPostsWithMention(instance, profile, mentionsUrl, postLimit);
            LOGGER.info("Successfully downloaded {} posts with mention '{}' from instance '{}'", jsonPosts.length(), profile, instance);

            for (var i = 0; i < jsonPosts.length(); i++) {
                final var post = jsonPosts.getJSONObject(i);
                posts.add(createPost(post));
            }

            return posts.stream();
        } catch (final BlueSkyException e) {
            LOGGER.error(e.getMessage(), e);
            return Stream.of();
        }
    }

    private @NotNull Post createPost(final @NotNull JSONObject post) {
        final var id = post.getString("uri");

        final var author = post.getJSONObject("author");
        final var handle = author.getString("handle");

        final var avatar = getStringOrDefault(author, "avatar", "");
        final var displayName = getStringOrDefault(author, "displayName", handle);

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

    private @NotNull String getStringOrDefault(@NotNull final JSONObject jsonObject, @NotNull final String key, @NotNull final String defaultValue) {
        if (jsonObject.has(key) && !jsonObject.isNull(key)) {
            final var value = jsonObject.getString(key);
            if (!value.isBlank()) {
                return value;
            }
        }

        return defaultValue;
    }
}
