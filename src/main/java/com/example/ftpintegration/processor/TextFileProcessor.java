package com.example.ftpintegration.processor;

import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TextFileProcessor implements FileProcessor {

    private static final Logger log = LoggerFactory.getLogger(TextFileProcessor.class);

    private final Charset charset;

    public TextFileProcessor() {
        this(StandardCharsets.UTF_8);
    }

    public TextFileProcessor(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void processFile(byte[] bytes) throws IOException {

        String charsetName;
        if (charset == null) {
            // Auto detect charset
            log.info("Auto detect file encoding.");

            CharsetDetector detector = new CharsetDetector();
            detector.setText(bytes);
            CharsetMatch matchedCharset = detector.detect();
            if (matchedCharset == null) {
                log.info("Cannot find matched charset. Set charset to default UTF-8");
                charsetName = StandardCharsets.UTF_8.name();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Matched charset is {}", matchedCharset.getName());
                }
                charsetName = matchedCharset.getName();
            }
        } else {
            // Use provided charset
            charsetName = charset.name();
        }

        log.info("Use charset {}", charsetName);
        String content = new String(bytes, charsetName);
        processContent(content);
    }

    protected abstract void processContent(final String content) throws IOException;
}
