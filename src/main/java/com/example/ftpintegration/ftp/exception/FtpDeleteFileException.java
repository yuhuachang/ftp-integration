package com.example.ftpintegration.ftp.exception;

/**
 * Ftp related exception.
 * 
 * @author Yu-Hua Chang
 *
 */
public class FtpDeleteFileException extends Exception {

    private static final long serialVersionUID = 1L;

    public FtpDeleteFileException(String message) {
        super(message);
    }

    public FtpDeleteFileException(Throwable cause) {
        super(cause);
    }

    public FtpDeleteFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
