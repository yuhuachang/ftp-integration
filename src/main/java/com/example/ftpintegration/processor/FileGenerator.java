package com.example.ftpintegration.processor;

import java.io.IOException;
import java.util.List;

public interface FileGenerator {

    String getFileName();

    <T> byte[] create(List<T> records, RecordMapper<T> mapper) throws IOException;
}
