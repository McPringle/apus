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
import social.bigbone.MastodonClient;
import social.bigbone.api.Pageable;
import social.bigbone.api.entity.Account;
import social.bigbone.api.entity.MediaAttachment;
import social.bigbone.api.entity.Status;
import social.bigbone.api.exception.BigBoneRequestException;
import swiss.fihlon.apus.social.Message;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static social.bigbone.api.method.TimelineMethods.StatusOrigin.LOCAL_AND_REMOTE;

public final class MastodonAPI {

    private final String instance;

    public MastodonAPI(@NotNull final String instance) {
        this.instance = instance;
    }

    public List<Message> getMessages(@NotNull final String hashtag) {
        try {
            final MastodonClient client = new MastodonClient.Builder(instance).build();
            final Pageable<Status> statuses = client.timelines().getTagTimeline(hashtag, LOCAL_AND_REMOTE).execute();
            return statuses.getPart().stream()
                    .map(this::convertToMessage)
                    .sorted()
                    .toList()
                    .reversed();
        } catch (final BigBoneRequestException e) {
            // TODO
        }
        return List.of();
    }

    private Message convertToMessage(@NotNull final Status status) {
        final String id = status.getId();
        final Account account = status.getAccount();
        final Instant instant = status.getCreatedAt().mostPreciseInstantOrNull();
        final LocalDateTime date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        final String author = account == null ? "" : account.getDisplayName();
        final String avatar = account == null ? "" : account.getAvatar();
        final String html = status.getContent();
        final List<String> images = getImages(status.getMediaAttachments());

        return new Message(id, date, author, avatar, html, images);
    }

    private List<String> getImages(@NotNull final List<MediaAttachment> mediaAttachments) {
        final List<String> images = new ArrayList<>();
        for (final MediaAttachment mediaAttachment : mediaAttachments) {
            if (MediaAttachment.MediaType.IMAGE.equals(mediaAttachment.getType())) {
                images.add(mediaAttachment.getUrl());
            }
        }
        return images;
    }

}
