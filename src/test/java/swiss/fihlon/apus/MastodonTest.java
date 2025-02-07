package swiss.fihlon.apus;

import org.junit.jupiter.api.Test;
import social.bigbone.MastodonClient;
import social.bigbone.api.Range;
import social.bigbone.api.entity.Status;
import social.bigbone.api.exception.BigBoneRequestException;

import java.util.List;

import static social.bigbone.api.method.TimelineMethods.StatusOrigin.LOCAL_AND_REMOTE;

public class MastodonTest {

    @Test
    void showPosts() throws BigBoneRequestException {
        final MastodonClient client = new MastodonClient.Builder("mastodon.social").build(); // AbstractMethodError
        final Range range = new Range(null, null, null, 10);
        final List<Status> posts = client.timelines().getTagTimeline("java", LOCAL_AND_REMOTE, range).execute().getPart();
        System.out.println(posts);
    }

}
