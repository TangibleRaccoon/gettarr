package es.tuliodiez.gettarr.util;

import es.tuliodiez.gettarr.service.DownloadService;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class saves a HashMap of the mappings of download URLs to fileIDs
 */
@Component
public class DownloadRegistry {
    private static final System.Logger LOGGER = System.getLogger("DownloadRegistry");
    // todo: save and load registry from file.
    private final ConcurrentHashMap<String, String> linkToFilename = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(0);

    /**
     * Returns filename for given url, if it doesn't exist, it creates a new one and saves it
     */
    public String getOrRegister(String url) {
        //todo: get file format from user.
        //todo: use randomID instead of incremental.
        if (!linkToFilename.containsKey(url)) {// if the file already exists, we just get the next index
            String filename = "gettarr_" + counter.incrementAndGet() + ".mp4";
            LOGGER.log(System.Logger.Level.INFO, "Trying name "+filename+" for URL: "+url);
            while (Files.exists(Path.of(DownloadService.TEMP_FOLDER + "/" + filename))) {
                LOGGER.log(System.Logger.Level.INFO, filename+" already exists.");
                filename = "gettarr_" + counter.incrementAndGet() + ".mp4";
            }
            LOGGER.log(System.Logger.Level.INFO, "Settled on "+filename+" for URL: "+url);
            linkToFilename.putIfAbsent(url, filename);
            return filename;
        } else {
            return linkToFilename.get(url);
        }
    }
}
