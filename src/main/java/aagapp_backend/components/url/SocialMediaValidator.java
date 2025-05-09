package aagapp_backend.components.url;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SocialMediaValidator {
//    private static final Pattern FACEBOOK_PATTERN = Pattern.compile("^(https?://)(www\\.)?facebook\\.com/(profile\\.php\\?id=\\d+|[a-zA-Z0-9.\\-_]+)(/)?$");
    private static final Pattern SNAPCHAT_PATTERN = Pattern.compile("^https://(www\\.)?snapchat\\.com/add/[^/]+/?$");
//    private static final Pattern INSTAGRAM_PATTERN = Pattern.compile("^https?://(www\\.)?instagram\\.com/[a-zA-Z0-9-_]{1,255}/?$", Pattern.CASE_INSENSITIVE);


//    private static final Pattern YOUTUBE_PATTERN = Pattern.compile("^https?://(www\\.)?youtube\\.com/@[a-zA-Z0-9_]{1,50}$", Pattern.CASE_INSENSITIVE);

//    private static final Pattern TWITTER_PATTERN = Pattern.compile("^(https?://)?(www\\.)?(twitter|x)\\.com/(?:@)?[a-zA-Z0-9_]{1,15}/?$");


    private static final Pattern FACEBOOK_PATTERN = Pattern.compile("^(https?://)?(www\\.)?facebook\\.com/(profile\\.php\\?id=\\d+|[a-zA-Z0-9.\\-_]+)(/)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TWITTER_PATTERN = Pattern.compile("^(https?://)?(www\\.)?(twitter|x)\\.com/(?:@)?[a-zA-Z0-9_]{1,15}/?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INSTAGRAM_PATTERN = Pattern.compile("^https?://(www\\.)?instagram\\.com/[a-zA-Z0-9-_]{1,255}/?.*$", Pattern.CASE_INSENSITIVE);

    private static final Pattern YOUTUBE_PATTERN = Pattern.compile(
            "^(?:https?://)?(?:" +
                    "(?:www\\.|m\\.)?youtube\\.com/(?:" +
                    "@[a-zA-Z0-9_\\-]+(?:\\?.*)?" +                 // @handle
                    "|watch\\?v=[a-zA-Z0-9_-]+" +                   // watch?v=
                    "|v/[a-zA-Z0-9_-]+" +                           // v/
                    "|embed/[a-zA-Z0-9_-]+" +                       // embed/
                    "|channel/[a-zA-Z0-9_-]+" +                     // channel/
                    ")" +
                    "|youtu\\.be/[a-zA-Z0-9_-]+" +                      // youtu.be/
                    ")(\\?.*)?$", Pattern.CASE_INSENSITIVE
    );

    public static boolean isFacebookUrl(String url) {
        return FACEBOOK_PATTERN.matcher(url).matches();
    }

    public static boolean isTwitterUrl(String url) {
        return TWITTER_PATTERN.matcher(url).matches();
    }

    public static boolean isSnapchatUrl(String url) {
        return SNAPCHAT_PATTERN.matcher(url).matches();
    }

    public static boolean isInstagramUrl(String url) {
        return INSTAGRAM_PATTERN.matcher(url).matches();
    }

    public static boolean isYouTubeUrl(String url) {
        return YOUTUBE_PATTERN.matcher(url).matches();
    }
}
