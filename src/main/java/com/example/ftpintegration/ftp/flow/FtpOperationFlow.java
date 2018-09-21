package com.example.ftpintegration.ftp.flow;

import org.apache.commons.net.ftp.FTPClient;

public abstract class FtpOperationFlow {

    protected FtpFlowSynchronizer synchronizor;

    public void setFtpFlowSynchronizor(FtpFlowSynchronizer synchronizor) {
        this.synchronizor = synchronizor;
    }

    public FtpFlowSynchronizer getFtpFlowSynchronizer() {
        return synchronizor;
    }

    public abstract void execute(FTPClient client);
}
