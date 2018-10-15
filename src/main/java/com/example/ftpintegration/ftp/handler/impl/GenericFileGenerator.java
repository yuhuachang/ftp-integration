package com.example.ftpintegration.ftp.handler.impl;

import com.example.ftpintegration.ftp.handler.FileGenerator;
import com.example.ftpintegration.ftp.handler.RecordMapper;

public abstract class GenericFileGenerator<S, T> implements FileGenerator<T> {

    protected final RecordMapper<S, T> mapper;

    public GenericFileGenerator(RecordMapper<S, T> mapper) {
        this.mapper = mapper;
    }
}
