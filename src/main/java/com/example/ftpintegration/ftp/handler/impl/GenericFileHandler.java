package com.example.ftpintegration.ftp.handler.impl;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.ftpintegration.ftp.handler.FileHandler;
import com.example.ftpintegration.ftp.handler.RecordHandler;
import com.example.ftpintegration.ftp.handler.RecordMapper;

public abstract class GenericFileHandler<S, T> implements FileHandler {

    private static final Logger log = LoggerFactory.getLogger(GenericFileHandler.class);

    private final RecordMapper<S, T> mapper;
    private final RecordHandler<T> handler;

    public GenericFileHandler(RecordMapper<S, T> mapper, RecordHandler<T> handler) {
        this.mapper = mapper;
        this.handler = handler;
    }

    public abstract List<S> getSourceObjectList(byte[] bytes) throws Throwable;

    @Override
    public String handleFile(byte[] bytes) throws Throwable {
        List<S> sourceList = getSourceObjectList(bytes);
        List<T> targetList = new LinkedList<>();
        for (S sourceObject : sourceList) {
            T targetObject = mapper.sourceToTarget(sourceObject);
            targetList.add(targetObject);
        }

        // handle records
        log.info("Done record mapping. Pass records to record handler.");
        handler.handleRecord(targetList);

        String msg = "All record handling are completed successfully.";
        log.info(msg);
        return msg;
    }
}
