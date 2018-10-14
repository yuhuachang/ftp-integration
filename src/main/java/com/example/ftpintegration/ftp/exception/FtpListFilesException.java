package com.example.ftpintegration.ftp.exception;

/**
 * Ftp related exception.
 * 
 * @author Yu-Hua Chang
 *
 */
public class FtpListFilesException extends Exception {

    private static final long serialVersionUID = 1L;

    public FtpListFilesException(String message) {
        super(message);
    }

    public FtpListFilesException(Throwable cause) {
        super(cause);
    }

    public FtpListFilesException(String message, Throwable cause) {
        super(message, cause);
    }
}
