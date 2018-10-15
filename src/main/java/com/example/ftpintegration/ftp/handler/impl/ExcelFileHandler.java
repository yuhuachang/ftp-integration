package com.example.ftpintegration.ftp.handler.impl;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.ftpintegration.ftp.handler.RecordHandler;
import com.example.ftpintegration.ftp.handler.RecordMapper;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * Handle Excel file; both xls and xlsx are supported and xlsx is tried first.
 * 
 * @author Yu-Hua Chang
 *
 * @param <T>
 */
public class ExcelFileHandler<T> extends GenericFileHandler<Row, T> {

    private static final Logger log = LoggerFactory.getLogger(ExcelFileHandler.class);

    public ExcelFileHandler(RecordMapper<Row, T> mapper, RecordHandler<T> handler) {
        super(mapper, handler);
    }

    @Override
    public List<Row> getSourceObjectList(byte[] bytes) throws Throwable {
        List<Row> list = new LinkedList<>();
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            if (log.isDebugEnabled()) {
                log.debug("Try read Excel file in OOXML (Office Open XML) format (*.xlsx files)");
            }
            try (XSSFWorkbook wb = new XSSFWorkbook(in)) {
                XSSFSheet sheet = wb.getSheetAt(0);
                for (Iterator<Row> iter = sheet.rowIterator(); iter.hasNext();) {
                    Row row = iter.next();
                    list.add(row);
                }
            } catch (OLE2NotOfficeXmlFileException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Not a OOXML file.");
                    log.debug("Try read Excel file in OLE2 format (*.xls files)");
                }
                try (HSSFWorkbook wb = new HSSFWorkbook(in)) {
                    HSSFSheet sheet = wb.getSheetAt(0);

                    for (Iterator<Row> iter = sheet.rowIterator(); iter.hasNext();) {
                        Row row = iter.next();
                        list.add(row);
                    }
                }
            }
        }
        return list;
    }
}
