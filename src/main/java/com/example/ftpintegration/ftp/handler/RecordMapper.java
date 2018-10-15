package com.example.ftpintegration.ftp.handler;

/**
 * Map source object to target object and vice versa.
 * 
 * @author Yu-Hua Chang
 *
 * @param <S>
 *            source object type
 * @param <T>
 *            target object type
 */
public interface RecordMapper<S, T> {

    T sourceToTarget(S source);

    Object[] targetToSource(T target);
}
