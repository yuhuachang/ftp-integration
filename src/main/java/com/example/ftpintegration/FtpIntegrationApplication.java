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
                }));
        operation.setSuccessCallback(message -> {
            System.err.println(message + " success");
        });
        operation.setErrorCallback(message -> {
            System.err.println(message + " fail");
        });
        new FtpTemplate("localhost", 21, "user", "password", FTPClientConfig.SYST_UNIX, 10000).execute(operation);
    }
}
