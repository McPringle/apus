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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import social.bigbone.api.entity.Account;
import social.bigbone.api.entity.MediaAttachment;
import social.bigbone.api.entity.Status;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.plugin.social.SocialPlugin;
import swiss.fihlon.apus.social.Post;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public final class MastodonPlugin implements SocialPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(MastodonPlugin.class);

    private final MastodonLoader mastodonLoader;
    private final String instance;
    private final String hashtags;
    private final boolean imagesEnabled;
    private final int imageLimit;

    public MastodonPlugin(@NotNull final MastodonLoader mastodonLoader,
                          @NotNull final Configuration configuration) {
        this.mastodonLoader = mastodonLoader;
        final var mastodonConfig = configuration.getMastodon();
        this.instance = mastodonConfig.instance();
        this.hashtags = mastodonConfig.hashtags();
        this.imagesEnabled = mastodonConfig.imagesEnabled();
        this.imageLimit = mastodonConfig.imageLimit();
    }

    @Override
    public boolean isEnabled() {
        return instance != null && !instance.isBlank() && hashtags != null && !hashtags.isBlank();
    }

    @Override
    public Stream<Post> getPosts() {
        try {
            final List<Status> statuses = new ArrayList<>();
            for (final String hashtag : hashtags.split(",")) {
                if (hashtag.isBlank()) {
                    continue;
                }
                LOGGER.info("Starting download of posts with hashtag '{}' from instance '{}'", hashtag, instance);
                statuses.addAll(mastodonLoader.getStatuses(instance, hashtag.trim()));
                LOGGER.info("Successfully downloaded {} posts with hashtag '{}' from instance '{}'", statuses.size(), hashtag, instance);
            }
            return statuses.stream()
                    .map(this::convertToPost)
                    .distinct()
                    .sorted();
        } catch (final MastodonException e) {
            LOGGER.error(e.getMessage(), e);
            return Stream.of();
        }
    }

    private Post convertToPost(@NotNull final Status status) {
        final String id = status.getId();
        final Account account = status.getAccount();
        final Instant instant = status.getCreatedAt().mostPreciseOrFallback(Instant.MIN);
        final LocalDateTime date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        final String author = account == null ? "" : account.getDisplayName();
        final String avatar = account == null ? "" : account.getAvatar();
        final String profile = account == null ? "" : getProfile(account);
        final String html = status.getContent();
        final List<String> images = imagesEnabled ? getImages(status.getMediaAttachments()) : List.of();
        final String inReplyToId = status.getInReplyToId();
        final boolean isReply = inReplyToId != null && !inReplyToId.isBlank();
        final boolean isSensitive = status.isSensitive();

        return new Post(id, date, author, avatar, profile, html, images, isReply, isSensitive);
    }

    @NotNull
    private String getProfile(@NotNull final Account account) {
        final var profile = account.getAcct();
        return profile.contains("@") ? profile : profile.concat("@").concat(instance);
    }

    private List<String> getImages(@NotNull final List<MediaAttachment> mediaAttachments) {
        final List<String> images = new ArrayList<>();
        for (final MediaAttachment mediaAttachment : mediaAttachments) {
            if (imageLimit == 0 || images.size() < imageLimit
                    && MediaAttachment.MediaType.IMAGE.equals(mediaAttachment.getType())) {
                images.add(mediaAttachment.getUrl());
            }
        }
        return images;
    }

}
