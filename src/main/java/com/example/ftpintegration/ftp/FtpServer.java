package com.example.ftpintegration.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;

/**
 * server info for creating a ftp connection.
 * 
 * @author Yu-Hua Chang
 *
 */
public class FtpServer {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String serverType;
    private final FtpAgent agent;

    public FtpServer(String host, int port, String username, String password, String serverType, int timeout) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.serverType = serverType;

        FTPClient client = new FTPClient();
        FTPClientConfig config = new FTPClientConfig(getServerType());
        client.configure(config);
        client.setConnectTimeout(timeout);
        client.setControlKeepAliveReplyTimeout(timeout);
        client.setControlKeepAliveTimeout(timeout);
        client.setDataTimeout(timeout);
        client.setDefaultTimeout(timeout);

        agent = new FtpAgent(client);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getServerType() {
        return serverType;
    }

    public FtpAgent getFtpAgent() {
        return agent;
    }

    @Override
    public String toString() {
        return "ftp://" + username + "/" + password + "@" + host + ":" + port;
    }

}
