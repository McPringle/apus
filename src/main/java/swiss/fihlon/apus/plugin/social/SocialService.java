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
package swiss.fihlon.apus.plugin.social;

import jakarta.annotation.PreDestroy;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.social.Post;
import swiss.fihlon.apus.util.HtmlUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
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
    private final SocialPlugin socialPlugin;
    private final int filterLength;
    private final boolean filterReplies;
    private final boolean filterSensitive;
    private final List<String> filterWords;
    private final Set<String> hiddenPosts = new HashSet<>();
    private final Set<String> blockedProfiles = new HashSet<>();
    private List<Post> posts = List.of();

    public SocialService(@NotNull final TaskScheduler taskScheduler,
                         @NotNull final Configuration configuration,
                         @NotNull final SocialPlugin socialPlugin) {
        this.socialPlugin = socialPlugin;
        filterLength = configuration.getFilter().length();
        filterReplies = configuration.getFilter().replies();
        filterSensitive = configuration.getFilter().sensitive();
        filterWords = configuration.getFilter().words().stream()
                .map(filterWord -> filterWord.toLowerCase(DEFAULT_LOCALE).trim())
                .toList();
        loadHiddenPostIds();
        loadBlockedProfiles();
        if (socialPlugin.isEnabled()) {
            updatePosts();
            updateScheduler = taskScheduler.scheduleAtFixedRate(this::updatePosts, UPDATE_FREQUENCY);
        } else {
            LOGGER.warn("No social plugin is enabled. No posts will be displayed.");
            updateScheduler = null;
        }
    }

    @PreDestroy
    public void stopUpdateScheduler() {
        updateScheduler.cancel(true);
    }

    private void updatePosts() {
        final var newPosts = socialPlugin.getPosts().stream()
                .filter(post -> !hiddenPosts.contains(post.id()))
                .filter(post -> !blockedProfiles.contains(post.profile()))
                .filter(post -> !filterSensitive || !post.isSensitive())
                .filter(post -> !filterReplies || !post.isReply())
                .filter(post -> filterLength <= 0 || HtmlUtil.extractText(post.html()).length() <= filterLength)
                .filter(this::checkWordFilter)
                .sorted()
                .toList();
        synchronized (this) {
            posts = new ArrayList<>(newPosts);
        }
    }

    private boolean checkWordFilter(@NotNull final Post post) {
        final String postText = Jsoup.parse(post.html()).text().toLowerCase(DEFAULT_LOCALE);
        for (final String filterWord : filterWords) {
            if (postText.contains(filterWord)) {
                return false;
            }
        }
        return true;
    }

    public List<Post> getPosts(final int limit) {
        synchronized (this) {
            if (limit <= 0 || posts.isEmpty()) {
                return List.copyOf(posts);
            }
            final int toIndex = Math.min(limit, posts.size());
            return List.copyOf(posts.subList(0, toIndex));
        }
    }

    public void hidePost(@NotNull final Post post) {
        LOGGER.warn("Hiding post (id={}, profile={}, author={})",
                post.id(), post.profile(), post.author());
        posts.remove(post);
        hiddenPosts.add(post.id());
        saveHiddenPostIds();
    }

    public void hideProfile(@NotNull final Post post) {
        LOGGER.warn("Hide profile (id={}, profile={}, author={})",
                post.id(), post.profile(), post.author());
        posts.remove(post);
        blockedProfiles.add(post.profile());
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

    private void saveHiddenPostIds() {
        final var filePath = getConfigDir().resolve("hiddenPosts");
        try {
            Files.writeString(filePath, String.join("\n", hiddenPosts));
        } catch (final IOException e) {
            LOGGER.error("Unable to save hidden posts to file '{}': {}", filePath, e.getMessage());
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

    private void loadHiddenPostIds() {
        final var filePath = getConfigDir().resolve("hiddenPosts");
        if (filePath.toFile().exists()) {
            try {
                hiddenPosts.addAll(Files.readAllLines(filePath));
            } catch (IOException e) {
                LOGGER.error("Unable to load hidden posts from file '{}': {}", filePath, e.getMessage());
            }
        } else {
            LOGGER.info("No previously saved hidden posts found.");
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
