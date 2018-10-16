package com.example.ftpintegration;

import com.example.ftpintegration.ftp.FtpServer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RetrieveFileRequest {

    private final FtpServer server;
    private final String fileName;

    public RetrieveFileRequest(@JsonProperty("server") FtpServer server, @JsonProperty("file_name") String fileName) {
        this.server = server;
        this.fileName = fileName;
    }

    public FtpServer getServer() {
        return server;
    }

    public String getFileName() {
        return fileName;
    }
}
