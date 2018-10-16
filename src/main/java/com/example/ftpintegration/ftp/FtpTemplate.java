package com.example.ftpintegration.ftp;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import com.example.ftpintegration.ftp.exception.FtpConnectionException;
import com.example.ftpintegration.ftp.exception.FtpLoginException;
import com.example.ftpintegration.ftp.exception.FtpModeSwitchException;
import com.example.ftpintegration.ftp.handler.FileHandler;

/**
 * Wrap common ftp actions, such as connect, disconnect, login, logout, etc.
 * into templates and organize common ftp operations as function calls.
 * 
 * @author Yu-Hua Chang
 */
public class FtpTemplate {

    private final FtpServer server;

    public FtpTemplate(FtpServer server) {
        this.server = server;
    }

    /**
     * internal interface that provide simple java lambda execution that optionally
     * write to the result object.
     */
    interface FtpOperation {
        void execute(final FtpOperationResult result) throws Throwable;
    }

    /**
     * the template to handle ftp connect and disconnect.
     */
    class DoWithConnectionTemplate implements FtpOperation {

        private final FtpOperation op;

        public DoWithConnectionTemplate(FtpOperation op) {
            this.op = op;
        }

        @Override
        public void execute(final FtpOperationResult result) throws Throwable {
            FtpAgent agent = server.getFtpAgent();
            try {
                agent.connect(server.getHost(), server.getPort());

                // nested call operations...
                if (op != null) {
                    op.execute(result);
                }
            } catch (FtpConnectionException e) {
                // set connection error
                result.setError(e);
                throw e;
            } catch (FtpModeSwitchException e) {
                // set mode switch error
                result.setError(e);
                throw e;
            } catch (Throwable e) {
                // do nothing. only handle the exceptions we know here.
                throw e;
            } finally {
                agent.disconnect();
            }
        }
    }

    /**
     * the template to handle ftp login and logout. it is placed inside connection
     * template usually.
     */
    class DoWithLoginTemplate implements FtpOperation {

        private final FtpOperation op;

        public DoWithLoginTemplate(FtpOperation op) {
            this.op = op;
        }

        @Override
        public void execute(final FtpOperationResult result) throws Throwable {
            FtpAgent agent = server.getFtpAgent();
            try {
                agent.login(server.getUsername(), server.getPassword());

                if (server.isPassiveMode()) {
                    agent.enterPassiveMode();
                }

                // nested call operations...
                if (op != null) {
                    op.execute(result);
                }
            } catch (FtpLoginException e) {
                // login error
                result.setError(e);
                throw e;
            } catch (Throwable e) {
                // do nothing. only handle the exceptions we know here.
                throw e;
            } finally {
                agent.logout();
            }
        }
    }

    /**
     * Default template that combine connection and login template together.
     */
    class DefaultTemplate {
        public FtpOperationResult run(FtpOperation op) {
            final FtpOperationResult result = new FtpOperationResult();

            DoWithLoginTemplate loginTemplate = new DoWithLoginTemplate(op);
            DoWithConnectionTemplate connectionTemplate = new DoWithConnectionTemplate(loginTemplate);

            try {
                connectionTemplate.execute(result);
            } catch (Throwable e) {
                result.setError(e);
            }
            return result;
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
        return new DefaultTemplate().run(result -> {
            FtpAgent agent = server.getFtpAgent();
            byte[] bytes = agent.retrieveFile(fileName);
            String message = handler.handleFile(bytes);
            if (message == null) {
                result.setMessage(String.format("Download file %s success.", fileName));
            } else {
                result.setMessage(message);
            }
            result.setSuccess(true);
        });
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
        return new DefaultTemplate().run(result -> {
            FtpAgent agent = server.getFtpAgent();
            byte[] bytes = agent.retrieveFile(fileName);
            String message = handler.handleFile(bytes);
            agent.deleteFile(fileName);
            if (message == null) {
                result.setMessage(String.format("Process file %s success.", fileName));
            } else {
                result.setMessage(message);
            }
            result.setSuccess(true);
        });
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
        return new DefaultTemplate().run(result -> {
            FtpAgent agent = server.getFtpAgent();
            byte[] bytes = agent.retrieveFile(inputFileName);
            String message = handler.handleFile(bytes);
            agent.storeFile(archiveFileName, bytes);
            agent.deleteFile(inputFileName);
            if (message == null) {
                result.setMessage(String.format("Process file %s success.", inputFileName));
            } else {
                result.setMessage(message);
            }
            result.setSuccess(true);
        });
    }

    /**
     * Retrieve (download) file content as byte array (binary) and delete the source
     * file after a successful handling for each file in the input directory.
     * Continue next file if one has error.
     * 
     * @param inputDirectory
     * @param handler
     * @return
     */
    public List<FtpOperationResult> retrieveThenDeleteAll(String inputDirectory, FileHandler handler) {
        List<FtpOperationResult> results = new LinkedList<>();
        new DefaultTemplate().run(result -> {
            FtpAgent agent = server.getFtpAgent();
            FTPFile[] files = agent.listFiles(inputDirectory);
            for (FTPFile file : files) {
                String fileName = file.getName();
                if (file.isDirectory()) {
                    continue;
                }
                String inputFileName = inputDirectory + "/" + fileName;

                FtpOperationResult r = new FtpOperationResult();
                try {
                    byte[] bytes = agent.retrieveFile(inputFileName);
                    String message = handler.handleFile(bytes);
                    agent.deleteFile(inputFileName);
                    if (message == null) {
                        r.setMessage(String.format("Process file %s success.", inputFileName));
                    } else {
                        r.setMessage(message);
                    }
                    r.setSuccess(true);
                } catch (Throwable e) {
                    r.setError(e);
                }
                results.add(r);
            }
        });
        return results;
    }

    /**
     * Retrieve (download) file content as byte array (binary) and move it to the
     * archive location after a successful handling for each file in the input
     * directory. Continue next file if one has error.
     * 
     * @param inputDirectory
     * @param archiveDirectory
     * @param handler
     * @return
     */
    public List<FtpOperationResult> retrieveThenMoveAll(String inputDirectory, String archiveDirectory,
            FileHandler handler) {
        List<FtpOperationResult> results = new LinkedList<>();
        new DefaultTemplate().run(result -> {
            FtpAgent agent = server.getFtpAgent();
            FTPFile[] files = agent.listFiles(inputDirectory);
            for (FTPFile file : files) {
                String fileName = file.getName();
                if (file.isDirectory()) {
                    continue;
                }
                String inputFileName = inputDirectory + "/" + fileName;
                String archiveFileName = archiveDirectory + "/" + fileName;

                FtpOperationResult r = new FtpOperationResult();
                try {
                    byte[] bytes = agent.retrieveFile(inputFileName);
                    String message = handler.handleFile(bytes);
                    agent.storeFile(archiveFileName, bytes);
                    agent.deleteFile(inputFileName);
                    if (message == null) {
                        r.setMessage(String.format("Process file %s success.", inputFileName));
                    } else {
                        r.setMessage(message);
                    }
                    r.setSuccess(true);
                } catch (Throwable e) {
                    r.setError(e);
                }
                results.add(r);
            }
        });
        return results;
    }

    /**
     * Upload to ftp.
     * 
     * @param fileName
     * @param bytes
     * @return
     */
    public FtpOperationResult storeFile(String fileName, byte[] bytes) {
        return new DefaultTemplate().run(result -> {
            FtpAgent agent = server.getFtpAgent();
            agent.storeFile(fileName, bytes);
            result.setMessage(String.format("Upload file %s success.", fileName));
            result.setSuccess(true);
        });
    }
}
