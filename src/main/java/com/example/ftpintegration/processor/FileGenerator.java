package com.example.ftpintegration.processor;

import java.io.IOException;

public interface FileGenerator {

    String getFileName();

    byte[] generateFile() throws IOException;
}
