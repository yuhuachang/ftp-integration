package com.example.ftpintegration.processor;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CsvProcessor implements RecordProcessor {

    private static final Logger log = LoggerFactory.getLogger(CsvProcessor.class);

    private final String splitter;
    private final String[] fieldNames;
    private final boolean ignoreHeader;

    public CsvProcessor(String splitter, String[] fieldNames, boolean ignoreHeader) {
        splitter.getClass();
        fieldNames.getClass();
        this.splitter = splitter;
        this.fieldNames = fieldNames;
        this.ignoreHeader = ignoreHeader;
    }

    @Override
    public void process(int lineNumber, String line) throws Throwable {
        if (ignoreHeader && lineNumber <= 1) {
            log.info("Ignore header (first) line in CSV file.");
            return;
        }
        String[] values = line.split(splitter);
        if (values.length != fieldNames.length) {
            String message = String.format("Expect %d fields but only have %d at line %d", fieldNames.length,
                    values.length, lineNumber);
            log.error(message);
            throw new RuntimeException(message);
        }
        Map<String, String> valueMap = new HashMap<>(fieldNames.length);
        for (int i = 0; i < fieldNames.length; i++) {
            valueMap.put(fieldNames[i], values[i].trim());
        }
        process(valueMap);
    }

    protected abstract void process(Map<String, String> valueMap) throws Throwable;
}
