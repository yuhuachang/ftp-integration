package com.example.ftpintegration;

import java.io.IOException;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.example.ftpintegration.ftp.FtpTemplate;
import com.example.ftpintegration.ftp.flow.FtpFlowSynchronizer;
import com.example.ftpintegration.ftp.flow.InboundProcessingFlow;
import com.example.ftpintegration.processor.CSVFileProcessor;

@SpringBootApplication
public class FtpIntegrationApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FtpIntegrationApplication.class).web(WebApplicationType.NONE).run(args);

        InboundProcessingFlow operation = new InboundProcessingFlow("/input", "/archive", new CSVFileProcessor('|') {
            @Override
            protected void processRecord(CSVRecord record) throws IOException {

                String sku = record.get(0);
                String clientId = record.get(1);
                String description = record.get(2);
                String unit = record.get(3);
                String commodityCode = record.get(4); // used for custom declaration
                boolean productHasSerialNumber = "".equals(record.get(5)); // need to scan SN during inbound and outbound
                boolean productIsDualUsed = "".equals(record.get(6)); // dual used??
                String productCategory = record.get(7); // A, B, C, D class. A is best selling product
                boolean productIsFragile = "".equals(record.get(8)); // need extra care when packing to prevent broken.
                boolean productIsValuable = "".equals(record.get(9)); // items is high priced and small. need extra care on packing to prevent stolen.
                
                System.err.println("sku = " + sku);
                System.err.println("clientId = " + clientId);
                System.err.println("description = " + description);
                System.err.println("unit = " + unit);
                System.err.println("commodityCode = " + commodityCode);
                System.err.println("productHasSerialNumber = " + productHasSerialNumber);
                System.err.println("productIsDualUsed = " + productIsDualUsed);
                System.err.println("productCategory = " + productCategory);
                System.err.println("productIsFragile = " + productIsFragile);
                System.err.println("productIsValuable = " + productIsValuable);
            }
        });

        operation.setFtpFlowSynchronizer(new FtpFlowSynchronizer() {

            @Override
            public void onSuccess(String fileName) {
                System.err.println("send email to tell user processing edi success.");
            }

            @Override
            public void onFailure(String fileName, String message, Throwable cause) {
                System.err.println("send email to tell user processing edi failed.");
                if (cause != null) {
                    cause.printStackTrace();
                }
            }

            @Override
            public void onFileError(String fileName, byte[] content, String message, Throwable cause) {
                System.err.println("send email to tell IT and user edi file is incorrect.");
                if (cause != null) {
                    cause.printStackTrace();
                }
            }

            @Override
            public void onFtpError(String message, Throwable cause) {
                System.err.println("technical error. send email to IT. " + message);
                if (cause != null) {
                    cause.printStackTrace();
                }
            }

            @Override
            public void onListing(int numberOfFiles) {
                System.err.println("numberOfFiles = " + numberOfFiles);
            }

            @Override
            public void onStart(String fileName) {
                System.err.println("start processing " + fileName);
            }
        });

        operation.setDryRun(true);
        
        FtpTemplate template = new FtpTemplate("localhost", 21, "user", "password", FTPClientConfig.SYST_UNIX, 10000);
        template.execute(operation);
    }
}
