package com.example.ftpintegration.ftp.exception;

/**
 * Ftp connection related exception.
 * 
 * @author Yu-Hua Chang
 *
 */
public class FtpConnectionException extends FtpException {

    private static final long serialVersionUID = 1L;

    public FtpConnectionException(String message) {
        super(message);
    }

    public FtpConnectionException(Throwable cause) {
        super(cause);
    }

    public FtpConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
