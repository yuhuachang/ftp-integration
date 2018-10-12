package com.example.ftpintegration.ftp;

public class FtpServer {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String serverType;

    public FtpServer(String host, int port, String username, String password, String serverType) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.serverType = serverType;
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

    @Override
    public String toString() {
        return "FtpServer [host=" + host + ", port=" + port + ", username=" + username + ", password=" + password
                + ", serverType=" + serverType + "]";
    }

}
