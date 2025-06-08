package es.tuliodiez.gettarr.controller;

import es.tuliodiez.gettarr.model.DownloadRequest;
import es.tuliodiez.gettarr.model.DownloadResult;
import es.tuliodiez.gettarr.model.GettarrResponse;
import es.tuliodiez.gettarr.service.DownloadService;
import es.tuliodiez.gettarr.service.DownloadStatusTracker;
import es.tuliodiez.gettarr.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
// this way we can get querys from frontend
@CrossOrigin
public class GettarrController {
    private static final System.Logger LOGGER = System.getLogger("GettarrController");

    private final DownloadService downloadService;
    private final DownloadStatusTracker statusTracker;

    @Autowired
    public GettarrController(DownloadService downloadService, DownloadStatusTracker statusTracker) {
        this.downloadService = downloadService;
        this.statusTracker = statusTracker;
    }

    @PostMapping(path = "/dw")
    public ResponseEntity<GettarrResponse> downloadVideo(@RequestBody DownloadRequest downloadRequest) {
        final String inputUrl = downloadRequest.inputUrl();

        final DownloadResult dwResult = downloadService.download(inputUrl);
        if (dwResult.success()){
            return ResponseEntity.ok(new GettarrResponse("OK", dwResult.filePath()));
        } else if (dwResult.message().contains("BAD_REQUEST")) {
            return ResponseEntity.badRequest().body(new GettarrResponse("BAD_REQUEST", dwResult.message()));
        } else {
            return ResponseEntity.internalServerError().body(new GettarrResponse("ERROR", dwResult.message()));
        }
    }

    @GetMapping(path = "/dw/status/{fileId}")
    public ResponseEntity<GettarrResponse> getDownloadStatus(@PathVariable String fileId) {
        final String status = statusTracker.getStatus(fileId);
        LOGGER.log(System.Logger.Level.INFO, "sent status "+status+" for fileID "+fileId);
        if (status == null) {
            return ResponseEntity.status(404).body(new GettarrResponse("NOT_FOUND", "No download found for ID: "+fileId));
        }
        return ResponseEntity.ok(new GettarrResponse(status, fileId));
    }
}
