package com.example.ftpintegration.ftp;

public class FtpOperationResult {

    private boolean isSuccess;
    private String message;
    private Throwable error;

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
