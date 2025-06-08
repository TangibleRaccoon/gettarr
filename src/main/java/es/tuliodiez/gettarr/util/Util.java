package es.tuliodiez.gettarr.util;

import java.net.URI;
import java.net.URISyntaxException;

public class Util {
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

}
