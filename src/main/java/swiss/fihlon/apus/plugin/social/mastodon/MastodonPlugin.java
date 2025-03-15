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
public final class MastodonPlugin implements SocialPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(MastodonPlugin.class);

    @SuppressWarnings("LineLength")
    private static final String MASTODON_LOGO = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512">
                <path d="M433 179.1c0-97.2-63.7-125.7-63.7-125.7-62.5-28.7-228.6-28.4-290.5 0 0 0-63.7 28.5-63.7 125.7 0 115.7-6.6 259.4 105.6 289.1 40.5 10.7 75.3 13 103.3 11.4 50.8-2.8 79.3-18.1 79.3-18.1l-1.7-36.9s-36.3 11.4-77.1 10.1c-40.4-1.4-83-4.4-89.6-54a102.5 102.5 0 0 1 -.9-13.9c85.6 20.9 158.7 9.1 178.8 6.7 56.1-6.7 105-41.3 111.2-72.9 9.8-49.8 9-121.5 9-121.5zm-75.1 125.2h-46.6v-114.2c0-49.7-64-51.6-64 6.9v62.5h-46.3V197c0-58.5-64-56.6-64-6.9v114.2H90.2c0-122.1-5.2-147.9 18.4-175 25.9-28.9 79.8-30.8 103.8 6.1l11.6 19.5 11.6-19.5c24.1-37.1 78.1-34.8 103.8-6.1 23.7 27.3 18.4 53 18.4 175z"/>
            </svg>""";

    private final MastodonLoader mastodonLoader;
    private final String instance;
    private final String postAPI;
    private final int postLimit;

    public MastodonPlugin(@NotNull final MastodonLoader mastodonLoader,
                          @NotNull final AppConfig appConfig) {
        this.mastodonLoader = mastodonLoader;
        final var mastodonConfig = appConfig.mastodon();
        this.instance = mastodonConfig.instance();
        this.postAPI = mastodonConfig.postAPI();
        this.postLimit = mastodonConfig.postLimit();
    }

    @Override
    @NotNull
    public String getServiceName() {
        return "Mastodon";
    }

    @Override
    public boolean isEnabled() {
        final var instanceOk = !instance.isBlank();
        final var postAPIOk = !postAPI.isBlank();
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
            LOGGER.info("Starting download of posts with hashtag '{}' from instance '{}'", hashtag, instance);
            final var jsonPosts = mastodonLoader.getPosts(instance, hashtag, postAPI, postLimit);
            LOGGER.info("Successfully downloaded {} posts with hashtag '{}' from instance '{}'", jsonPosts.length(), hashtag, instance);

            final var posts = new ArrayList<Post>();
            for (var i = 0; i < jsonPosts.length(); i++) {
                final var post = jsonPosts.getJSONObject(i);
                posts.add(createPost(post));
            }

            return posts.stream();
        } catch (final MastodonException e) {
            LOGGER.error(e.getMessage(), e);
            return Stream.of();
        }
    }

    @NotNull
    private Post createPost(final @NotNull JSONObject post) {
        final var id = post.getString("id");
        final var date = ZonedDateTime.parse(post.getString("created_at"));
        final var account = post.getJSONObject("account");
        final var author = account.getString("display_name");
        final var avatar = account.getString("avatar");
        final var profile = account.getString("acct");
        final var html = post.getString("content");
        final var isReply = !post.isNull("in_reply_to_id") && !post.getString("in_reply_to_id").isBlank();
        final var isSensitive = post.getBoolean("sensitive");
        final var images = getImages(post.getJSONArray("media_attachments"));
        return new Post(id, date, author, avatar, profile, html, images, isReply, isSensitive, MASTODON_LOGO);
    }

    @NotNull
    private List<String> getImages(final @NotNull JSONArray mediaAttachments) {
        final List<String> images = new ArrayList<>();
        for (var i = 0; i < mediaAttachments.length(); i++) {
            final var mediaAttachment = mediaAttachments.getJSONObject(i);
            if (mediaAttachment.getString("type").equals("image")) {
                images.add(mediaAttachment.getString("preview_url"));
            }
        }
        return images;
    }

}
