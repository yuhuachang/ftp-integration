package com.example.ftpintegration.processor;

public interface RecordProcessor {

    void process(int lineNumber, String line) throws Throwable;
}
