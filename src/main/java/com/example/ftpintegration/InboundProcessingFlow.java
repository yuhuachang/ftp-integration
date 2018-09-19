package com.example.ftpintegration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboundProcessingFlow implements FtpOperationFlow {

    private static final Logger log = LoggerFactory.getLogger(InboundProcessingFlow.class);

    private final FileProcessor processor;
    private final String inputPath;
    private final String archivePath;
    private boolean isDryRun;
    private ActionCallback successCallback;
    private ActionCallback errorCallback;

    public InboundProcessingFlow(String inputPath, FileProcessor processor) {
        this(inputPath, null, processor);
    }

    public InboundProcessingFlow(String inputPath, String archivePath, FileProcessor processor) {
        Assert.assertNotNull(processor);
        this.processor = processor;
        this.inputPath = inputPath == null ? "/" : inputPath;
        this.archivePath = archivePath == null ? "/" : archivePath;
        Assert.assertFalse("Input path and archive path cannot be the same.", inputPath.equals(archivePath));
        isDryRun = false;
    }

    public boolean isDryRun() {
        return isDryRun;
    }

    public void setDryRun(boolean isDryRun) {
        this.isDryRun = isDryRun;
    }

    public void setSuccessCallback(ActionCallback successCallback) {
        this.successCallback = successCallback;
    }

    public void setErrorCallback(ActionCallback errorCallback) {
        this.errorCallback = errorCallback;
    }

    @Override
    public void execute(FTPClient client) {

        // List files in the target directory.
        log.info("List input directory {}", inputPath);
        FTPFile[] files = null;
        try {
            files = client.listFiles(inputPath);
        } catch (Exception e) {
            log.warn("Error on list files.", e);
            return;
        }

        if (files == null || files.length == 0) {
            log.warn("Input directory is empty.");
            return;
        }

        // Iterate all files and send to callback function.
        for (FTPFile file : files) {
            if (file.isDirectory()) {
                // Ignore directories.
                continue;
            }

            // Begin transaction here. Each file has its own transaction.
            try {
                String inputFileName = String.format("%s/%s", inputPath, file.getName());
                if (log.isDebugEnabled()) {
                    log.debug("Try to read file: {}", inputFileName);
                }

                // Download file and read into String
                byte[] bytes = null;
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    if (client.retrieveFile(inputFileName, outputStream)) {
                        bytes = outputStream.toByteArray();
                    } else {
                        throw new RuntimeException("Not successfully completed reading file: " + inputFileName);
                    }
                }

                if (bytes == null) {
                    throw new RuntimeException("Nothing read from file: " + inputFileName);
                } else {
                    log.info("Read {} bytes from file: {}", bytes.length, inputFileName);

                    // Processor should throw exception if something goes wrong.
                    processor.processFile(bytes);
                }

                // Stop here if is for testing.
                if (isDryRun) {
                    log.info("Operating in test mode.  Nothing will be changed on FTP.");
                    continue;
                }

                // If nothing wrong, we start to commit the transaction by
                // moving file to archive folder.
                String archiveFileName = String.format("%s/%s", archivePath, file.getName());
                log.info("Archive file {}", archiveFileName);
                boolean uploadResult = false;
                try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                    uploadResult = client.storeFile(archiveFileName, inputStream);
                }

                if (uploadResult) {
                    if (log.isDebugEnabled()) {
                        log.debug("Upload success.");
                    }
                } else {
                    throw new RuntimeException("Upload file " + archiveFileName + " failed.");
                }

                // Finally, we delete the input file.
                log.info("Delete input file: {}", inputFileName);
                if (client.deleteFile(inputFileName)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Delete success.");
                    }
                } else {
                    throw new RuntimeException("Delete file " + inputFileName + " failed.");
                }

                // Send success callback
                if (successCallback != null) {
                    successCallback.callback("Processing input file success.");
                }
            } catch (Throwable e) {
                log.error("Processing input file failed.", e);

                // Send error callback
                if (errorCallback != null) {
                    errorCallback.callback("Processing input file failed.");
                }
            }
        }
    }

}
