package es.tuliodiez.gettarr.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.tuliodiez.gettarr.service.DownloadService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class saves a HashMap of the mappings of download URLs to fileIDs
 */
@Component
public class DownloadRegistry {
    private static final System.Logger LOGGER = System.getLogger("DownloadRegistry");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Path FILE_PATH = Path.of("/data/registry.json");
    private static final Random RANDOM = new Random();
    private final ConcurrentHashMap<String, String> linkToFilename = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(0);

    /**
     * Returns filename for given url, if it doesn't exist, it creates a new one and saves it
     */
    public String getOrRegister(String url) {
        //todo: get file format from user.
        if (linkToFilename.containsKey(url)) {
            return linkToFilename.get(url);
        }
        // if we haven't downloaded the URL, we get a new ID
        String filename = "gettarr_" + counter.getAndSet(RANDOM.nextLong()) + ".mp4";
        LOGGER.log(System.Logger.Level.INFO, "Trying name "+filename+" for URL: "+url);

        // if the filename is in the map or the downloads folder, look for another one.
        while (Files.exists(Path.of(DownloadService.TEMP_FOLDER + "/" + filename)) || linkToFilename.containsValue(filename)) {
            LOGGER.log(System.Logger.Level.INFO, filename+" already exists.");
            filename = "gettarr_" + counter.getAndSet(RANDOM.nextLong()) + ".mp4";
        }

        LOGGER.log(System.Logger.Level.INFO, "Settled on "+filename+" for URL: "+url);
        linkToFilename.putIfAbsent(url, filename);
        return filename;
    }

    @PostConstruct
    public void readFromFile() {
        if (Files.exists(FILE_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(FILE_PATH)) {
                ConcurrentHashMap<String, String> loadedMap = objectMapper.readValue(reader, new TypeReference<ConcurrentHashMap<String, String>>() {});
                linkToFilename.putAll(loadedMap);
                String anyKey = linkToFilename.keys().hasMoreElements() ? linkToFilename.keys().nextElement() : "none";
                LOGGER.log(System.Logger.Level.INFO, "Loaded file from " + FILE_PATH + ". Last key-val: " + linkToFilename.get(anyKey));
            } catch (JsonProcessingException e) {
                LOGGER.log(System.Logger.Level.ERROR, "ERROR when reading from " + FILE_PATH + ": " + e.getMessage());
            } catch (IOException e) {
                LOGGER.log(System.Logger.Level.ERROR, "I/O Exception when reading from " + FILE_PATH + ": " + e.getMessage());
            }
        } else {
            LOGGER.log(System.Logger.Level.WARNING, "Tried reading from " + FILE_PATH + " but no file was found, creating file.");
        }
    }


    @PreDestroy
    public void writeToFile() {
        if (Files.isDirectory(Path.of("/data"))) {
            LOGGER.log(System.Logger.Level.ERROR, "Folder /data doesn't exist, can't write registry to file.");
        }
        try {
            try {
                Files.createFile(FILE_PATH);
            // Keep going if file already exists
            } catch (FileAlreadyExistsException _) {}
            objectMapper.writeValue(Files.newBufferedWriter(FILE_PATH), linkToFilename);

        } catch (IOException e) {
           LOGGER.log(System.Logger.Level.ERROR, "I/O Exception when writing to /data/registry.json");
        }
    }
}
