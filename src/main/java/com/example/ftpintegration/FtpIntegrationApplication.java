package com.example.ftpintegration;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.example.ftpintegration.ftp.FtpTemplate;
import com.example.ftpintegration.ftp.flow.FtpFlowSynchronizor;
import com.example.ftpintegration.ftp.flow.InboundProcessingFlow;
import com.example.ftpintegration.processor.TextFileProcessor;

@SpringBootApplication
public class FtpIntegrationApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FtpIntegrationApplication.class).web(WebApplicationType.NONE).run(args);

        InboundProcessingFlow operation = new InboundProcessingFlow("/input", "/archive",
                new TextFileProcessor((lineNumber, line) -> {
                    if (lineNumber < 10) {
                        System.err.println("line " + lineNumber + ": " + line + " (" + line.length() + ")");
                    }
                    
                    if (line.contains("UPSSAV")) {
                        throw new IllegalArgumentException("oh! this value is incorrect...");
                    }
                }));

        operation.setFtpFlowSynchronizor(new FtpFlowSynchronizor() {
            
            @Override
            public void onSuccess(String fileName) {
                System.err.println("send email to tell user processing edi success.");
            }

            @Override
            public void onFailure(String fileName, String message, Throwable cause) {
                System.err.println("send email to tell user processing edi failed.");
            }

            @Override
            public void onFileError(String fileName, String message, Throwable cause) {
                System.err.println("send email to tell IT and user edi file is incorrect.");
            }

            @Override
            public void onFtpError(String message, Throwable cause) {
                System.err.println("technical error. send email to IT. " + message);
            }            
        });

        FtpTemplate template = new FtpTemplate("localhost", 21, "user", "password", FTPClientConfig.SYST_UNIX, 10000);
        template.execute(operation);
    }
}
