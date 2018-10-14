package com.example.ftpintegration.ftp.exception;

/**
 * Ftp login related exception.
 * 
 * @author Yu-Hua Chang
 *
 */
public class FtpModeSwitchException extends FtpException {

    private static final long serialVersionUID = 1L;

    public FtpModeSwitchException(String message) {
        super(message);
    }

    public FtpModeSwitchException(Throwable cause) {
        super(cause);
    }

    public FtpModeSwitchException(String message, Throwable cause) {
        super(message, cause);
    }
}
