package es.tuliodiez.gettarr.model;

public record DownloadResult(boolean success, String message, String filePath) {
}
