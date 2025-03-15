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
package swiss.fihlon.apus;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.util.List;

public final class MemoryAppender extends ListAppender<ILoggingEvent> {

    public List<ILoggingEvent> searchMessages(final String string, final Level level) {
        return this.list.stream()
                .filter(event -> event.getMessage().contains(string) && event.getLevel().equals(level))
                .toList();
    }

    public List<ILoggingEvent> searchFormattedMessages(final String string, final Level level) {
        return this.list.stream()
                .filter(event -> event.getFormattedMessage().contains(string) && event.getLevel().equals(level))
                .toList();
    }

}
