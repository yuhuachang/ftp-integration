package com.example.ftpintegration.ftp;

import java.io.IOException;

/**
 * Handle input binary file content and return processing message (optional).
 * 
 * @author Yu-Hua Chang
 *
 */
public interface FileHandler {

    /**
     * Handle input binary file content as byte array. In case of any error or
     * exception, this method call should die with an exception. If the method
     * execution complete without any exception, the handling processing is success.
     * 
     * @param bytes
     *            file content
     * @return any information related to the handling process
     * @throws IOException
     */
    String handleFile(byte[] bytes) throws IOException;
}
