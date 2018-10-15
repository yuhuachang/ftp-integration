package com.example.ftpintegration.ftp.handler.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.example.ftpintegration.ftp.handler.RecordMapper;

public class ExcelFileGenerator<T> extends GenericFileGenerator<Row, T> {

    public ExcelFileGenerator(RecordMapper<Row, T> mapper) {
        super(mapper);
    }

    @Override
    public byte[] createFileContent(List<T> records) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            
            int rownum = 0;
            for (T o : records) {
                Object[] values = mapper.targetToSource(o);
                Row row = sheet.createRow(rownum);
                for (int i = 0; i < values.length; i++) {
                    Object value = values[i];
                    if (value != null) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(value.toString());
                    }
                }
                rownum++;
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                wb.write(out);
                return out.toByteArray();
            }
        }
    }

}
