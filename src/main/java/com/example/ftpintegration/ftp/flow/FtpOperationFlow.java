package com.example.ftpintegration.ftp.flow;

import org.apache.commons.net.ftp.FTPClient;

public abstract class FtpOperationFlow {

    protected FtpFlowSynchronizor synchronizor;

    public void setFtpFlowSynchronizor(FtpFlowSynchronizor synchronizor) {
        this.synchronizor = synchronizor;
    }

    public FtpFlowSynchronizor getFtpFlowSynchronizor() {
        return synchronizor;
    }

    public abstract void execute(FTPClient client);
}
