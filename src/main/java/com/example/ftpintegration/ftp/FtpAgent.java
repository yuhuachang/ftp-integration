package com.example.ftpintegration.ftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * FTP agent that wrap core functions in {@link FTPClient} for easier unit
 * testing.
 * 
 * @author Yu-Hua Chang
 */
public class FtpAgent {

    private final FTPClient client;

    public FtpAgent(FTPClient client) {
        client.getClass();
        this.client = client;
    }

    /**
     * List files in ftp directory.
     * 
     * @param pathname
     * @return
     * @throws FtpException
     */
    public FTPFile[] listFiles(String pathname) throws FtpException {
        try {
            FTPFile[] files = client.listFiles(pathname);
            if (files == null) {
                throw new FtpException(String.format("Listing pathname %s returns null.", pathname));
            }
            int reply = client.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                return files;
            }
        } catch (IOException e) {
            throw new FtpException(e);
        }
        throw new FtpException(String.format("Unsuccessful ftp command on listing path: %s", pathname));
    }

    /**
     * Retrieve (download) the file content as byte array (binary) from ftp.
     * 
     * @param fileName
     *            path + file name.
     * @return
     * @throws FtpException
     */
    public byte[] retrieveFile(String fileName) throws FtpException {
        byte[] bytes = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (client.retrieveFile(fileName, outputStream)) {
                bytes = outputStream.toByteArray();
            }
        } catch (IOException e) {
            throw new FtpException(e);
        }
        if (bytes == null) {
            // Something goes wrong if returned byte array is null.
            throw new FtpException(String.format("Unsuccessfully reading file: %s", fileName));
        }
        return bytes;
    }

    /**
     * Store (upload) file content in byte array (binary) to ftp.
     * 
     * @param fileName
     *            path + file name
     * @param bytes
     * @throws FtpException
     */
    public void storeFile(String fileName, byte[] bytes) throws FtpException {
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            if (client.storeFile(fileName, inputStream)) {
                // good.
            } else {
                throw new FtpException(String.format("Upload file %s not success.", fileName));
            }
        } catch (IOException e) {
            throw new FtpException(e);
        }
    }

    /**
     * Delete remote file in ftp.
     * 
     * @param fileName
     *            path + file name
     * @throws FtpException
     */
    public void deleteFile(String fileName) throws FtpException {
        try {
            if (client.deleteFile(fileName)) {
                // good.
            } else {
                throw new FtpException(String.format("Delete file %s not success.", fileName));
            }
        } catch (IOException e) {
            throw new FtpException(e);
        }
    }
}
