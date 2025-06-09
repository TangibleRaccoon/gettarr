package es.tuliodiez.gettarr.service;

import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class ProcessBuilderManager {
    public  ProcessBuilder getBestAudioBestVideo(String url, Path filePath) {
        return new ProcessBuilder(
                "yt-dlp",
                "-f", "bv+ba",                        // Best video + best audio (merged)
                "--merge-output-format", "mp4",       // Ensure MP4 format
                "-o", filePath.toString(),            // Output filename
                url                                   // Url to download
        );
    }

    // todo: Get this manually from --dump-json?
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
