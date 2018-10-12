package com.example.ftpintegration.ftp;

public class FtpOperationResult {

    private String fileName;
    private boolean isSuccess;
    private String message;
    private Throwable error;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getMessage() {
        if (error != null) {
            return error.getMessage();
        }
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
        setSuccess(false);
    }

}
