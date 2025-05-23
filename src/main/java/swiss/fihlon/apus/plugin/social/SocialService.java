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
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.configuration.AppConfig;
import swiss.fihlon.apus.plugin.social.demo.SocialDemoPlugin;
import swiss.fihlon.apus.social.Post;
import swiss.fihlon.apus.util.HtmlUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public final class SocialService {

    private static final @NotNull Duration UPDATE_FREQUENCY = Duration.ofSeconds(30);
    private static final int MAX_POSTS = 50;
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(SocialService.class);

    private final @Nullable ScheduledFuture<?> updateScheduler;
    private final @NotNull List<@NotNull String> hashtags;
    private final int filterLength;
    private final boolean filterReplies;
    private final boolean filterSensitive;
    private final @NotNull List<@NotNull String> filterWords;
    private final boolean imagesEnabled;
    private final int imageLimit;
    private final @NotNull Set<@NotNull String> hiddenPosts = new HashSet<>();
    private final @NotNull Set<@NotNull String> blockedProfiles = new HashSet<>();
    private final @NotNull Map<@NotNull SocialPlugin, @NotNull List<@NotNull Post>> postsByPlugin = new HashMap<>();

    public SocialService(final @NotNull TaskScheduler taskScheduler,
                         final @NotNull AppConfig appConfig,
                         final @NotNull List<@NotNull SocialPlugin> socialPlugins) {
        final var demoMode = appConfig.demoMode();
        hashtags = Arrays.stream(appConfig.social().hashtags().split(","))
                .filter(hashtag -> !hashtag.isBlank())
                .map(String::trim)
                .toList();
        filterLength = appConfig.social().filter().length();
        filterReplies = appConfig.social().filter().replies();
        filterSensitive = appConfig.social().filter().sensitive();
        filterWords = appConfig.social().filter().words().stream()
                .map(String::toLowerCase)
                .map(String::trim)
                .toList();
        loadHiddenPostIds();
        loadBlockedProfiles();
        imagesEnabled = appConfig.social().imagesEnabled();
        imageLimit = appConfig.social().imageLimit();

        if (demoMode) {
            postsByPlugin.put(new SocialDemoPlugin(appConfig), List.of());
        } else {
            socialPlugins.stream()
                    .filter(SocialPlugin::isEnabled)
                    .forEach(plugin -> postsByPlugin.put(plugin, List.of()));
        }

        if (!hashtags.isEmpty() && !postsByPlugin.isEmpty()) {
            updatePosts();
            final var startTime = Instant.now().plus(UPDATE_FREQUENCY);
            updateScheduler = taskScheduler.scheduleAtFixedRate(this::updatePosts, startTime, UPDATE_FREQUENCY);
        } else {
            LOGGER.warn("No social plugin is enabled. No posts will be displayed.");
            updateScheduler = null;
        }
    }

    public @NotNull Stream<@NotNull String> getServiceNames() {
        synchronized (postsByPlugin) {
            return postsByPlugin.keySet().stream()
                    .filter(SocialPlugin::isEnabled)
                    .map(SocialPlugin::getServiceName);
        }
    }

    @PreDestroy
    public void stopUpdateScheduler() {
        if (updateScheduler != null) {
            updateScheduler.cancel(true);
        }
    }

    private void updatePosts() {
        final var futures = postsByPlugin.keySet().stream()
                .map(socialPlugin -> CompletableFuture.runAsync(() -> {
                    try {
                        final var posts = socialPlugin.getPosts(hashtags)
                                .filter(post -> !hiddenPosts.contains(post.id()))
                                .filter(post -> !blockedProfiles.contains(post.profile()))
                                .filter(post -> !filterSensitive || !post.isSensitive())
                                .filter(post -> !filterReplies || !post.isReply())
                                .filter(post -> filterLength <= 0 || HtmlUtil.extractText(post.html()).length() <= filterLength)
                                .filter(this::checkWordFilter)
                                .sorted()
                                .limit(MAX_POSTS)
                                .map(this::checkImages)
                                .toList();
                        if (!posts.isEmpty()) {
                            synchronized (postsByPlugin) {
                                postsByPlugin.put(socialPlugin, List.copyOf(posts));
                            }
                        }
                    } catch (final Exception e) {
                        LOGGER.error("Unable to load posts from social plugin '{}': {}", socialPlugin, e.getMessage());
                    }
                }))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private @NotNull Post checkImages(final @NotNull Post post) {
        if ((imagesEnabled && imageLimit == 0) || post.images().isEmpty()) {
            return post;
        } else if (!imagesEnabled) {
            return post.withImages(List.of());
        } else {
            final var images = post.images().subList(0, Math.min(imageLimit, post.images().size()));
            return post.withImages(images);
        }
    }

    private boolean checkWordFilter(final @NotNull Post post) {
        final String postText = Jsoup.parse(post.html()).text().toLowerCase(Locale.getDefault());
        for (final String filterWord : filterWords) {
            if (postText.contains(filterWord)) {
                return false;
            }
        }
        return true;
    }

    public @NotNull List<@NotNull Post> getPosts(final int limit) {
        synchronized (postsByPlugin) {
            return postsByPlugin.values()
                    .parallelStream()
                    .flatMap(List::stream)
                    .sorted()
                    .limit(limit <= 0 ? MAX_POSTS : limit)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    public void hidePost(final @NotNull Post postToHide) {
        LOGGER.warn("Hiding post (id={}, profile={}, author={})",
                postToHide.id(), postToHide.profile(), postToHide.author());
        synchronized (postsByPlugin) {
            final var socialPlugins = postsByPlugin.keySet();
            for (final var socialPlugin : socialPlugins) {
                final var filteredPosts = postsByPlugin.get(socialPlugin)
                        .parallelStream()
                        .filter(post -> !post.id().equals(postToHide.id()))
                        .toList();
                postsByPlugin.put(socialPlugin, filteredPosts);
            }
        }
        hiddenPosts.add(postToHide.id());
        saveHiddenPostIds();
    }

    public void blockProfile(final @NotNull Post postToHide) {
        LOGGER.warn("Block profile (id={}, profile={}, author={})",
                postToHide.id(), postToHide.profile(), postToHide.author());
        synchronized (postsByPlugin) {
            final var socialPlugins = postsByPlugin.keySet();
            for (final var socialPlugin : socialPlugins) {
                final var filteredPosts = postsByPlugin.get(socialPlugin)
                        .parallelStream()
                        .filter(post -> !post.profile().equals(postToHide.profile()))
                        .toList();
                postsByPlugin.put(socialPlugin, filteredPosts);
            }
        }
        blockedProfiles.add(postToHide.profile());
        saveBlockedProfiles();
    }

    private @NotNull Path getConfigDir() {
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
