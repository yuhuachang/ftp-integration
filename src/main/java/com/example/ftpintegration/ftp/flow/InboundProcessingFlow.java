package com.example.ftpintegration.ftp.flow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.ftpintegration.processor.FileProcessor;

public class InboundProcessingFlow extends FtpOperationFlow {

    private static final Logger log = LoggerFactory.getLogger(InboundProcessingFlow.class);

    private final FileProcessor processor;
    private final String inputPath;
    private final String archivePath;
    private boolean isDryRun;

    public InboundProcessingFlow(FileProcessor processor) {
        this("/", null, processor);
    }

    public InboundProcessingFlow(String inputPath, FileProcessor processor) {
        this(inputPath, null, processor);
    }

    public InboundProcessingFlow(String inputPath, String archivePath, FileProcessor processor) {
        processor.getClass();
        inputPath.getClass();
        this.processor = processor;
        this.inputPath = inputPath;
        this.archivePath = archivePath;
        if (inputPath.equals(archivePath)) {
            throw new IllegalArgumentException("Input path and archive path cannot be the same.");
        }
        isDryRun = false;
    }

    public String getInputPath() {
        return inputPath;
    }

    public String getArchivePath() {
        return archivePath;
    }

    public boolean isDryRun() {
        return isDryRun;
    }

    public void setDryRun(boolean isDryRun) {
        this.isDryRun = isDryRun;
    }

    @Override
    public void execute(FTPClient client) {

        // List files in the target directory.
        log.info("List input directory {}", inputPath);
        FTPFile[] files = null;
        try {
            files = client.listFiles(inputPath);
        } catch (Exception e) {
            String message = String.format("Error occur while listing input directory: %s", inputPath);
            log.error(message, e);
            if (synchronizor != null) {
                synchronizor.onFtpError(message, e);
            }
            return;
        }

        if (files == null) {
            // This is strange. It can be empty but shouldn't be null.
            log.warn("Input directory is null.");
            return;
        }

        if (files.length == 0) {
            log.info("Input directory is empty.");
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
                        String message = String.format("Not successfully completed reading file: %s", inputFileName);
                        log.error(message);
                        if (synchronizor != null) {
                            synchronizor.onFileError(inputFileName, message, null);
                        }
                        continue;
                    }
                }

                if (bytes == null) {
                    // It can be empty but shouldn't be null.
                    String message = String.format("Nothing read from file: %s. EDI content may be wrong.", inputFileName);
                    log.warn(message);
                    if (synchronizor != null) {
                        synchronizor.onFileError(inputFileName, message, null);
                    }
                    continue;
                } else {
                    log.info("Read {} bytes from file: {}", bytes.length, inputFileName);

                    // The core file processing is here.
                    try {
                        processor.processFile(bytes);
                        log.info("Complete processing file {}", inputFileName);
                    } catch (UnsupportedEncodingException e) {
                        // File encoding error.
                        String message = "Cannot decode file content.";
                        log.error(message, e);
                        if (synchronizor != null) {
                            synchronizor.onFileError(inputFileName, message, e);
                        }
                        continue;
                    } catch (Throwable e) {
                        // Errors related to business reason. For example, file content validation failure.
                        // Usually need to contact business users.
                        log.error("Processing file {} failed with error.", inputFileName, e);
                        if (synchronizor != null) {
                            synchronizor.onFailure(inputFileName, e.getMessage(), e);
                        }
                        continue;
                    }
                }

                // If nothing wrong, we start to commit the transaction by
                // moving file to archive folder.
                String archiveFileName = String.format("%s/%s", archivePath, file.getName());
                log.info("Archive file {}", archiveFileName);
                if (isDryRun) {
                    log.info("Operating in test mode.  Nothing will be changed on FTP.");
                } else {
                    try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                        if (client.storeFile(archiveFileName, inputStream)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Upload success.");
                            }
                        } else {
                            String message = String.format("Upload file %s failed.", archiveFileName);
                            log.error(message);
                            if (synchronizor != null) {
                                synchronizor.onFtpError(message, null);
                            }
                            continue;
                        }
                    }
                }

                // Finally, we delete the input file.
                log.info("Delete input file: {}", inputFileName);
                if (isDryRun) {
                    log.info("Operating in test mode.  Nothing will be changed on FTP.");
                } else {
                    if (client.deleteFile(inputFileName)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Delete success.");
                        }
                    } else {
                        String message = String.format("Delete file %s failed.", inputFileName);
                        log.error(message);
                        if (synchronizor != null) {
                            synchronizor.onFtpError(message, null);
                        }
                        continue;
                    }
                }

                // Send success callback
                if (synchronizor != null) {
                    synchronizor.onSuccess(inputFileName);
                }
            } catch (Throwable e) {
                // Technical errors, for example, not able to extract the file content.
                // Usually need to contact business users and technical people.
                String message = "There is a technical problem processing the input file. Processing input file failed.";
                log.error(message, e);
                if (synchronizor != null) {
                    synchronizor.onFtpError(message, e);
                }
            }
        }
    }

}
