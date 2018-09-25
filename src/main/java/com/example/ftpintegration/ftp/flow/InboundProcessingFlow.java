package com.example.ftpintegration.ftp.flow;

import java.io.IOException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.ftpintegration.ftp.FtpOperation;
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
    public void execute(FTPClient client) throws IOException {
        execute(new FtpOperation(client));
    }

    public void execute(FtpOperation op) throws IOException {

        // List files in the target directory.
        log.info("List input directory {}", inputPath);
        FTPFile[] files = op.listFiles(inputPath);

        int numberOfFiles = files.length;
        synchronizer.onListing(numberOfFiles);
        if (numberOfFiles == 0) {
            log.info("Input directory is empty.");
            return;
        }

        // Iterate all files and send to callback function.
        for (FTPFile file : files) {

            String fileName = file.getName();

            // Ignore directories.
            if (file.isDirectory()) {
                continue;
            }

            // Begin transaction here. Each file has its own transaction.
            try {
                String inputFileName = String.format("%s/%s", inputPath, fileName);
                if (log.isDebugEnabled()) {
                    log.debug("Try to read file: {}", inputFileName);
                }
                if (synchronizer != null) {
                    synchronizer.onStart(inputFileName);
                }

                // 1. Retrieve file content to byte array.
                byte[] bytes = op.retrieveFile(inputFileName);
                log.info("Read {} bytes from file: {}", bytes.length, inputFileName);

                // 2. Process file content.
                try {
                    processor.processFile(bytes);
                    log.info("Complete processing file {}", inputFileName);
                } catch (IOException e) {
                    String message = String.format("Error occur while processing file %s", inputFileName);
                    log.error(message, e);
                    if (synchronizer != null) {
                        synchronizer.onFileError(inputFileName, bytes, message, e);
                    }
                    continue;
                }

                // 3. archiving (if archive folder is set)
                if (archivePath == null) {
                    log.info("No archive folder set.  No archiving action.");
                } else {
                    // moving file to archive folder.
                    String archiveFileName = String.format("%s/%s", archivePath, fileName);
                    log.info("Archive file {}", archiveFileName);
                    if (isDryRun) {
                        log.info("Operating in test mode.  Nothing will be changed on FTP.");
                    } else {
                        op.storeFile(archiveFileName, bytes);
                        if (log.isDebugEnabled()) {
                            log.debug("Upload success.");
                        }
                    }

                    // 4. Finally, we delete the input file.
                    log.info("Delete input file: {}", inputFileName);
                    if (isDryRun) {
                        log.info("Operating in test mode.  Nothing will be changed on FTP.");
                    } else {
                        op.deleteFile(inputFileName);
                        if (log.isDebugEnabled()) {
                            log.debug("Delete success.");
                        }
                    }
                }

                // Send success callback
                if (synchronizer != null) {
                    synchronizer.onSuccess(inputFileName);
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                if (synchronizer != null) {
                    synchronizer.onFtpError(e.getMessage(), e);
                }
                // continue process next file.
            }
        }
    }

}
