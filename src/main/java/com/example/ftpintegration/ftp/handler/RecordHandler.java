package com.example.ftpintegration.ftp.handler;

import java.util.List;

/**
 * Take care of the list of input objects and complete processing them. Its up
 * to the implemented class to decide how to "handle" these objects. In case of
 * any error, throw an exception to let the caller to know. Otherwise, we assume
 * everything is done successfully.
 * 
 * @author Yu-Hua Chang
 *
 * @param <T>
 */
public interface RecordHandler<T> {

    void handleRecord(List<T> list);
}
