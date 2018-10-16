package com.example.ftpintegration.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * server info for creating a ftp connection.
 * 
 * @author Yu-Hua Chang
 *
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class FtpServer {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean isPassiveMode;
    private final FtpAgent agent;

    /**
     * 
     * @param host
     * @param port
     * @param username
     * @param password
     * @param serverType
     *            See constants in {@link FTPClientConfig}. Common values are:
     *            "UNIX", "UNIX_LTRIM", "WINDOWS".
     * @param isPassiveMode
     * @param timeout
     */
    public FtpServer(@JsonProperty(value = "host", required = true) String host,
            @JsonProperty(value = "port", required = true) int port,
            @JsonProperty(value = "username", required = true) String username,
            @JsonProperty(value = "password", required = true) String password,
            @JsonProperty(value = "is_passive_mode", required = true) boolean isPassiveMode,
            @JsonProperty(value = "timeout", required = true) int timeout) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.isPassiveMode = isPassiveMode;

        FTPClient client = new FTPClient();
        FTPClientConfig config = new FTPClientConfig();
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

    public boolean isPassiveMode() {
        return isPassiveMode;
    }

    public FtpAgent getFtpAgent() {
        return agent;
    }

    @Override
    public String toString() {
        return "ftp://" + username + ":" + password + "@" + host + ":" + port + " PassiveMode=" + isPassiveMode;
    }

}
