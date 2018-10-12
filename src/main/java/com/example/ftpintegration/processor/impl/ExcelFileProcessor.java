package com.example.ftpintegration.processor.impl;

import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.ftpintegration.processor.CharsetDetectionUtils;
import com.example.ftpintegration.processor.FileProcessor;
import com.example.ftpintegration.processor.RecordMapper;

public class ExcelFileProcessor<T> implements FileProcessor {

    private static final Logger log = LoggerFactory.getLogger(ExcelFileProcessor.class);

    @Override
    public <T> List<T> read(byte[] bytes, RecordMapper<T> mapper) throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            if (log.isDebugEnabled()) {
                log.debug("Try read Excel file in OOXML (Office Open XML) format (*.xlsx files)");
            }

            // XSSFWorkbook, InputStream, needs more memory
            try (OPCPackage pkg = OPCPackage.open(is)) {
                try (XSSFWorkbook wb = new XSSFWorkbook(pkg)) {
                    handleWorkbook(wb);
                }
            } catch (OLE2NotOfficeXmlFileException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Not a OOXML file.");
                    log.debug("Try read Excel file in OLE2 format (*.xls files)");
                }

                // HSSFWorkbook, InputStream, needs more memory
                // try (NPOIFSFileSystem fs = new NPOIFSFileSystem(is)) {
                // try (HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true)) {
                // handleWorkbook(wb);
                // }
                // }
            } catch (Throwable e) {
                throw new IOException(e);
            }
        }
        return null;
    }
//
//    @Override
//    public byte[] create(List<T> records, RecordMapper<T> mapper) throws IOException {
//        // TODO Auto-generated method stub
//        return null;
//    }

    private void handleWorkbook(Workbook workbook) {

        if (workbook.getNumberOfSheets() < 1) {
            throw new RuntimeException("There is no worksheet in this Excel file.");
        }

        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            // row.getCell(5).get
            String v5 = row.getCell(5).getRichStringCellValue().getString();
            String v6 = row.getCell(6).getRichStringCellValue().getString();
            String v12 = row.getCell(12).getRichStringCellValue().getString();
            String v14 = row.getCell(14).getRichStringCellValue().getString();

            System.err.println(v5 + " -> " + CharsetDetectionUtils.detect(v5.getBytes()));
            System.err.println(v6 + " -> " + CharsetDetectionUtils.detect(v6.getBytes()));
            System.err.println(v12 + " -> " + CharsetDetectionUtils.detect(v12.getBytes()));
            System.err.println(v14 + " -> " + CharsetDetectionUtils.detect(v14.getBytes()));
        }
    }
}
