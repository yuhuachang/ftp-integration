package com.example.ftpintegration.processor;

import java.io.IOException;

public interface FileProcessor {

    void processFile(byte[] bytes) throws IOException;
}
