package com.example.ftpintegration.inbound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@EnableAsync
@EnableScheduling
@Configuration
public class FtpDirectoryMonitor {

    @Value("${ftp-host}")
    private String host;

    @Value("${ftp-port}")
    private int port;

    @Value("${ftp-username}")
    private String username;

    @Value("${ftp-password}")
    private String password;

    private String inputDir;
    private String archiveDir;
    private String errorDir;

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        return taskScheduler;
    }

    public String getInputDir() {
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public String getArchiveDir() {
        return archiveDir;
    }

    public void setArchiveDir(String archiveDir) {
        this.archiveDir = archiveDir;
    }

    public String getErrorDir() {
        return errorDir;
    }

    public void setErrorDir(String errorDir) {
        this.errorDir = errorDir;
    }

    @Scheduled(fixedDelay = 5000)
    public void poll() {
        FtpTemplate template = new FtpTemplate(host, port, username, password, FtpTemplate.ServerType.UNIX, 10000);
        template.execute(client -> {
            FTPFile[] files = client.listFiles(inputDir);
            if (files == null) {
                return;
            }
            for (FTPFile file : files) {
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    if (client.retrieveFile(file.getName(), outputStream)) {
                        
                        String content = outputStream.toString(StandardCharsets.UTF_8.toString());
                        System.out.println(content);
                        
                        // processing...
                        // return or throw exception if something wrong.
                        
                        // success if runs here...
                        
                        /*
                         * 1. list all files in "in" folder
                         *    1. if filename is like *.lock.timestamp,
                         *       extract the timestamp from the file name
                         *       calculate the timestamp and current time
                         *       if the timestamp is older than <process timeout>,
                         *       rename it back to its original name.
                         *       otherwise, ignore.
                         *    2. for regular files, find the first file as the target file.
                         *       rename filename to *.lock.timestamp
                         *       process
                         *       if success
                         *       1. ...
                         * 
                         * */
                        // 1. list all files in "in" except *.lock...
                        // 2. for the target file, rename file to file.lock.timestamp
                        // 3. read content of the target file and process
                        // 4. if ...
                        
                        // client.storeFile(remote, local);
                        
                        // client.deleteFile(pathname);
                        
                    } else {
                        //log.warn("Fail to download from FTP server {} file name: {}", host, file.getName());
                    }
                }
            }
        });
    }

}
