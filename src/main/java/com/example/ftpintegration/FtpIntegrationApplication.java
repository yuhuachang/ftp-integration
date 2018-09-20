package com.example.ftpintegration;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class FtpIntegrationApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FtpIntegrationApplication.class).web(WebApplicationType.NONE).run(args);

        InboundProcessingFlow operation = new InboundProcessingFlow("/input", "/archive",
                new TextFileProcessor((lineNumber, line) -> {
                    System.err.println("line " + lineNumber + ": " + line + " (" + line.length() + ")");
                    
                    if (line.contains("UPSSAV")) {
                        throw new BusinessException("oh! this value is incorrect...", null);
                    }
                }));
        operation.setSuccessCallback((message) -> {
            System.err.println("send email to tell user processing edi success.");
        });
        operation.setErrorCallback((message, cause) -> {
            System.err.println("send email to tell user processing edi failed.");
        });

        FtpTemplate template = new FtpTemplate("localhost", 21, "user", "password", FTPClientConfig.SYST_UNIX, 10000);
        template.setTechnicalErrorCallback((message, cause) -> {
            System.err.println("technical error. send email to IT. " + message);
        });
        template.execute(operation);
    }
}
