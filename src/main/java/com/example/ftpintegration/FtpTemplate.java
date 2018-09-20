package com.example.ftpintegration;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpTemplate {

    private static final Logger log = LoggerFactory.getLogger(FtpTemplate.class);

    private String host;
    private int port;
    private String username;
    private String password;
    private boolean isPassiveMode;
    private FTPClient client;
    private ErrorCallback technicalErrorCallback;

    public FtpTemplate(FTPClient client) {
        this.host = "mock-host";
        this.port = 21;
        this.username = "mock-user";
        this.password = "mock-password";
        this.isPassiveMode = true;
        this.client = client;
    }

    public FtpTemplate(String host, int port, String username, String password, String serverType,
            int timeout) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        // Create ftp client
        client = new FTPClient();

        // Set server type.
        FTPClientConfig config = new FTPClientConfig(serverType);
        client.configure(config);
        client.setConnectTimeout(timeout);
        client.setControlKeepAliveReplyTimeout(timeout);
        client.setControlKeepAliveTimeout(timeout);
        client.setDataTimeout(timeout);
        client.setDefaultTimeout(timeout);
        
       
    }

    public void setTechnicalErrorCallback(ErrorCallback technicalErrorCallback) {
        this.technicalErrorCallback = technicalErrorCallback;
    }

    public void execute(FtpOperationFlow operation) {
        try {
            client.connect(host, port);

            int reply = client.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {

                // Enter passive mode
                client.enterLocalPassiveMode();
                client.enterRemotePassiveMode();

                if (client.login(username, password)) {
                    try {
                        // execute operation
                        operation.execute(client);
                    } catch (Throwable e) {
                        // operation should handle all exception by itself.
                        // the template is not able to handle errors in business flow.
                        String message = "Detected a technical problem in the ftp operation flow.";
                        log.error(message, e);
                        if (technicalErrorCallback != null) {
                            technicalErrorCallback.callback(message, e);
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
                    if (technicalErrorCallback != null) {
                        technicalErrorCallback.callback(message, null);
                    }
                }
            } else {
                String message = String.format("Failed to connect FTP server %s", host);
                log.error(message);
                if (technicalErrorCallback != null) {
                    technicalErrorCallback.callback(message, null);
                }
            }
        } catch (Throwable e) {
            String message = "Error on open FTP connection.";
            log.error(message, e);
            if (technicalErrorCallback != null) {
                technicalErrorCallback.callback(message, e);
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
}
