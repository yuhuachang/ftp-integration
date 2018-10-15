package com.example.ftpintegration.ftp.handler.impl;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.ftpintegration.ftp.handler.CharsetDetectionUtils;
import com.example.ftpintegration.ftp.handler.RecordHandler;
import com.example.ftpintegration.ftp.handler.RecordMapper;

public class CsvFileHandler<T> extends GenericFileHandler<CSVRecord, T> {

    private static final Logger log = LoggerFactory.getLogger(CsvFileHandler.class);

    private final char delimiter;
    private final Charset charset;

    public CsvFileHandler(char delimiter, RecordMapper<CSVRecord, T> mapper, RecordHandler<T> handler) {
        this(delimiter, mapper, handler, null);
    }

    public CsvFileHandler(char delimiter, RecordMapper<CSVRecord, T> mapper, RecordHandler<T> handler,
            Charset charset) {
        super(mapper, handler);
        this.delimiter = delimiter;
        this.charset = charset;
    }

    @Override
    public List<CSVRecord> getSourceObjectList(byte[] bytes) throws Throwable {
        String charsetName = CharsetDetectionUtils.detect(charset, bytes);
        String content = new String(bytes, charsetName);

        List<CSVRecord> list = new LinkedList<>();
        try (Reader reader = new StringReader(content)) {
            Iterable<CSVRecord> records = CSVFormat.newFormat(delimiter).withIgnoreEmptyLines().withTrim(true)
                    .parse(reader);

            for (CSVRecord record : records) {

                // ignore empty lines
                if (record.size() == 1 && record.get(0).trim().length() == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("line {}: (empty line)", record.getRecordNumber());
                    }
                    continue;
                }
                if (log.isDebugEnabled()) {
                    log.debug("line {}: {}", record.getRecordNumber(), record);
                }

                list.add(record);
            }
        }

        return list;
    }

}
