package aagapp_backend.components.url;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SocialMediaValidator {

    private static final Pattern FACEBOOK_PATTERN = Pattern.compile("^https://(www\\.)?facebook\\.com/[^/]+/?$");
    private static final Pattern TWITTER_PATTERN = Pattern.compile("^https://(www\\.)?twitter\\.com/[^/]+/?$");
    private static final Pattern SNAPCHAT_PATTERN = Pattern.compile("^https://(www\\.)?snapchat\\.com/add/[^/]+/?$");
    private static final Pattern INSTAGRAM_PATTERN = Pattern.compile("^https://(www\\.)?instagram\\.com/[^/]+/?$");
    private static final Pattern YOUTUBE_PATTERN = Pattern.compile("^https://(www\\.)?youtube\\.com/channel/[^/]+/?$");

    public static boolean isFacebookUrl(String url) {
        return FACEBOOK_PATTERN.matcher(url).matches();
    }

    public static boolean isTwitterUrl(String url) {
        return TWITTER_PATTERN.matcher(url).matches();
    }

    public static boolean isSnapchatUrl(String url) {
        return SNAPCHAT_PATTERN.matcher(url).matches();
    }

    // Validate Instagram URL
    public static boolean isInstagramUrl(String url) {
        return INSTAGRAM_PATTERN.matcher(url).matches();
    }

    // Validate YouTube URL
    public static boolean isYouTubeUrl(String url) {
        return YOUTUBE_PATTERN.matcher(url).matches();
    }
}
