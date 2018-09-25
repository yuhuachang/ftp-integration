package com.example.ftpintegration.ftp.flow;

public interface FtpFlowSynchronizer {

    /**
     * After listing the input folder.
     * 
     * @param numberOfFiles
     */
    void onListing(int numberOfFiles);

    /**
     * When start processing a file.
     * 
     * @param fileName
     */
    void onStart(String fileName);

    /**
     * After complete processing a file successfully.
     * 
     * @param fileName
     */
    void onSuccess(String fileName);

    /**
     * After failed to process a file.
     * 
     * @param fileName
     * @param message
     * @param cause
     */
    void onFailure(String fileName, String message, Throwable cause);

    /**
     * When a FTP error occur.
     * 
     * @param message
     * @param cause
     */
    void onFtpError(String message, Throwable cause);

    /**
     * When an error on the file was detected. For example, unable to open the
     * file, failed to decode the file and retrieve the file content.
     * 
     * @param fileName
     * @param message
     * @param cause
     */
    void onFileError(String fileName, String message, Throwable cause);
}
