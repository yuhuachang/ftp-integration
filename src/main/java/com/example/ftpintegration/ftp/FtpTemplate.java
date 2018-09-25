package com.example.ftpintegration.ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.ftpintegration.ftp.flow.FtpFlowSynchronizer;
import com.example.ftpintegration.ftp.flow.FtpOperationFlow;

/**
 * This class takes care of FTP connect/disconnect, login/logout, and execute
 * the operation flow. Detailed FTP operation after a successful connect/login
 * is defined in the {@link FtpOperationFlow}. This class can be created once,
 * and call {@link #execute(FtpOperationFlow)} many times with different
 * operation flows.
 * 
 * @author Yu-Hua Chang
 */
public class FtpTemplate {

    private static final Logger log = LoggerFactory.getLogger(FtpTemplate.class);

    private String host;
    private int port;
    private String username;
    private String password;
    private FTPClient client;

    public FtpTemplate(String host, int port, String username, String password, String serverType, int timeout) {
        this(new FTPClient(), host, port, username, password, serverType, timeout);
    }

    private FtpTemplate(FTPClient client, String host, int port, String username, String password, String serverType,
            int timeout) {
        this.client = client;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        // Set server type.
        FTPClientConfig config = new FTPClientConfig(serverType);
        client.configure(config);
        client.setConnectTimeout(timeout);
        client.setControlKeepAliveReplyTimeout(timeout);
        client.setControlKeepAliveTimeout(timeout);
        client.setDataTimeout(timeout);
        client.setDefaultTimeout(timeout);
    }

    public void execute(FtpOperationFlow operation) {
        FtpFlowSynchronizer synchronizer = operation.getFtpFlowSynchronizer();
        try {
            client.connect(host, port);

            int reply = client.getReplyCode();
            if (log.isDebugEnabled()) {
                log.debug("connection reply code = {}", reply);
            }
            if (FTPReply.isPositiveCompletion(reply)) {

                // Enter passive mode
                client.enterLocalPassiveMode();
                client.enterRemotePassiveMode();

                if (client.login(username, password)) {
                    try {
                        // execute operation
                        operation.execute(client);
                    } catch (IOException e) {
                        // operation should handle all exception by itself. the template is not able to
                        // handle errors in business flow.
                        String message = "Detected a technical problem in the ftp operation flow.";
                        log.error(message, e);
                        if (synchronizer != null) {
                            synchronizer.onFtpError(message, e);
                        }
                    }

                    // logout
                    if (client.logout()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Logout FTP");
                        }
                    } else {
                        log.warn("Failed to logout FTP");
                    }
                } else {
                    // login failed.
                    String message = String.format("Failed to login FTP server %s with %s/%s.", host, username,
                            password);
                    log.error(message);
                    if (synchronizer != null) {
                        synchronizer.onFtpError(message, null);
                    }
                }
            } else {
                String message = String.format("Failed to connect FTP server %s", host);
                log.error(message);
                if (synchronizer != null) {
                    synchronizer.onFtpError(message, null);
                }
            }
        } catch (Throwable e) {
            String message = "Error on open FTP connection.";
            log.error(message, e);
            if (synchronizer != null) {
                synchronizer.onFtpError(message, null);
            }
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

    @Override
    public String toString() {
        return String.format("FTP %s/%s@%s:%d", username, password, host, port);
    }
}
