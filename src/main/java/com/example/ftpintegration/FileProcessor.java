package com.example.ftpintegration;

import java.io.UnsupportedEncodingException;

public interface FileProcessor {

    void processFile(byte[] bytes) throws UnsupportedEncodingException;
}
