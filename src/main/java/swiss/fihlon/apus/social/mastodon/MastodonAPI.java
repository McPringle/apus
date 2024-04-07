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
package swiss.fihlon.apus.social.mastodon;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import social.bigbone.MastodonClient;
import social.bigbone.api.Pageable;
import social.bigbone.api.entity.Account;
import social.bigbone.api.entity.MediaAttachment;
import social.bigbone.api.entity.Status;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.social.Message;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static social.bigbone.api.method.TimelineMethods.StatusOrigin.LOCAL_AND_REMOTE;

public final class MastodonAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(MastodonAPI.class);

    private final String instance;
    private final String hashtag;
    private final int imageLimit;

    public MastodonAPI(@NotNull final Configuration configuration) {
        final var mastodonConfig = configuration.getMastodon();
        this.instance = mastodonConfig.instance();
        this.hashtag = mastodonConfig.hashtag();
        this.imageLimit = mastodonConfig.imageLimit();
    }

    public List<Message> getMessages() {
        try {
            LOGGER.info("Starting download of messages with hashtag '{}' from instance '{}'", hashtag, instance);
            final MastodonClient client = new MastodonClient.Builder(instance).build();
            final Pageable<Status> statuses = client.timelines().getTagTimeline(hashtag, LOCAL_AND_REMOTE).execute();
            final List<Message> messages = statuses.getPart().stream()
                    .map(this::convertToMessage)
                    .sorted()
                    .toList()
                    .reversed();
            LOGGER.info("Successfully downloaded {} messages with hashtag '{}' from instance '{}'", messages.size(), hashtag, instance);
            return messages;
        } catch (final Exception e) {
            LOGGER.error("Unable to load statuses with hashtag '{}' from Mastodon instance '{}': {}",
                    hashtag, instance, e.getMessage());
            return List.of();
        }
    }

    private Message convertToMessage(@NotNull final Status status) {
        final String id = status.getId();
        final Account account = status.getAccount();
        final Instant instant = status.getCreatedAt().mostPreciseOrFallback(Instant.MIN);
        final LocalDateTime date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        final String author = account == null ? "" : account.getDisplayName();
        final String avatar = account == null ? "" : account.getAvatar();
        final String profile = account == null ? "" : getProfile(account);
        final String html = status.getContent();
        final List<String> images = getImages(status.getMediaAttachments());
        final String inReplyToId = status.getInReplyToId();
        final boolean isReply = inReplyToId != null && !inReplyToId.isBlank();
        final boolean isSensitive = status.isSensitive();

        return new Message(id, date, author, avatar, profile, html, images, isReply, isSensitive);
    }

    @NotNull
    private String getProfile(@NotNull final Account account) {
        final var profile = account.getAcct();
        return profile.contains("@") ? profile : profile.concat("@").concat(instance);
    }

    private List<String> getImages(@NotNull final List<MediaAttachment> mediaAttachments) {
        final List<String> images = new ArrayList<>();
        for (final MediaAttachment mediaAttachment : mediaAttachments) {
            if (images.size() < imageLimit
                    && MediaAttachment.MediaType.IMAGE.equals(mediaAttachment.getType())) {
                images.add(mediaAttachment.getUrl());
            }
        }
        return images;
    }

}
