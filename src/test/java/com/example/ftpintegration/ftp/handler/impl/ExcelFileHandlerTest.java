package com.example.ftpintegration.ftp.handler.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Row;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.ftpintegration.ftp.handler.FileHandler;
import com.example.ftpintegration.ftp.handler.RecordMapper;
import com.example.ftpintegration.ftp.handler.impl.ExcelFileHandler;
import com.example.ftpintegration.ftp.handler.impl.test.Order;

public class ExcelFileHandlerTest {

    private RecordMapper<Row, Order> mapper = new RecordMapper<Row, Order>() {
        @Override
        public Order sourceToTarget(Row source) {
            Order o = new Order();
            o.setOrderNumber(source.getCell(0).getStringCellValue());
            o.setCustomerName(source.getCell(1).getStringCellValue());
            return o;
        }

        @Override
        public Object[] targetToSource(Order target) {
            Object[] o = new Object[2];
            o[0] = target.getOrderNumber();
            o[1] = target.getCustomerName();
            return o;
        }
    };

    public byte[] readFile(String fileName) throws IOException {
        ClassLoader classLoader = ExcelFileHandlerTest.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        try (FileInputStream input = new FileInputStream(file)) {
            return IOUtils.toByteArray(input);
        }
    }

    /**
     * Both xls and xlsx format are supported and result should be the same (xlsx
     * will be tried first). There is no such concept of empty lines (rows) in an
     * Excel file. Therefore, empty rows is also read. Mapper may throw exception on
     * invalid values.
     * 
     * @param fileName
     * @throws Throwable
     */
    @ParameterizedTest
    @ValueSource(strings = { "test1.xls", "test1.xlsx" })
    public void handleXlsFile(String fileName) throws Throwable {

        FileHandler handler = new ExcelFileHandler<Order>(mapper, list -> {

            assertEquals(3, list.size());

            assertEquals("ORD001", list.get(0).getOrderNumber());
            assertEquals("John", list.get(0).getCustomerName());

            assertEquals("ORD002", list.get(1).getOrderNumber());
            assertEquals("Marry", list.get(1).getCustomerName());

            assertEquals("ORD003", list.get(2).getOrderNumber());
            assertEquals("Mike", list.get(2).getCustomerName());
        });

        byte[] bytes = readFile(fileName);
        handler.handleFile(bytes);
    }
}
