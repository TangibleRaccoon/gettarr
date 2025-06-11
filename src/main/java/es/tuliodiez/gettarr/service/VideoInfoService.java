package es.tuliodiez.gettarr.service;

import es.tuliodiez.gettarr.model.GettarrInfo;
import es.tuliodiez.gettarr.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.*;

@Service
public class VideoInfoService {
    private final ProcessBuilderManager pbManager;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private static final System.Logger LOGGER = System.getLogger("VideoInfoService");


    @Autowired
    public VideoInfoService(ProcessBuilderManager pbManager) {
        this.pbManager = pbManager;
    }

    public GettarrInfo getVideoInfo(String queryUrl) {
        if (queryUrl == null || queryUrl.isBlank() || !Util.isValidUrl(queryUrl) ) {
            return new GettarrInfo("", "", "");
        }

        final ProcessBuilder pb = pbManager.getTitleAndThumb(queryUrl);

        final Future<GettarrInfo> info = executor.submit(new InfoTask(pb, queryUrl));

        try {
            return info.get();
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.log(System.Logger.Level.ERROR, "Execution exception getting info: "+e.getMessage());
        }
        return new GettarrInfo("", "", "");
    }

    private record InfoTask(ProcessBuilder pb, String queryUrl) implements Callable<GettarrInfo> {
        @Override
        public GettarrInfo call() {
            String title="", thumbnailURL = "", duration = "";
            try {
                final Process p = pb.start();
                try (final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;


                    while ((line = br.readLine()) != null) {
                        if (line.isEmpty()) {
                            LOGGER.log(System.Logger.Level.ERROR, "Received empty output.");
                            return new GettarrInfo("", "", "");
                        }

                        // Splitting the output
                        final String[] parts = line.split("@@@SPLITHERE@@@");

                        if (parts.length == 3) {
                            title = parts[0];
                            thumbnailURL = parts[1];
                            duration = parts[2];

                            LOGGER.log(System.Logger.Level.INFO, "Got title: " + title + " and url: " + thumbnailURL + " for query: " + queryUrl);
                        } else {
                            LOGGER.log(System.Logger.Level.ERROR, "Unexpected output format: " + line);
                        }
                    }

                    final int code = p.waitFor();
                    if (code != 0)
                        LOGGER.log(System.Logger.Level.INFO, "Info Process finished with error code: " + code);

                    return new GettarrInfo(title, thumbnailURL, duration);
                } catch (InterruptedException e) {
                    LOGGER.log(System.Logger.Level.INFO, "Info Process interrupt.");
                }
            } catch (IOException e) {
                LOGGER.log( System.Logger.Level.ERROR,"I/O exception: "+e.getMessage());
            }
            return new GettarrInfo("", "", "");
        }
    }
}

