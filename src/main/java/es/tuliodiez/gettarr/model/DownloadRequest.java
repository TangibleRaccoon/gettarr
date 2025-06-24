package es.tuliodiez.gettarr.model;

public record DownloadRequest(String inputUrl, String vidQuality, String audioQuality) {}
