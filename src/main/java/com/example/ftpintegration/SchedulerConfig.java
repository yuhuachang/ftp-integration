package com.example.ftpintegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.example.ftpintegration.ftp.FtpTemplate;
import com.example.ftpintegration.ftp.flow.FtpFlowSynchronizer;
import com.example.ftpintegration.ftp.flow.InboundProcessingFlow;
import com.example.ftpintegration.processor.CSVFileProcessor;
import com.example.ftpintegration.processor.FileProcessor;

import io.micrometer.core.instrument.MeterRegistry;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableAsync
@EnableScheduling
public class SchedulerConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);

    private static final int PROCESS_TIMEOUT = 4000;
    private static final int PROCESS_DELAY = 1000;

    private final AtomicInteger listFileCount;

    @Autowired
    public SchedulerConfig(MeterRegistry meterRegistry) {
        listFileCount = meterRegistry.gauge("luckies.current", new AtomicInteger(0));
    }

    /**
     * Limited to 2 thread for scheduler. (if a scheduler process does not return,
     * that thread is gone!)
     * 
     * @return
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(2);
        return taskScheduler;
    }

    @Scheduled(fixedRate = PROCESS_TIMEOUT + PROCESS_DELAY)
    public void appleScheduler() {
        if (log.isDebugEnabled()) {
            log.info("Trigger FTP Pooler");
        }

        try {
            FileProcessor processor = new CSVFileProcessor('|') {

                @Override
                protected void processRecord(CSVRecord record) throws IOException {

                    String sku = record.get(0);
                    String clientId = record.get(1);
                    String description = record.get(2);
                    String unit = record.get(3);
                    String commodityCode = record.get(4); // used for custom declaration
                    boolean productHasSerialNumber = "".equals(record.get(5)); // need to scan SN during inbound and
                                                                               // outbound
                    boolean productIsDualUsed = "".equals(record.get(6)); // dual used??
                    String productCategory = record.get(7); // A, B, C, D class. A is best selling product
                    boolean productIsFragile = "".equals(record.get(8)); // need extra care when packing to prevent
                                                                         // broken.
                    boolean productIsValuable = "".equals(record.get(9)); // items is high priced and small. need extra
                                                                          // care on packing to prevent stolen.

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
            };

            InboundProcessingFlow operation = new InboundProcessingFlow("/input", "/archive", processor);

            operation.setFtpFlowSynchronizer(new FtpFlowSynchronizer() {

                @Override
                public void onSuccess(String fileName) {
                    listFileCount.set(1);
                    System.err.println("input file processing success.");
                }

                @Override
                public void onFileError(String fileName, byte[] content, String message, Throwable cause) {
                    System.err.println("input file has some error.");
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
                    listFileCount.set(numberOfFiles);
                    System.err.println("(for debug) list numberOfFiles = " + numberOfFiles);
                }

                @Override
                public void onStart(String fileName) {
                    System.err.println("(for debug) start processing " + fileName);
                }
            });

            operation.setDryRun(true);

            FtpTemplate template = new FtpTemplate("localhost", 21, "user", "password", FTPClientConfig.SYST_UNIX,
                    PROCESS_TIMEOUT);
            template.execute(operation);
        } catch (Throwable e) {
            String msg = "Error on sending Apple EDI 214";
            log.error(msg, e);
        }
    }
}
