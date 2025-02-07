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

    private final MastodonLoader mastodonLoader;
    private final String hashtags;
    private final String instance;
    private final String postAPI;
    private final int postLimit;
    private final boolean imagesEnabled;
    private final int imageLimit;

    public MastodonPlugin(@NotNull final MastodonLoader mastodonLoader,
                          @NotNull final AppConfig appConfig) {
        this.mastodonLoader = mastodonLoader;
        final var mastodonConfig = appConfig.mastodon();
        this.hashtags = mastodonConfig.hashtags();
        this.instance = mastodonConfig.instance();
        this.postAPI = mastodonConfig.postAPI();
        this.postLimit = mastodonConfig.postLimit();
        this.imagesEnabled = mastodonConfig.imagesEnabled();
        this.imageLimit = mastodonConfig.imageLimit();
    }

    @Override
    public boolean isEnabled() {
        final var instanceOk = instance != null && !instance.isBlank();
        final var hashtagsOk = hashtags != null && !hashtags.isBlank();
        final var postAPIOk = postAPI != null && !postAPI.isBlank();
        return instanceOk && hashtagsOk && postAPIOk;
    }

    @Override
    public Stream<Post> getPosts() {
        try {
            final var posts = new ArrayList<Post>();

            for (final String hashtag : hashtags.split(",")) {
                if (hashtag.isBlank()) {
                    continue;
                }
                LOGGER.info("Starting download of posts with hashtag '{}' from instance '{}'", hashtag, instance);
                final var jsonPosts = mastodonLoader.getPosts(instance, hashtag.trim(), postAPI, postLimit);
                LOGGER.info("Successfully downloaded {} posts with hashtag '{}' from instance '{}'", jsonPosts.length(), hashtag, instance);

                for (var i = 0; i < jsonPosts.length(); i++) {
                    final var post = jsonPosts.getJSONObject(i);
                    posts.add(createPost(post));
                }
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
        final var date = ZonedDateTime.parse(post.getString("created_at")).toLocalDateTime();
        final var account = post.getJSONObject("account");
        final var author = account.getString("display_name");
        final var avatar = account.getString("avatar");
        final var profile = account.getString("acct");
        final var html = post.getString("content");
        final var isReply = !post.isNull("in_reply_to_id") && !post.getString("in_reply_to_id").isBlank();
        final var isSensitive = post.getBoolean("sensitive");
        final var images = getImages(post.getJSONArray("media_attachments"));
        return new Post(id, date, author, avatar, profile, html, images, isReply, isSensitive);
    }

    @NotNull
    private List<String> getImages(final @NotNull JSONArray mediaAttachments) {
        final List<String> images = new ArrayList<>();
        if (imagesEnabled) {
            for (var i = 0; i < mediaAttachments.length(); i++) {
                final var mediaAttachment = mediaAttachments.getJSONObject(i);
                if ((imageLimit == 0 || images.size() < imageLimit)
                        && mediaAttachment.getString("type").equals("image")) {
                    images.add(mediaAttachment.getString("preview_url"));
                }
            }
        }
        return images;
    }

}
