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
import org.springframework.stereotype.Service;
import social.bigbone.MastodonClient;
import social.bigbone.api.Range;
import social.bigbone.api.entity.Status;
import social.bigbone.api.exception.BigBoneRequestException;

import java.util.List;

import static social.bigbone.api.method.TimelineMethods.StatusOrigin.LOCAL_AND_REMOTE;

@Service
public final class DefaultMastodonLoader implements MastodonLoader {

    @Override
    @NotNull public List<Status> getStatuses(@NotNull final String instance, @NotNull final String hashtag) throws BigBoneRequestException {
        final MastodonClient client = new MastodonClient.Builder(instance).build();
        final Range range = new Range(null, null, null, 100);
        return client.timelines().getTagTimeline(hashtag, LOCAL_AND_REMOTE, range).execute().getPart();
    }

}
