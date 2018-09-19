package com.example.ftpintegration;

import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
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
    public void processFile(byte[] bytes) {

        String charsetName;
        if (charset == null) {
            // Auto detect charset
            log.info("Auto detect file encoding.");

            CharsetDetector detector = new CharsetDetector();
            detector.setText(bytes);
            CharsetMatch matchedCharset = detector.detect();
            
            log.info("Matched charset is {}", matchedCharset.getName());
            charsetName = matchedCharset.getName();
        } else {
            // Use provided charset
            charsetName = charset.name();
        }

        try {
            String content = new String(bytes, charsetName);
            int lineNumber = 1;
            for (String line : content.split("[\\r\\n]")) {
                if (line.length() == 0) {
                    continue;
                }
                processor.process(lineNumber, line);
                lineNumber++;
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to read file.", e);
        }
    }
}
