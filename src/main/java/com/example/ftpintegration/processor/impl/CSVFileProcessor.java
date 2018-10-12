package com.example.ftpintegration.processor.impl;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.ftpintegration.processor.CharsetDetectionUtils;
import com.example.ftpintegration.processor.FileGenerator;
import com.example.ftpintegration.processor.FileProcessor;
import com.example.ftpintegration.processor.RecordMapper;

public class CSVFileProcessor implements FileProcessor, FileGenerator {

    private static final Logger log = LoggerFactory.getLogger(CSVFileProcessor.class);

    private final Charset charset;
    private final char delimiter;
    private String fileName;

    public CSVFileProcessor(char delimiter) {
        this(null, delimiter);
    }

    public CSVFileProcessor(Charset charset, char delimiter) {
        this.charset = charset;
        this.delimiter = delimiter;
    }

    @Override
    public <T> List<T> read(byte[] bytes, RecordMapper<T> mapper) throws IOException {

        String charsetName = CharsetDetectionUtils.detect(charset, bytes);
        String content = new String(bytes, charsetName);

        List<T> list = new LinkedList<>();
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

                // convert CSV record to string array
                String[] values = new String[record.size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = record.get(i);
                }

                T o = mapper.mapRecord(values);
                list.add(o);
            }
        }
        return list;
    }

    @Override
    public <T> byte[] create(List<T> records, RecordMapper<T> mapper) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (T record : records) {
            String[] values = mapper.toValues(record);
            String line = String.join("" + delimiter, values);
            sb.append(line);
        }
        return sb.toString().getBytes(charset);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

}
