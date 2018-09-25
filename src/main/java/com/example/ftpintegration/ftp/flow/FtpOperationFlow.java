package com.example.ftpintegration.ftp.flow;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;

public abstract class FtpOperationFlow {

    protected FtpFlowSynchronizer synchronizer;

    public void setFtpFlowSynchronizer(FtpFlowSynchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }

    public FtpFlowSynchronizer getFtpFlowSynchronizer() {
        return synchronizer;
    }

    public abstract void execute(FTPClient client) throws IOException;
}
