package com.example.ftpintegration.ftp.handler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to "guess" the encoding of a plain text file with UTF-8 as the
 * default charset.
 * 
 * @author yu
 *
 */
public class CharsetDetectionUtils {

    private static final Logger log = LoggerFactory.getLogger(CharsetDetectionUtils.class);

    public static String detect(byte[] content) {
        return detect(StandardCharsets.UTF_8, content);
    }

    public static String detect(Charset defaultCharset, byte[] content) {
        if (log.isDebugEnabled()) {
            log.debug("Auto detect encoding for {}", content);
        }
        CharsetDetector detector = new CharsetDetector();
        detector.setText(content);

        CharsetMatch matchedCharset = detector.detect();
        if (matchedCharset == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot find matched charset. Set charset to default charset {}", defaultCharset);
            }
            return defaultCharset.name();
        }
        if (log.isDebugEnabled()) {
            log.debug(matchedCharset.toString());
        }
        return matchedCharset.getName();
    }
}
