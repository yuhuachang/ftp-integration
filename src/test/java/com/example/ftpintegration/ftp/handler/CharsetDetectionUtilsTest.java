package com.example.ftpintegration.ftp.handler;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.example.ftpintegration.ftp.handler.CharsetDetectionUtils;

public class CharsetDetectionUtilsTest {

    @BeforeClass
    public static void beforeClass() {
        // System.setProperty("java.util.logging.config.file",
        // ClassLoader.getSystemResource("logging.properties").getPath());
        System.setProperty("logging.level.com.example", "DEBUG");
    }

    /**
     * GB2312 (Simplified Chinese)
     * 
     * http://ash.jp/code/cn/gb2312tbl.htm
     * 
     * @throws UnsupportedEncodingException
     */
    @Test
    public void detectGB2312() throws UnsupportedEncodingException {
        byte[] content = new byte[] { (byte) 0xB1, (byte) 0xA1, (byte) 0xB1, (byte) 0xA2, (byte) 0xB1, (byte) 0xB0,
                (byte) 0xB1, (byte) 0xB1, (byte) 0xB1, (byte) 0xB2, (byte) 0xB1, (byte) 0xB3, (byte) 0xB1, (byte) 0xB4,
                (byte) 0xB1, (byte) 0xB5, (byte) 0xB1, (byte) 0xB6, (byte) 0xB1, (byte) 0xB7, (byte) 0xB1, (byte) 0xB8,
                (byte) 0xB1, (byte) 0xB9, (byte) 0xB1, (byte) 0xBA, (byte) 0xB1, (byte) 0xBB, (byte) 0xB1, (byte) 0xBC,
                (byte) 0xB1, (byte) 0xBD };
        String cs = CharsetDetectionUtils.detect(content);
        System.out.println(new String(content, "GB2312") + " " + cs);
    }

    /**
     * Big5 (Traditional Chinese) character code table
     * 
     * http://ash.jp/code/cn/big5tbl.htm
     * 
     * @throws UnsupportedEncodingException
     */
    @Test
    public void detectBig5() throws UnsupportedEncodingException {
        byte[] content = new byte[] { (byte) 0xB9, (byte) 0x40, (byte) 0xB9, (byte) 0x41, (byte) 0xB9, (byte) 0x42,
                (byte) 0xB9, (byte) 0x43, (byte) 0xB9, (byte) 0x44, (byte) 0xB9, (byte) 0x45, (byte) 0xB9, (byte) 0x46,
                (byte) 0xB9, (byte) 0x47, (byte) 0xB9, (byte) 0x48, (byte) 0xB9, (byte) 0x49, (byte) 0xB9, (byte) 0x4A,
                (byte) 0xB9, (byte) 0x4B, (byte) 0xB9, (byte) 0x4C, (byte) 0xB9, (byte) 0x4D, };
        
        String cs = CharsetDetectionUtils.detect(content);
        System.out.println(new String(content, "BIG5") + " " + cs);
    }
}
