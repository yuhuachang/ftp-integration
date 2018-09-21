package com.example.ftpintegration.processor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CSVFileProcessor extends TextFileProcessor {

    private static final Logger log = LoggerFactory.getLogger(CSVFileProcessor.class);

    private final char delimiter;

    public CSVFileProcessor(char delimiter) {
        super();
        this.delimiter = delimiter;
    }

    public CSVFileProcessor(Charset charset, char delimiter) {
        super(charset);
        this.delimiter = delimiter;
    }

    @Override
    protected void processContent(final String content) throws IOException {       
        try (Reader reader = new StringReader(content)) {
            Iterable<CSVRecord> records = CSVFormat.newFormat(delimiter)
                    .withIgnoreEmptyLines()
                    .withTrim(true)
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

                processRecord(record);
            }
        }
    }

    protected abstract void processRecord(final CSVRecord record) throws IOException;
}
