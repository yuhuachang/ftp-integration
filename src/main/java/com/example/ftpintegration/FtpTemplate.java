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
    private FTPClient client;

    public FtpTemplate(FTPClient client) {
        this.host = "mock-host";
        this.port = 21;
        this.username = "mock-user";
        this.password = "mock-password";
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

    public void execute(FtpOperationFlow operation) {
        try {
            client.connect(host, port);

            int reply = client.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {

                // TODO: do we need to test passive FTP?
                // enter passive mode
                // ftp.enterLocalPassiveMode();
                // ftp.enterRemotePassiveMode();

                if (client.login(username, password)) {
                    try {
                        // execute operation
                        operation.execute(client);
                    } catch (Throwable e) {
                        // operation should handle all exception by itself.
                        // the template is not able to handle errors in business flow.
                        log.warn("Detected an unhandled exception from ftp operation flow.", e);
                    }

                    // logout
                    if (client.logout()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Logout FTP");
                        }
                    } else {
                        log.info("Failed to logout FTP");
                    }
                } else {
                    // login failed.
                    log.warn("Failed to login FTP server {} with {}/{}.", host, username, password);
                }
            } else {
                log.warn("Failed to connect FTP server {}.", host);
            }
        } catch (Exception e) {
            log.warn("Error on open FTP connection.", e);
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
