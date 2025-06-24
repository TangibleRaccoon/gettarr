package es.tuliodiez.gettarr.util;

import es.tuliodiez.gettarr.model.DownloadRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class Util {

    // video and audio valid quality values
    public static final Set<String> VIDEO_QUALITY_VALUES = new HashSet<>(Set.of("144p", "240p", "360p", "480p", "720p", "1080p", "1440p", "2160p"));
    public static final Set<String> AUDIO_QUALITY_VALUES = new HashSet<>(Set.of("64kbps", "128kbps", "192kbps", "256kbps", "320kbps"));

    /**
     * Checks if URL is valid by using URI class
     */
    public static boolean isValidUrl(String inputString) {
        try {
            URI uri = new URI(inputString);
            return uri.getScheme() != null && (uri.getScheme().equals("http") || uri.getScheme().equals("https"));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Checks if given DownloadRequest has all values and they are valid values
     */
    public static boolean isCompleteRequest(DownloadRequest request) {
        return request.inputUrl() != null
                && isValidUrl(request.inputUrl())
                && request.vidQuality() != null
                && VIDEO_QUALITY_VALUES.contains(request.vidQuality())
                && request.audioQuality() != null
                && AUDIO_QUALITY_VALUES.contains(request.audioQuality());
    }

    public static String getQualityString(DownloadRequest request) {
        return "-" + request.vidQuality() + "-" + request.audioQuality();
    }
}
