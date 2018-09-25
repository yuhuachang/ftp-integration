package com.example.ftpintegration.ftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FtpOperation {

    private final FTPClient client;

    public FtpOperation(FTPClient client) {
        client.getClass();
        this.client = client;
    }

    public FTPFile[] listFiles(String pathname) throws IOException {
        FTPFile[] files = client.listFiles(pathname);
        if (files == null) {
            throw new IOException(String.format("Listing pathname %s returns null.", pathname));
        }
        int reply = client.getReplyCode();
        if (FTPReply.isPositiveCompletion(reply)) {
            return files;
        }
        throw new IOException(String.format("Unsuccessful ftp command on listing path: %s", pathname));
    }

    public byte[] retrieveFile(String fileName) throws IOException {
        byte[] bytes = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (client.retrieveFile(fileName, outputStream)) {
                bytes = outputStream.toByteArray();
            }
        }
        if (bytes == null) {
            // Something goes wrong if returned byte array is null.
            throw new IOException(String.format("Not successfully completed reading file: %s", fileName));
        }
        return bytes;
    }

    public void storeFile(String fileName, byte[] bytes) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            if (client.storeFile(fileName, inputStream)) {
                // good.
            } else {
                throw new IOException(String.format("Upload file %s not success.", fileName));
            }
        }
    }

    public void deleteFile(String fileName) throws IOException {
        if (client.deleteFile(fileName)) {
            // good.
        } else {
            throw new IOException(String.format("Delete file %s not success.", fileName));
        }
    }
}
