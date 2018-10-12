package com.example.ftpintegration.processor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        
        
//        CharsetMatch[] matches = detector.detectAll();
//        for (CharsetMatch m : matches) {
//            System.err.println(m);
//        }
//        for (String s : CharsetDetector.getAllDetectableCharsets()) {
//            System.err.println(s);
//        }
        
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
