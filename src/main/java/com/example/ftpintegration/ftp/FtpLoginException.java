package com.example.ftpintegration.ftp;

/**
 * Ftp related exception.
 * 
 * @author Yu-Hua Chang
 *
 */
public class FtpLoginException extends FtpException {

    private static final long serialVersionUID = 1L;

    public FtpLoginException(String message) {
        super(message);
    }

    public FtpLoginException(Throwable cause) {
        super(cause);
    }

    public FtpLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
