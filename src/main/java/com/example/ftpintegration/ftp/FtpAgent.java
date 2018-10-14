package com.example.ftpintegration.ftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.ftpintegration.ftp.exception.FtpConnectionException;
import com.example.ftpintegration.ftp.exception.FtpDeleteFileException;
import com.example.ftpintegration.ftp.exception.FtpException;
import com.example.ftpintegration.ftp.exception.FtpListFilesException;
import com.example.ftpintegration.ftp.exception.FtpLoginException;
import com.example.ftpintegration.ftp.exception.FtpModeSwitchException;
import com.example.ftpintegration.ftp.exception.FtpRetrieveFileException;
import com.example.ftpintegration.ftp.exception.FtpStoreFileException;

/**
 * FTP agent that wrap core functions in {@link FTPClient} for easier unit
 * testing.
 * 
 * @author Yu-Hua Chang
 */
public class FtpAgent {

    private static final Logger log = LoggerFactory.getLogger(FtpAgent.class);

    private final FTPClient client;

    public FtpAgent(FTPClient client) {
        client.getClass();
        this.client = client;
    }

    public void connect(String hostname, int port) throws FtpConnectionException {
        log.info(String.format("Connect to %s:%d", hostname, port));
        try {
            client.connect(hostname, port);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new FtpConnectionException(e);
        }
        int reply = client.getReplyCode();
        if (FTPReply.isPositiveCompletion(reply)) {
            // good.
        } else {
            String msg = String.format("Failed to connect %s:%d", hostname, port);
            log.error(msg);
            throw new FtpConnectionException(msg);
        }
    }

    public void disconnect() {
        log.info("Disconnect");
        if (client.isConnected()) {
            try {
                client.disconnect();
            } catch (IOException e) {
                log.warn("Disconnect failed.", e);
            }
        }
    }

    public void enterPassiveMode() throws FtpModeSwitchException {
        log.info("Enter passive mode");
        client.enterLocalPassiveMode();
        try {
            if (client.enterRemotePassiveMode()) {
                // good.
            } else {
                String msg = "Unable to enter remote passive mode.";
                log.error(msg);
                throw new FtpModeSwitchException(msg);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new FtpModeSwitchException(e);
        }
    }

    public void login(String username, String password) throws FtpLoginException {
        log.info(String.format("Login with %s/%s", username, password));
        try {
            if (client.login(username, password)) {
                // good
            } else {
                String msg = String.format("Failed to login with %s/%s", username, password);
                log.error(msg);
                throw new FtpLoginException(msg);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new FtpLoginException(e);
        }
    }

    public void logout() {
        log.info("Logout");
        try {
            if (client.logout()) {
                // good
            } else {
                log.warn("Logout unsuccessful");
            }
        } catch (IOException e) {
            log.warn("Logout error", e);
        }
    }

    /**
     * List files in ftp directory.
     * 
     * @param pathname
     * @return
     * @throws FtpException
     */
    public FTPFile[] listFiles(String pathname) throws FtpListFilesException {
        log.info(String.format("List %s", pathname));
        try {
            FTPFile[] files = client.listFiles(pathname);
            if (files == null) {
                String msg = String.format("Listing %s returns null.", pathname);
                log.warn(msg);
                throw new FtpListFilesException(msg);
            }
            int reply = client.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                return files;
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            throw new FtpListFilesException(e);
        }
        String msg = String.format("Unsuccessful ftp command on listing path: %s", pathname);
        log.warn(msg);
        throw new FtpListFilesException(msg);
    }

    /**
     * Retrieve (download) the file content as byte array (binary) from ftp.
     * 
     * @param fileName
     *            path + file name.
     * @return
     * @throws FtpRetrieveFileException
     */
    public byte[] retrieveFile(String fileName) throws FtpRetrieveFileException {
        log.info(String.format("Retrieve %s", fileName));
        byte[] bytes = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (client.retrieveFile(fileName, outputStream)) {
                bytes = outputStream.toByteArray();
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            throw new FtpRetrieveFileException(e);
        }
        if (bytes == null) {
            // Something goes wrong if returned byte array is null.
            String msg = String.format("Unsuccessfully reading file: %s", fileName);
            log.warn(msg);
            throw new FtpRetrieveFileException(msg);
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
    public void storeFile(String fileName, byte[] bytes) throws FtpStoreFileException {
        log.info(String.format("Store %s", fileName));
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            if (client.storeFile(fileName, inputStream)) {
                // good.
            } else {
                String msg = String.format("Store file %s not success.", fileName);
                log.warn(msg);
                throw new FtpStoreFileException(msg);
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            throw new FtpStoreFileException(e);
        }
    }

    /**
     * Delete remote file in ftp.
     * 
     * @param fileName
     *            path + file name
     * @throws FtpException
     */
    public void deleteFile(String fileName) throws FtpDeleteFileException {
        log.info(String.format("Store %s", fileName));
        try {
            if (client.deleteFile(fileName)) {
                // good.
            } else {
                String msg = String.format("Delete file %s not success.", fileName);
                log.warn(msg);
                throw new FtpDeleteFileException(msg);
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            throw new FtpDeleteFileException(e);
        }
    }
}
