package com.example.ftpintegration.ftp.handler.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.example.ftpintegration.ftp.handler.RecordMapper;

public class CsvFileGenerator<T> extends GenericFileGenerator<CSVRecord, T> {

    private final char delimiter;
    private final Charset charset;

    public CsvFileGenerator(char delimiter, RecordMapper<CSVRecord, T> mapper) {
        this(delimiter, mapper, StandardCharsets.UTF_8);
    }

    public CsvFileGenerator(char delimiter, RecordMapper<CSVRecord, T> mapper, Charset charset) {
        super(mapper);
        this.delimiter = delimiter;
        this.charset = charset;
    }

    @Override
    public byte[] createFileContent(List<T> records) throws IOException {

        CSVFormat format = CSVFormat.newFormat(delimiter).withIgnoreEmptyLines().withTrim(true);

        try (Writer writer = new StringWriter()) {
            try (CSVPrinter csvPrinter = new CSVPrinter(writer, format)) {
                for (T target : records) {
                    Object[] values = mapper.targetToSource(target);
                    csvPrinter.printRecord(values);
                }
                csvPrinter.flush();
            }
            return writer.toString().getBytes(charset);
        }
    }

}
