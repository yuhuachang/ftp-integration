package com.example.ftpintegration;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;

import java.nio.charset.Charset;

public class TextFileProcessor implements FileProcessor {

    private final Charset charset;
    private final RecordProcessor processor;

    public TextFileProcessor(RecordProcessor processor) {
        this(StandardCharsets.UTF_8, processor);
    }

    public TextFileProcessor(Charset charset, RecordProcessor processor) {
        Assert.assertNotNull(charset);
        Assert.assertNotNull(processor);
        this.charset = charset;
        this.processor = processor;
    }

    @Override
    public void processFile(byte[] bytes) {
        String content = new String(bytes, charset);
        for (String line : content.split("[\\r\\n]")) {
            if (line.length() == 0) {
                continue;
            }
            processor.process(line);
        }
    }
}
