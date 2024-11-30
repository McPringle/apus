package swiss.fihlon.apus.plugin.social.bluesky;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import swiss.fihlon.apus.configuration.Configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BlueSkyConfigTest {

    @Autowired
    private Configuration configuration;

    @Test
    void testBlueSkyConfig() {
        final var blueSkyConfig = configuration.getBlueSky();
        assertNotNull(blueSkyConfig);
        assertEquals("java", blueSkyConfig.hashtags());
        assertEquals("public.api.bsky.app", blueSkyConfig.instance());
        assertEquals("https://%s/xrpc/app.bsky.feed.searchPosts?q=%s", blueSkyConfig.postAPI());
    }

}
