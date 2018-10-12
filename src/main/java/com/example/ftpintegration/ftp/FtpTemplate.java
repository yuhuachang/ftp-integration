package com.example.ftpintegration.ftp;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Yu-Hua Chang
 */
public class FtpTemplate {

    private static final Logger log = LoggerFactory.getLogger(FtpTemplate.class);

    private final FtpServer server;
    private final FTPClient client;

    public FtpTemplate(FtpServer server, int timeout) {
        this(new FTPClient(), server, timeout);
    }

    // for unit test
    private FtpTemplate(FTPClient client, FtpServer server, int timeout) {
        server.getClass();
        this.server = server;

        this.client = client;

        // Set server type.
        FTPClientConfig config = new FTPClientConfig(server.getServerType());
        client.configure(config);
        client.setConnectTimeout(timeout);
        client.setControlKeepAliveReplyTimeout(timeout);
        client.setControlKeepAliveTimeout(timeout);
        client.setDataTimeout(timeout);
        client.setDefaultTimeout(timeout);
    }

    private static interface FtpOperation {
        void execute(FtpAgent op);
    }

    private void execute(FtpOperation operation) throws FtpException {
        try {
            client.connect(server.getHost(), server.getPort());

            int reply = client.getReplyCode();
            if (log.isDebugEnabled()) {
                log.debug("connection reply code = {}", reply);
            }
            if (FTPReply.isPositiveCompletion(reply)) {
                // good.
            } else {
                String message = String.format("Failed to connect FTP server %s", server);
                log.error(message);
                throw new FtpConnectionException(message);
            }

            // Enter passive mode
            client.enterLocalPassiveMode();
            client.enterRemotePassiveMode();

            try {
                if (client.login(server.getUsername(), server.getPassword())) {
                    // good
                } else {
                    // login failed.
                    String message = String.format("Failed to login FTP server %s", server);
                    log.error(message);
                    throw new FtpLoginException(message);
                }

                // create agent
                FtpAgent op = new FtpAgent(client);

                // execute operation
                // the execution method can only throw FtpException for any ftp related errors.
                // any other errors (file content related...) should be handled properly and
                // never throw out.
                operation.execute(op);
            } catch (IOException e) {
                String message = "FTP login error.";
                log.error(message, e);
                throw new FtpLoginException(message, e);
            } finally {
                // logout
                if (client.logout()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Logout FTP");
                    }
                } else {
                    log.warn("Failed to logout FTP");
                }
            }
        } catch (FtpException e) {
            // connection or login error (from this class),
            // or other ftp error (from ftp execution).
            throw e;
        } catch (IOException e) {
            String message = "Error on open FTP connection.";
            log.error(message, e);
            throw new FtpConnectionException(message, e);
        } finally {
            if (client.isConnected()) {
                if (log.isDebugEnabled()) {
                    log.debug("Disconnect FTP");
                }
                try {
                    client.disconnect();
                } catch (IOException e) {
                    log.warn("Error on closing FTP connection.", e);
                    // nothing we can do here...
                }
            }
        }
    }

    /**
     * Download and process file.
     * 
     * @param fileName
     * @param handler
     * @return
     */
    public FtpOperationResult retrieveFile(String fileName, FileHandler handler) {
        final FtpOperationResult result = new FtpOperationResult();
        try {
            execute(new FtpOperation() {
                @Override
                public void execute(FtpAgent op) {
                    try {
                        byte[] bytes = op.retrieveFile(fileName);
                        String message = handler.handleFile(bytes);

                        // report success
                        result.setFileName(fileName);
                        result.setSuccess(true);
                        result.setMessage(message);
                    } catch (FtpException e) {
                        result.setError(e);
                    } catch (Throwable e) {
                        result.setError(e);
                    }
                }
            });
        } catch (FtpException e) {
            result.setError(e);
        }
        return result;
    }

    /**
     * Retrieve (download) file content as byte array (binary) from input directory
     * and move it to the archive location after a successful handling.
     * 
     * @param inputFileName
     * @param archiveFileName
     * @param handler
     * @return
     */
    public FtpOperationResult retrieveThenMove(String inputFileName, String archiveFileName, FileHandler handler) {
        final FtpOperationResult result = new FtpOperationResult();
        try {
            execute(new FtpOperation() {
                @Override
                public void execute(FtpAgent op) {
                    try {
                        byte[] bytes = op.retrieveFile(inputFileName);
                        String message = handler.handleFile(bytes);
                        op.storeFile(archiveFileName, bytes);
                        op.deleteFile(inputFileName);

                        // report success
                        result.setFileName(inputFileName);
                        result.setSuccess(true);
                        result.setMessage(message);
                    } catch (FtpException e) {
                        result.setError(e);
                    } catch (Throwable e) {
                        result.setError(e);
                    }
                }
            });
        } catch (FtpException e) {
            result.setError(e);
        }
        return result;
    }

    /**
     * Retrieve (download) file content as byte array (binary) from input directory
     * and delete the source file after a successful handling.
     * 
     * @param fileName
     * @param handler
     * @return
     */
    public FtpOperationResult retrieveThenDelete(String fileName, FileHandler handler) {
        final FtpOperationResult result = new FtpOperationResult();
        try {
            execute(new FtpOperation() {
                @Override
                public void execute(FtpAgent op) {
                    try {
                        byte[] bytes = op.retrieveFile(fileName);
                        String message = handler.handleFile(bytes);
                        op.deleteFile(fileName);

                        // report success
                        result.setFileName(fileName);
                        result.setSuccess(true);
                        result.setMessage(message);
                    } catch (FtpException e) {
                        result.setError(e);
                    } catch (Throwable e) {
                        result.setError(e);
                    }
                }
            });
        } catch (FtpException e) {
            result.setError(e);
        }
        return result;
    }

    /**
     * Retrieve (download) file content as byte array (binary) and move it to the
     * archive location after a successful handling for each file in the input
     * directory. Continue next file if one has error.
     * 
     * @param inputDirectory
     * @param archiveDirectory
     * @param handler
     */
    public void retrieveThenMoveAll(String inputDirectory, String archiveDirectory, FileHandler handler) {
        final List<FtpOperationResult> results = new LinkedList<>();
        try {
            execute(new FtpOperation() {
                @Override
                public void execute(FtpAgent op) {
                    try {
                        FTPFile[] files = op.listFiles(inputDirectory);
                        for (FTPFile file : files) {
                            String fileName = file.getName();
                            if (file.isDirectory()) {
                                continue;
                            }
                            String inputFileName = inputDirectory + "/" + fileName;
                            String archiveFileName = archiveDirectory + "/" + fileName;

                            FtpOperationResult result = new FtpOperationResult();
                            try {
                                byte[] bytes = op.retrieveFile(inputFileName);
                                String message = handler.handleFile(bytes);
                                op.storeFile(archiveFileName, bytes);
                                op.deleteFile(inputFileName);

                                // report success
                                result.setFileName(inputFileName);
                                result.setSuccess(true);
                                result.setMessage(message);
                            } catch (FtpException e) {
                                result.setError(e);
                            } catch (Throwable e) {
                                result.setError(e);
                            }
                            results.add(result);
                        }
                    } catch (FtpException e) {
                        FtpOperationResult result = new FtpOperationResult();
                        result.setError(e);
                        results.add(result);
                    }
                }
            });
        } catch (FtpException e) {
            FtpOperationResult result = new FtpOperationResult();
            result.setError(e);
            results.add(result);
        }
    }

    /**
     * Retrieve (download) file content as byte array (binary) and delete the source
     * file after a successful handling for each file in the input directory.
     * Continue next file if one has error.
     * 
     * @param inputDirectory
     * @param handler
     */
    public void retrieveThenDeleteAll(String inputDirectory, FileHandler handler) {
        final List<FtpOperationResult> results = new LinkedList<>();
        try {
            execute(new FtpOperation() {
                @Override
                public void execute(FtpAgent op) {
                    try {
                        FTPFile[] files = op.listFiles(inputDirectory);
                        for (FTPFile file : files) {
                            String fileName = file.getName();
                            if (file.isDirectory()) {
                                continue;
                            }
                            String inputFileName = inputDirectory + "/" + fileName;

                            FtpOperationResult result = new FtpOperationResult();
                            try {
                                byte[] bytes = op.retrieveFile(inputFileName);
                                String message = handler.handleFile(bytes);
                                op.deleteFile(inputFileName);

                                // report success
                                result.setFileName(inputFileName);
                                result.setSuccess(true);
                                result.setMessage(message);
                            } catch (FtpException e) {
                                result.setError(e);
                            } catch (Throwable e) {
                                result.setError(e);
                            }
                            results.add(result);
                        }
                    } catch (FtpException e) {
                        FtpOperationResult result = new FtpOperationResult();
                        result.setError(e);
                        results.add(result);
                    }
                }
            });
        } catch (FtpException e) {
            FtpOperationResult result = new FtpOperationResult();
            result.setError(e);
            results.add(result);
        }
    }

    /**
     * Upload to ftp.
     * 
     * @param fileName
     * @param bytes
     * @return
     */
    public FtpOperationResult storeFile(String fileName, byte[] bytes) {
        final FtpOperationResult result = new FtpOperationResult();
        try {
            execute(new FtpOperation() {
                @Override
                public void execute(FtpAgent op) {
                    try {
                        op.storeFile(fileName, bytes);

                        // report success
                        result.setFileName(fileName);
                        result.setSuccess(true);
                        result.setMessage("Store File Success");
                    } catch (FtpException e) {
                        result.setError(e);
                    }
                }
            });
        } catch (FtpException e) {
            result.setError(e);
        }
        return result;
    }
}
