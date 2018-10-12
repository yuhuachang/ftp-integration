package com.example.ftpintegration.ftp;

/**
 * Ftp related exception.
 * 
 * @author Yu-Hua Chang
 *
 */
public class FtpException extends Exception {

    private static final long serialVersionUID = 1L;

    public FtpException(String message) {
        super(message);
    }

    public FtpException(Throwable cause) {
        super(cause);
    }

    public FtpException(String message, Throwable cause) {
        super(message, cause);
    }
}
