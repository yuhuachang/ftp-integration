package com.example.ftpintegration.ftp;

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
     * execution complete without any exception, the handling processing is
     * success. The returning message is optional and is only for informative
     * purpose. A message can exist either for a success or failed execution.
     * 
     * @param bytes
     *            file content
     * @return any information related to the handling process
     * @throws Throwable
     */
    String handleFile(byte[] bytes) throws Throwable;
}
