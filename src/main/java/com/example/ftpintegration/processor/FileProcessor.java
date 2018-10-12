package com.example.ftpintegration.processor;

import java.io.IOException;
import java.util.List;

/**
 * Process one file. A file may contain one structured data or multiple data
 * with one data per line. It's up the the implemented class to decide how to
 * read and parse the content of an input file.
 * <p>
 * The implementation class does not need to handle file open and close. It can
 * assume the file is handled properly and the file content has been read into
 * byte array that passed in.
 */
public interface FileProcessor{

    <T> List<T> read(byte[] bytes, RecordMapper<T> mapper) throws IOException;
}
