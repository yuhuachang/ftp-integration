package com.example.ftpintegration.ftp.flow;

import java.io.IOException;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.ftpintegration.ftp.FtpOperation;
import com.example.ftpintegration.processor.FileGenerator;

public class OutboundProcessingFlow extends FtpOperationFlow {

    private static final Logger log = LoggerFactory.getLogger(OutboundProcessingFlow.class);

    private final FileGenerator generator;
    private final String outputPath;

    public OutboundProcessingFlow(FileGenerator generator) {
        this("/", generator);
    }

    public OutboundProcessingFlow(String outputPath, FileGenerator generator) {
        generator.getClass();
        outputPath.getClass();
        this.generator = generator;
        this.outputPath = outputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    @Override
    public void execute(FTPClient client) throws IOException {
        execute(new FtpOperation(client));
    }

    public void execute(FtpOperation op) throws IOException {

        String fileName = null;
        byte[] bytes = null;
        try {
            // 1. get file name
            fileName = generator.getFileName();
            if (fileName == null || fileName.isEmpty()) {
                throw new IOException("File generator does not return a file name.");
            }

            // 2. generate file content
            bytes = generator.generateFile();
            if (bytes == null) {
                throw new IOException("File generator created a file that is null.");
            }
            log.info("Created new file {} of {} bytes", fileName, bytes.length);
        } catch (IOException e) {
            String message = String.format("Error occur while generating file %s", fileName);
            log.error(message, e);
            if (synchronizer != null) {
                synchronizer.onFileError(fileName, bytes, message, e);
            }
            throw e;
        }

        // 3. upload file
        String uploadFileName = String.format("%s/%s", outputPath, fileName);
        log.info("Upload file {}", uploadFileName);
        op.storeFile(uploadFileName, bytes);
        if (log.isDebugEnabled()) {
            log.debug("Upload success.");
        }

        // Send success callback
        if (synchronizer != null) {
            synchronizer.onSuccess(uploadFileName);
        }
    }

}
