package com.example.ftpintegration;

import org.apache.commons.net.ftp.FTPClient;

public interface FtpOperationFlow {

    void execute(FTPClient client);
}
