package com.example.ftpintegration.processor;

import java.io.IOException;

/**
 * An interface used by {@link FileProcessor} for mapping records in an EDI
 * file. Implementations of this interface perform the actual work of mapping
 * each record in the file but don't need to worry about exception handling. All
 * exceptions will be handled by integration flow.
 * 
 * @param <T>
 */
public interface RecordMapper<T> {

    T mapRecord(String[] values) throws IOException;
    
    String[] toValues(T record) throws IOException;
}
