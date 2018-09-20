package com.example.ftpintegration.processor;

import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextFileProcessor implements FileProcessor {

    private static final Logger log = LoggerFactory.getLogger(TextFileProcessor.class);

    private final Charset charset;
    private final RecordProcessor processor;

    public TextFileProcessor(RecordProcessor processor) {
        this(null, processor);
    }

    public TextFileProcessor(Charset charset, RecordProcessor processor) {
        processor.getClass();
        this.charset = charset;
        this.processor = processor;
    }

    @Override
    public void processFile(byte[] bytes) throws UnsupportedEncodingException {

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
        int lineNumber = 1;
        for (String line : content.split("[\\r\\n]")) {
            if (line.length() == 0) {
                continue;
            }
            if (log.isDebugEnabled()) {
                log.debug("line {}: {}", lineNumber, line);
            }
            try {
                processor.process(lineNumber, line);
            } catch (Throwable e) {
                String message = String.format("There is an error at line %d. %s", lineNumber, e.getMessage());
                throw new RuntimeException(message, e);
            }
            lineNumber++;
        }
    }
}
