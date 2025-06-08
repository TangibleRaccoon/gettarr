package es.tuliodiez.gettarr.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class DownloadStatusTracker {
    private final ConcurrentHashMap<String, String> statusMap = new ConcurrentHashMap<>();
    private static final System.Logger LOGGER = System.getLogger("statusTracker");

    /**
     * Update the status for a given file ID.
     * @param fileId the unique ID
     * @param status the current status (e.g. "DOWNLOADING", "COMPLETED", "FAILED")
     */
    public void updateStatus(String fileId, String status) {
        statusMap.put(fileId, status);
        LOGGER.log(System.Logger.Level.INFO, "updated: "+fileId+" as "+status);
    }

    /**
     * Retrieves the current status for a file ID.
     * @param fileId the unique ID
     * @return the status string, or null if the file ID isn’t tracked yet
     */
    public String getStatus(String fileId) {
        return statusMap.get(fileId);
    }
}
