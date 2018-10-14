package com.example.ftpintegration.ftp.exception;

/**
 * Ftp related exception.
 * 
 * @author Yu-Hua Chang
 *
 */
public class FtpRetrieveFileException extends Exception {

    private static final long serialVersionUID = 1L;

    public FtpRetrieveFileException(String message) {
        super(message);
    }

    public FtpRetrieveFileException(Throwable cause) {
        super(cause);
    }

    public FtpRetrieveFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
