package es.tuliodiez.gettarr.service;

import es.tuliodiez.gettarr.model.DownloadRequest;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class ProcessBuilderManager {

    public static ProcessBuilder getVideoFromRequest(DownloadRequest request, Path filePath) {

        final String quality = "bestvideo[height<=" + request.vidQuality().replaceAll("\\D+", "") +"]+bestaudio[abr<="+request.audioQuality().replaceAll("\\D+", "")+ "]";
        return new ProcessBuilder(
                "yt-dlp",
                "-f", quality,
                "--merge-output-format", "mp4",
                "--recode-video", "mp4",
                "-o", filePath.toString(),
                request.inputUrl()
        );
    }

    public  ProcessBuilder getBestAudioBestVideo(String url, Path filePath) {
        return new ProcessBuilder(
                "yt-dlp",
                "-f", "bv+ba",                        // Best video + best audio (merged)
                "--merge-output-format", "mp4",       // Ensure MP4 format
                "-o", filePath.toString(),            // Output filename
                url                                   // Url to download
        );
    }

    public ProcessBuilder getTitleAndThumb(String queryUrl) {
        return new ProcessBuilder(
                //yt-dlp --print "%(title)s  %(thumbnail)s" "https://es.pinterest.com/pin/648307308896155710/"
                "yt-dlp",
                "--print",
                "%(title)s @@@SPLITHERE@@@ %(thumbnail)s @@@SPLITHERE@@@ %(duration)s",
                queryUrl
        );
    }
}
