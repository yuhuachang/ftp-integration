package com.example.ftpintegration.ftp.exception;

/**
 * Ftp related exception.
 * 
 * @author Yu-Hua Chang
 *
 */
public class FtpStoreFileException extends Exception {

    private static final long serialVersionUID = 1L;

    public FtpStoreFileException(String message) {
        super(message);
    }

    public FtpStoreFileException(Throwable cause) {
        super(cause);
    }

    public FtpStoreFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
