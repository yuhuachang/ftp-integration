package com.example.ftpintegration.ftp.handler.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import com.example.ftpintegration.ftp.handler.FileHandler;
import com.example.ftpintegration.ftp.handler.RecordMapper;
import com.example.ftpintegration.ftp.handler.impl.CsvFileHandler;
import com.example.ftpintegration.ftp.handler.impl.test.Order;

public class CsvFileHandlerTest {

    private RecordMapper<CSVRecord, Order> mapper = new RecordMapper<CSVRecord, Order>() {
        @Override
        public Order sourceToTarget(CSVRecord source) {
            Order o = new Order();
            o.setOrderNumber(source.get(0));
            o.setCustomerName(source.get(1));
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

    /**
     * In CSV file, empty lines will be skipped and extra fields will be ignored.
     * 
     * @throws Throwable
     */
    @Test
    public void handleFileSuccess() throws Throwable {

        // test data with empty lines and extra fields.
        String content = String.join("\n", "ORD001|John", "", "   ", "ORD002|Marry", "ORD003|Mike|extra");

        FileHandler handler = new CsvFileHandler<Order>('|', mapper, list -> {

            assertEquals(3, list.size());

            assertEquals("ORD001", list.get(0).getOrderNumber());
            assertEquals("John", list.get(0).getCustomerName());

            assertEquals("ORD002", list.get(1).getOrderNumber());
            assertEquals("Marry", list.get(1).getCustomerName());

            assertEquals("ORD003", list.get(2).getOrderNumber());
            assertEquals("Mike", list.get(2).getCustomerName());
        });

        handler.handleFile(content.getBytes());
    }

    /**
     * There will be any error if the fields in one line in a CSV file is
     * insufficient.
     * 
     * @throws Throwable
     */
    @Test
    public void handleFileFailure() throws Throwable {

        // test data with missing fields and will die.
        String content = String.join("\n", "ORD001|John", "", "   ", "ORD002", "ORD003|Mike|extra");

        FileHandler handler = new CsvFileHandler<Order>('|', mapper, list -> {
            fail("shouldn't be here. here is the result. should die before this.");
        });

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            handler.handleFile(content.getBytes());
        });
    }

}
