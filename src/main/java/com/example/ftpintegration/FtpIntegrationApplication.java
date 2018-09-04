package com.example.ftpintegration;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class FtpIntegrationApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FtpIntegrationApplication.class).web(WebApplicationType.NONE).run(args);
    }
}
