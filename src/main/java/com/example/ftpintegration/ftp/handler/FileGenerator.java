package com.example.ftpintegration.ftp.handler;

import java.io.IOException;
import java.util.List;

/**
 * Given a list of objects and convert them into byte array used for ftp upload.
 * 
 * @author Yu-Hua Chang
 *
 * @param <T>
 */
public interface FileGenerator<T> {

    byte[] createFileContent(List<T> records) throws IOException;
}
