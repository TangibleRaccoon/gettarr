package es.tuliodiez.gettarr.service;

import es.tuliodiez.gettarr.model.DownloadRequest;
import es.tuliodiez.gettarr.model.DownloadResult;
import es.tuliodiez.gettarr.util.DownloadRegistry;
import es.tuliodiez.gettarr.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DownloadService {
    public static final Path TEMP_FOLDER = Path.of("/tmp/media");
    private static final System.Logger LOGGER = System.getLogger("DownloadService");
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final DownloadRegistry downloadRegistry;
    private final DownloadStatusTracker downloadStatusTracker;
    private final ProcessBuilderManager pbManager;

    @Autowired
    public DownloadService(DownloadRegistry downloadRegistry, DownloadStatusTracker downloadStatusTracker, ProcessBuilderManager pbManager) {
        this.downloadRegistry = downloadRegistry;
        this.downloadStatusTracker = downloadStatusTracker;
        this.pbManager = pbManager;
    }

    /**
     * Download method taking in DownloadRequest directly, downloads with given format if avaliable
     */
    public DownloadResult download(DownloadRequest request) {

        if (!Util.isCompleteRequest(request)){
            LOGGER.log(System.Logger.Level.WARNING, "Got incomplete request, getting best audio and video as fallback.");
            return download(request.inputUrl());
        }

        // Get the filename with given quality
        final String filename = downloadRegistry.getOrRegister(request, TEMP_FOLDER);
        final Path filePath = TEMP_FOLDER.resolve(filename);

        // if the file already exists, return it
        if (Files.exists(filePath)) {
            downloadStatusTracker.updateStatus(filename, "COMPLETED");
            return new DownloadResult(true, "COMPLETED", filename);
        }

        // the file doesn't exist, download it
        downloadStatusTracker.updateStatus(filename, "DOWNLOADING");
        ProcessBuilder pb = ProcessBuilderManager.getVideoFromRequest(request, filePath).inheritIO();

        executor.submit(new DownloadTask(pb, filePath, filename, downloadStatusTracker));
        return new DownloadResult(true, "DOWNLOADING", filename);
    }

    /**
     * Basic download method, uses the best Audio and Video quality
     */
    public DownloadResult download(String url) {
        if (url == null || url.isBlank() ) {
            LOGGER.log(System.Logger.Level.ERROR, "Provided URL \""+url+"\"  is null or empty.");
            return new DownloadResult(false, "BAD_REQUEST: No URL provided", "");
        }

        if (!Util.isValidUrl(url)) {
            return new DownloadResult(false, "BAD_REQUEST: Request did not match a URL", "");
        }

        // make the registry map the URL to an ID and search for it in the temp folder.
        final String fileId = downloadRegistry.getOrRegister(url);
        if (fileId == null)
            return new DownloadResult(false, "SERVER ERROR: fileId was null ", "");
        final Path filePath = TEMP_FOLDER.resolve(fileId);

        // If the file already exists, update status (just in case) and return.
        if (Files.exists(filePath)) {
            LOGGER.log(System.Logger.Level.INFO, "File already exists at: " + filePath);
            downloadStatusTracker.updateStatus(fileId, "COMPLETED");
            return new DownloadResult(true, "COMPLETED", filePath.getFileName().toString());
        }

        // we set the status as downloading
        downloadStatusTracker.updateStatus(fileId, "DOWNLOADING");

        // Builder for yt-dlp process
        final ProcessBuilder pb = pbManager.getBestAudioBestVideo(url, filePath).inheritIO();

        // send the download task to the executor and send response to client
        executor.submit(new DownloadTask(pb, filePath, fileId, downloadStatusTracker));
        return new DownloadResult(true, "DOWNLOADING", filePath.getFileName().toString());
    }

    /*
        Represent download task, this way we can use different ProcessBuilders for different URLs
        This task updates the download status directly
     */
    private record DownloadTask(ProcessBuilder pb, Path filePath, String fileId,
                                DownloadStatusTracker tracker) implements Runnable {

    @Override
        public void run() {
            try {
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    LOGGER.log(System.Logger.Level.ERROR, "Downloader exited with error code: " + exitCode);
                    tracker.updateStatus(fileId, "FAILED: " + exitCode);
                } else {
                    LOGGER.log(System.Logger.Level.INFO, "Download completed: " + filePath);
                    tracker.updateStatus(fileId, "COMPLETED");
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.log(System.Logger.Level.ERROR, "Download process error: " + e.getMessage());
                tracker.updateStatus(fileId, "FAILED: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
}
