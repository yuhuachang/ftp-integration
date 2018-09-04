package com.example.ftpintegration.inbound;

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

    public static interface FtpOperation {
        void execute(FTPClient client) throws IOException;
    }

    public static enum ServerType {
        WINDOWS, UNIX
    }

    public FtpTemplate(FTPClient client) {
        this.host = "mock-host";
        this.port = 21;
        this.username = "mock-user";
        this.password = "mock-password";
        this.client = client;
    }

    public FtpTemplate(String host, int port, String username, String password, ServerType serverType, int timeout) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        // Create ftp client
        client = new FTPClient();

        // Set server type.
        FTPClientConfig config = null;
        switch (serverType) {
        case WINDOWS:
            config = new FTPClientConfig(FTPClientConfig.SYST_NT);
            break;
        case UNIX:
            config = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
            break;
        }
        client.configure(config);

        client.setConnectTimeout(timeout);
        client.setControlKeepAliveReplyTimeout(timeout);
        client.setControlKeepAliveTimeout(timeout);
        client.setDataTimeout(timeout);
        client.setDefaultTimeout(timeout);
    }

    public void execute(FtpOperation operation) {
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
                    } catch (Exception e) {
                        log.warn("error while executing ftp operation", e);
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
                    // nothing we can do here...
                    log.warn("Error on close FTP connection.", e);
                }
            }
        }
    }
}
