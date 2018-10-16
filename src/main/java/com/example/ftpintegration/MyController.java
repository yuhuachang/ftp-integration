package com.example.ftpintegration;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.ftpintegration.ftp.FtpOperationResult;
import com.example.ftpintegration.ftp.FtpServer;
import com.example.ftpintegration.ftp.FtpTemplate;
import com.example.ftpintegration.ftp.handler.RecordMapper;
import com.example.ftpintegration.ftp.handler.impl.CsvFileHandler;

@RestController
public class MyController {

    private static final Logger log = LoggerFactory.getLogger(MyController.class);

    /**
     * Example to fetch a file from FTP.
     * 
     * @param request example: <pre>
     * {
  "server": {
    "host": "localhost",
    "port": 21,
    "username": "user",
    "password": "password",
    "is_passive_mode": true,
    "timeout": 1000
  },
  "fileName": "/test.txt"
}
     * </pre>
     * @return
     */
    @PostMapping("/show")
    public FtpOperationResult retrieveFile(@RequestBody RetrieveFileRequest request) {
        log.info("Receive retrieve ftp file request");

        FtpServer server = request.getServer();
        log.info("FTP: " + server);

        String fileName = request.getFileName();
        log.info("fileName: " + fileName);

        FtpTemplate template = new FtpTemplate(server);

        RecordMapper<CSVRecord, String[]> mapper = new RecordMapper<CSVRecord, String[]>() {

            @Override
            public Object[] targetToSource(String[] target) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String[] sourceToTarget(CSVRecord source) {
                String[] row = new String[source.size()];
                for (int i = 0; i < source.size(); i++) {
                    row[i] = source.get(i);
                }
                return row;
            }
        };

        CsvFileHandler<String[]> fileHandler = new CsvFileHandler<>('|', mapper, list -> {
            for (String[] row : list) {
                log.info(" -> " + String.join(", ", row));
            }
        });

        return template.retrieveFile(fileName, fileHandler);
    }
}
