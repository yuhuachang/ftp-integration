package com.example.ftpintegration.ftp;

import static org.junit.Assert.*;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;

import com.example.ftpintegration.ftp.flow.FtpFlowSynchronizer;
import com.example.ftpintegration.ftp.flow.FtpOperationFlow;

public class FtpTemplateTest {

    private FTPClient client;
    private FtpTemplate template;
    private FtpOperationFlow operation;
    private FtpFlowSynchronizer synchronizer;

    private String host = "mock-host";
    private int port = 123;
    private String username = "mock-user";
    private String password = "mock-password";
    private String serverType = "mock-serverType";
    private int timeout = 321;

    @Before
    public void before() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException {

        // create mocked ftp client
        client = mock(FTPClient.class);

        // create test ftp template object
        Constructor<FtpTemplate> constructor = FtpTemplate.class.getDeclaredConstructor(FTPClient.class, String.class,
                Integer.TYPE, String.class, String.class, String.class, Integer.TYPE);
        constructor.setAccessible(true);
        template = constructor.newInstance(client, host, port, username, password, serverType, timeout);

        // create test operation object
        operation = mock(FtpOperationFlow.class);
        synchronizer = mock(FtpFlowSynchronizer.class);
        when(operation.getFtpFlowSynchronizer()).thenReturn(synchronizer);
    }

    /**
     * check host, port, username, password are all set.
     */
    @Test
    public void basicSetup() {
        assertEquals("FTP mock-user/mock-password@mock-host:123", template.toString());
    }

    /**
     * check default configuration on ftp client are set.
     */
    @Test
    public void ftpClientSetup() {
        verify(client, times(1)).configure(any(FTPClientConfig.class));
        verify(client, times(1)).setConnectTimeout(timeout);
        verify(client, times(1)).setControlKeepAliveReplyTimeout(timeout);
        verify(client, times(1)).setControlKeepAliveTimeout(timeout);
        verify(client, times(1)).setDataTimeout(timeout);
        verify(client, times(1)).setDefaultTimeout(timeout);
    }

    /**
     * simulate connection failure
     */
    private void connectionFailureHelper(Class<? extends Throwable> e, boolean isConnected)
            throws SocketException, IOException {

        // throw SocketException or IOException when connect
        doThrow(e).when(client).connect(anyString(), anyInt());
        when(client.isConnected()).thenReturn(isConnected);

        // execute
        template.execute(operation);

        verify(client, times(1)).connect(anyString(), anyInt());
        if (isConnected) {
            verify(client, times(1)).disconnect();
        }

        // should notify synchronizer about this error
        verify(synchronizer, times(1)).onFtpError(anyString(), any());
    }

    @Test
    public void connectionFailure1() throws SocketException, IOException {
        connectionFailureHelper(SocketException.class, false);
    }

    @Test
    public void connectionFailure2() throws SocketException, IOException {
        connectionFailureHelper(IOException.class, false);
    }

    @Test
    public void connectionFailure3() throws SocketException, IOException {
        connectionFailureHelper(SocketException.class, true);
    }

    @Test
    public void connectionFailure4() throws SocketException, IOException {
        connectionFailureHelper(IOException.class, true);
    }

    /**
     * Reply code is negative. Connection is not established.
     */
    @Test
    public void unsuccessfulConnect() {

        // return negative reply code after connect
        when(client.getReplyCode()).thenReturn(-1);

        // execute
        template.execute(operation);

        verify(client, times(1)).getReplyCode();

        // should notify synchronizer about this error
        verify(synchronizer, times(1)).onFtpError(anyString(), any());
    }

    /**
     * FTPConnectionClosedException, IOException
     * 
     * @throws IOException
     */
    private void loginFailHelper(Class<? extends Throwable> e, boolean isSuccess) throws IOException {

        // return a success reply code
        when(client.getReplyCode()).thenReturn(200);

        if (e == null) {
            // no exception.
            when(client.login(anyString(), anyString())).thenReturn(isSuccess);

            template.execute(operation);

            verify(client, times(1)).login(anyString(), anyString());

            if (isSuccess) {
                // operation is executed. (successfulness does not matter)
                verify(operation, times(1)).execute(any(FTPClient.class));

                // success. should logout. (successfulness does not matter)
                verify(client, times(1)).logout();
            } else {
                // fail. should notify synchronizer about this error.
                verify(synchronizer, times(1)).onFtpError(anyString(), any());
            }
        } else {
            // has exception.
            doThrow(e).when(client).login(anyString(), anyString());

            template.execute(operation);

            verify(client, times(1)).login(anyString(), anyString());

            // should notify synchronizer about this error
            verify(synchronizer, times(1)).onFtpError(anyString(), any());
        }
    }

    @Test
    public void loginFail1() throws IOException {
        loginFailHelper(null, true);
    }

    @Test
    public void loginFail2() throws IOException {
        loginFailHelper(null, false);
    }

    @Test
    public void loginFail3() throws IOException {
        loginFailHelper(FTPConnectionClosedException.class, true);
    }

    @Test
    public void loginFail4() throws IOException {
        loginFailHelper(IOException.class, true);
    }

    /**
     * No matter what happened, ftp client should logout and close, and report on
     * errors.
     * 
     * @param hasError
     * @throws IOException
     */
    private void operationFailureHelper(boolean hasError) throws IOException {
        
        // connect success
        when(client.getReplyCode()).thenReturn(200);
        
        // login success 
        when(client.login(anyString(), anyString())).thenReturn(true);
 
        if (hasError) {
            // got exception during processing...
            doThrow(RuntimeException.class).when(operation).execute(any(FTPClient.class));
        }

        template.execute(operation);

        // has login
        verify(client, times(1)).login(anyString(), anyString());
        
        // operation is executed
        verify(operation, times(1)).execute(any(FTPClient.class));
        
        if (hasError) {
            // if error occurs, trigger synchronizer once.
            verify(synchronizer, times(1)).onFtpError(anyString(), any());
        } else {
            // if no error, do not trigger synchronizer any error.
            verify(synchronizer, times(0)).onFtpError(anyString(), any());
        }
        
        // should logout anyway.
        verify(client, times(1)).logout();
    }

    @Test
    public void operationFailure1() throws IOException {
        operationFailureHelper(true);
    }

    @Test
    public void operationFailure2() throws IOException {
        operationFailureHelper(false);
    }
}
