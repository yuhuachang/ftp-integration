package com.example.ftpintegration.ftp.flow;

public interface FtpFlowSynchronizer {

    void onSuccess(String fileName);
    
    void onFailure(String fileName, String message, Throwable cause);
    
    void onFtpError(String message, Throwable cause);
    
    void onFileError(String fileName, String message, Throwable cause);
}