package com.example.ftpintegration.processor;

import java.io.UnsupportedEncodingException;

public interface FileProcessor {

    void processFile(byte[] bytes) throws UnsupportedEncodingException;
}
