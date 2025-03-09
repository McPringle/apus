package swiss.fihlon.apus.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Footer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ocpsoft.prettytime.PrettyTime;
import swiss.fihlon.apus.social.Post;
import swiss.fihlon.apus.util.TestUtil;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static swiss.fihlon.apus.util.TestUtil.getComponentsByClassName;
import static swiss.fihlon.apus.util.TestUtil.getComponentsByTagName;

class PostViewTest {

    private static Stream<Arguments> provideDataForPostViewTest() {
        return Stream.of(
                Arguments.of(
                        "TEST:1",
                        ZonedDateTime.now().minusHours(1),
                        "Firstname Lastname",
                        "http://localhost/avatar.png",
                        "@nickname",
                        "<p>Test</p>",
                        List.of("http://localhost/image1.png", "http://localhost/image2.svg"),
                        ""),
                Arguments.of(
                        "TEST:FOOBAR",
                        ZonedDateTime.now().minusDays(3),
                        "Foobar",
                        "file://foobar.svg",
                        "@foobar",
                        "<p>Foobar</p>",
                        List.of("file://foobar.svg", "https://test/foobar.jpg"),
                        """
                        <svg width="100" height="100" xmlns="http://www.w3.org/2000/svg">
                           <circle cx="50" cy="50" r="40" stroke="green" stroke-width="4" fill="yellow" />
                        </svg>""")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForPostViewTest")
    void testPostView(@NotNull final String postId,
                      @NotNull final ZonedDateTime postDate,
                      @NotNull final String postAuthor,
                      @NotNull final String postAvatar,
                      @NotNull final String postProfile,
                      @NotNull final String postHtml,
                      @NotNull final List<String> postImages,
                      @NotNull final String sourceLogo) {
        final var post = new Post(postId, postDate, postAuthor, postAvatar, postProfile, postHtml, postImages, false, false, sourceLogo);
        final var locale = Locale.ENGLISH;
        final var postView = new PostView(post, locale);

        assertNotNull(postView);
        assertEquals("post-%s".formatted(postId), postView.getId().orElseThrow());
        assertTrue(postView.getClassName().contains("post-view"));

        assertHeader(postView, postAvatar, postAuthor, postProfile);
        assertContent(postView, postHtml);
        assertImage(postView, postImages);
        assertFooter(postView, sourceLogo, postDate, locale);
    }

    private static void assertHeader(@NotNull final PostView postView,
                                     @NotNull final String expectedAvatar,
                                     @NotNull final String expectedAuthor,
                                     @NotNull final String expectedProfile) {
        final var headers = getComponentsByClassName(postView, "header");
        assertEquals(1, headers.size());

        final var header = headers.getFirst();
        assertAvatar(header, expectedAvatar);

        final var authorContainers = getComponentsByClassName(header, "author-container");
        assertEquals(1, authorContainers.size());

        final var authorContainer = authorContainers.getFirst();
        assertAuthor(authorContainer, expectedAuthor);
        assertProfile(authorContainer, expectedProfile);
    }

    private static void assertAvatar(@NotNull final Component component,
                                     @NotNull final String expectedAvatar) {
        final var avatars = getComponentsByClassName(component, "avatar");
        assertEquals(1, avatars.size());

        final var avatar = avatars.getFirst();
        final var element = avatar.getElement();
        assertEquals(expectedAvatar, element.getAttribute("img"));
    }

    private static void assertAuthor(@NotNull final Component component,
                                     @NotNull final String expectedAuthor) {
        final var authors = getComponentsByClassName(component, "author");
        assertEquals(1, authors.size());

        final var author = authors.getFirst();
        final var element = author.getElement();
        assertEquals(expectedAuthor, element.getText());
    }

    private static void assertProfile(@NotNull final Component component,
                                      @NotNull final String expectedProfile) {
        final var profiles = getComponentsByClassName(component, "profile");
        assertEquals(1, profiles.size());

        final var profile = profiles.getFirst();
        final var element = profile.getElement();
        assertEquals(expectedProfile, element.getText());
    }

    private static void assertContent(@NotNull final PostView postView,
                                      @NotNull final String expectedHtml) {
        final var components = getComponentsByClassName(postView, "content");
        assertEquals(1, components.size());

        final var component = components.getFirst();
        final var element = component.getElement();

        final var expectedOuterHtml = """
                <div class="content">
                 %s
                </div>"""
                .formatted(expectedHtml);
        assertEquals(expectedOuterHtml, element.getOuterHTML());
    }

    private static void assertImage(@NotNull final PostView postView,
                                    @NotNull final List<String> expectedImages) {
        final var components = getComponentsByTagName(postView, "img");
        assertEquals(2, components.size());
        assertEquals(expectedImages.get(0), components.getFirst().getElement().getAttribute("src"));
        assertEquals(expectedImages.get(1), components.getLast().getElement().getAttribute("src"));
    }

    private void assertFooter(@NotNull final PostView postView,
                                @NotNull final String sourceLogo,
                                @NotNull final ZonedDateTime postDate,
                                @NotNull final Locale locale) {
        final var components = getComponentsByClassName(postView, "footer");
        assertEquals(1, components.size());
        final var footer = (Footer) components.getFirst();
        assertSourceLogo(footer, sourceLogo);
        assertDateTime(footer, postDate, locale);
    }

    private void assertSourceLogo(@NotNull final Footer footer,
                                  @NotNull final String sourceLogo) {
        final var components = getComponentsByClassName(footer, "source-logo");
        assertEquals(1, components.size());

        final var component = components.getFirst();
        final var html = component.getElement().getOuterHTML();
        if (sourceLogo.isBlank()) {
            assertFalse(html.contains("<svg"));
        } else {
            final var svgCode = TestUtil.extractFirstHtmlTag(html, "svg");
            final var expectedSvgCode = TestUtil.extractFirstHtmlTag(sourceLogo, "svg");
            assertEquals(expectedSvgCode, svgCode);
        }
    }

    private void assertDateTime(@NotNull final Footer footer,
                                @NotNull final ZonedDateTime postDate,
                                @NotNull final Locale locale) {
        final var components = getComponentsByClassName(footer, "datetime");
        assertEquals(1, components.size());
        assertEquals(new PrettyTime(locale).format(postDate), components.getFirst().getElement().getText());
    }

}
