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
package swiss.fihlon.apus.service;

import jakarta.annotation.PreDestroy;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.social.Message;
import swiss.fihlon.apus.social.mastodon.MastodonAPI;
import swiss.fihlon.apus.util.HtmlUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

@Service
public final class SocialService {

    private static final Duration UPDATE_FREQUENCY = Duration.ofSeconds(30);
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static final Logger LOGGER = LoggerFactory.getLogger(SocialService.class);

    private final ScheduledFuture<?> updateScheduler;
    private final MastodonAPI mastodonAPI;
    private final int filterLength;
    private final boolean filterReplies;
    private final boolean filterSensitive;
    private final List<String> filterWords;
    private final Set<String> hiddenMessages = new HashSet<>();
    private final Set<String> blockedProfiles = new HashSet<>();
    private List<Message> messages = List.of();

    public SocialService(@NotNull final TaskScheduler taskScheduler,
                         @NotNull final Configuration configuration) {
        mastodonAPI = new MastodonAPI(configuration);
        filterLength = configuration.getFilter().length();
        filterReplies = configuration.getFilter().replies();
        filterSensitive = configuration.getFilter().sensitive();
        filterWords = configuration.getFilter().words().stream()
                .map(filterWord -> filterWord.toLowerCase(DEFAULT_LOCALE).trim())
                .toList();
        loadHiddenMessageIds();
        loadBlockedProfiles();
        updateMessages();
        updateScheduler = taskScheduler.scheduleAtFixedRate(this::updateMessages, UPDATE_FREQUENCY);
    }

    @PreDestroy
    public void stopUpdateScheduler() {
        updateScheduler.cancel(true);
    }

    private void updateMessages() {
        final var newMessages = mastodonAPI.getMessages().stream()
                .filter(message -> !hiddenMessages.contains(message.id()))
                .filter(message -> !blockedProfiles.contains(message.profile()))
                .filter(message -> !filterSensitive || !message.isSensitive())
                .filter(message -> !filterReplies || !message.isReply())
                .filter(message -> filterLength <= 0 || HtmlUtil.extractText(message.html()).length() <= filterLength)
                .filter(this::checkWordFilter)
                .toList();
        synchronized (this) {
            messages = new ArrayList<>(newMessages);
        }
    }

    private boolean checkWordFilter(@NotNull final Message message) {
        final String messageText = Jsoup.parse(message.html()).text().toLowerCase(DEFAULT_LOCALE);
        for (final String filterWord : filterWords) {
            if (messageText.contains(filterWord)) {
                return false;
            }
        }
        return true;
    }

    public List<Message> getMessages(final int limit) {
        synchronized (this) {
            if (limit <= 0 || messages.isEmpty()) {
                return Collections.unmodifiableList(messages);
            }
            final int toIndex = limit < messages.size() ? limit : messages.size() - 1;
            return Collections.unmodifiableList(messages.subList(0, toIndex));
        }
    }

    public void hideMessage(@NotNull final Message message) {
        LOGGER.warn("Hiding message (id={}, profile={}, author={})",
                message.id(), message.profile(), message.author());
        messages.remove(message);
        hiddenMessages.add(message.id());
        saveHiddenMessageIds();
    }

    public void hideProfile(@NotNull final Message message) {
        LOGGER.warn("Hide profile (id={}, profile={}, author={})",
                message.id(), message.profile(), message.author());
        messages.remove(message);
        blockedProfiles.add(message.profile());
        saveBlockedProfiles();
    }

    private Path getConfigDir() {
        final Path configDir = Path.of(System.getProperty("user.home"), ".apus");
        if (!configDir.toFile().exists()) {
            try {
                Files.createDirectories(configDir);
            } catch (final IOException e) {
                LOGGER.error("Unable to create configuration directory {}: {}", configDir, e.getMessage());
            }
        }
        return configDir;
    }

    private void saveHiddenMessageIds() {
        final var filePath = getConfigDir().resolve("hiddenMessages");
        try {
            Files.writeString(filePath, String.join("\n", hiddenMessages));
        } catch (final IOException e) {
            LOGGER.error("Unable to save hidden messages to file '{}': {}", filePath, e.getMessage());
        }
    }

    private void saveBlockedProfiles() {
        final var filePath = getConfigDir().resolve("blockedProfiles");
        try {
            Files.writeString(filePath, String.join("\n", blockedProfiles));
        } catch (final IOException e) {
            LOGGER.error("Unable to save blocked profiles to file '{}': {}", filePath, e.getMessage());
        }
    }

    private void loadHiddenMessageIds() {
        final var filePath = getConfigDir().resolve("hiddenMessages");
        if (filePath.toFile().exists()) {
            try {
                hiddenMessages.addAll(Files.readAllLines(filePath));
            } catch (IOException e) {
                LOGGER.error("Unable to load hidden messages from file '{}': {}", filePath, e.getMessage());
            }
        } else {
            LOGGER.info("No previously saved hidden messages found.");
        }
    }

    private void loadBlockedProfiles() {
        final var filePath = getConfigDir().resolve("blockedProfiles");
        if (filePath.toFile().exists()) {
            try {
                blockedProfiles.addAll(Files.readAllLines(filePath));
            } catch (IOException e) {
                LOGGER.error("Unable to load blocked profiles from file '{}': {}", filePath, e.getMessage());
            }
        } else {
            LOGGER.info("No previously saved blocked profiles found.");
        }
    }
}
