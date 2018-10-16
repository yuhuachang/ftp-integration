package com.example.ftpintegration.ftp;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.example.ftpintegration.ftp.exception.FtpConnectionException;
import com.example.ftpintegration.ftp.exception.FtpDeleteFileException;
import com.example.ftpintegration.ftp.exception.FtpListFilesException;
import com.example.ftpintegration.ftp.exception.FtpLoginException;
import com.example.ftpintegration.ftp.exception.FtpModeSwitchException;
import com.example.ftpintegration.ftp.exception.FtpRetrieveFileException;
import com.example.ftpintegration.ftp.exception.FtpStoreFileException;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.stream.Stream;

public class FtpAgentTest {

    private FTPClient client;

    @Before
    @BeforeEach
    public void before() {
        client = mock(FTPClient.class);
    }

    @Test(expected = NullPointerException.class)
    public void setup() {
        new FtpAgent(null);
    }

    public static Stream<Arguments> connectionExceptions() {
        return Stream.of(Arguments.of(SocketException.class), Arguments.of(IOException.class));
    }

    @ParameterizedTest
    @MethodSource("connectionExceptions")
    public void connectionError(Class<? extends Throwable> error) throws SocketException, IOException {
        doThrow(error).when(client).connect(anyString(), anyInt());

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpConnectionException.class, () -> {
            agent.connect(anyString(), anyInt());
        });

        verify(client, times(1)).connect(anyString(), anyInt());
        verifyNoMoreInteractions(client);
    }

    @Test
    public void unsuccessfulConnect() throws SocketException, IOException {
        when(client.getReplyCode()).thenReturn(0);

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpConnectionException.class, () -> {
            agent.connect(anyString(), anyInt());
        });

        verify(client, times(1)).connect(anyString(), anyInt());
        verify(client, times(2)).getReplyCode();
        verify(client, times(1)).getReplyString();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void disconnectionFailure() throws IOException {
        when(client.isConnected()).thenReturn(true);
        doThrow(IOException.class).when(client).disconnect();

        FtpAgent agent = new FtpAgent(client);
        agent.disconnect();

        verify(client, times(1)).isConnected();
        verify(client, times(1)).disconnect();
        verify(client, times(1)).getReplyCode();
        verify(client, times(1)).getReplyString();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void enterPassiveModeError() throws IOException {
        doThrow(IOException.class).when(client).enterRemotePassiveMode();

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpModeSwitchException.class, () -> {
            agent.enterPassiveMode();
        });

        verify(client, times(1)).enterRemotePassiveMode();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void enterPassiveModeFailure() throws IOException {
        when(client.enterRemotePassiveMode()).thenReturn(false);

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpModeSwitchException.class, () -> {
            agent.enterPassiveMode();
        });

        verify(client, times(1)).enterRemotePassiveMode();
        verify(client, times(1)).getReplyCode();
        verify(client, times(1)).getReplyString();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void enterPassiveModeSuccess() throws IOException, FtpModeSwitchException {
        when(client.enterRemotePassiveMode()).thenReturn(true);

        FtpAgent agent = new FtpAgent(client);
        agent.enterPassiveMode();

        verify(client, times(1)).enterRemotePassiveMode();
        verify(client, times(1)).enterLocalPassiveMode();
        verify(client, times(1)).getLocalAddress();
        verify(client, times(1)).getLocalPort();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void loginError() throws IOException {
        doThrow(IOException.class).when(client).login(anyString(), anyString());

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpLoginException.class, () -> {
            agent.login(anyString(), anyString());
        });

        verify(client, times(1)).login(anyString(), anyString());
        verifyNoMoreInteractions(client);
    }

    @Test
    public void loginFailure() throws IOException {
        when(client.login(anyString(), anyString())).thenReturn(false);

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpLoginException.class, () -> {
            agent.login(anyString(), anyString());
        });

        verify(client, times(1)).login(anyString(), anyString());
        verify(client, times(1)).getReplyCode();
        verify(client, times(1)).getReplyString();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void logoutError() throws IOException {
        doThrow(IOException.class).when(client).logout();

        FtpAgent agent = new FtpAgent(client);
        agent.logout();

        verify(client, times(1)).logout();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void logoutFailure() throws IOException {
        when(client.logout()).thenReturn(false);

        FtpAgent agent = new FtpAgent(client);
        agent.logout();

        verify(client, times(1)).logout();
        verify(client, times(1)).getReplyCode();
        verify(client, times(1)).getReplyString();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void listFilesError() throws IOException {
        String pathname = "pathname";
        doThrow(IOException.class).when(client).listFiles(eq(pathname));

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpListFilesException.class, () -> {
            agent.listFiles(pathname);
        });

        verify(client, times(1)).listFiles(eq(pathname));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void listFilesReturnNull() throws IOException {
        String pathname = "pathname";
        when(client.listFiles(eq(pathname))).thenReturn(null);

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpListFilesException.class, () -> {
            agent.listFiles(pathname);
        });

        verify(client, times(1)).listFiles(eq(pathname));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void listFilesUnsuccess() throws IOException {
        String pathname = "pathname";
        when(client.listFiles(eq(pathname))).thenReturn(new FTPFile[] {});
        when(client.getReplyCode()).thenReturn(0);

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpListFilesException.class, () -> {
            agent.listFiles(pathname);
        });

        verify(client, times(1)).listFiles(eq(pathname));
        verify(client, times(2)).getReplyCode();
        verify(client, times(1)).getReplyString();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void listFilesSuccess1() throws IOException, FtpListFilesException {
        String pathname = "pathname";
        when(client.listFiles(eq(pathname))).thenReturn(new FTPFile[] {});
        when(client.getReplyCode()).thenReturn(200);

        FtpAgent agent = new FtpAgent(client);
        FTPFile[] files = agent.listFiles(pathname);
        assertEquals("returned files not match.", 0, files.length);

        verify(client, times(1)).listFiles(eq(pathname));
        verify(client, times(1)).getReplyCode();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void listFilesSuccess2() throws IOException, FtpListFilesException {
        String pathname = "pathname";
        when(client.listFiles(eq(pathname))).thenReturn(new FTPFile[] { mock(FTPFile.class), mock(FTPFile.class) });
        when(client.getReplyCode()).thenReturn(200);

        FtpAgent agent = new FtpAgent(client);
        FTPFile[] files = agent.listFiles(pathname);
        assertEquals("returned files not match.", 2, files.length);

        verify(client, times(1)).listFiles(eq(pathname));
        verify(client, times(1)).getReplyCode();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void retrieveFileError() throws IOException {
        String fileName = "fileName";
        doThrow(IOException.class).when(client).retrieveFile(eq(fileName), any(OutputStream.class));

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpRetrieveFileException.class, () -> {
            agent.retrieveFile(fileName);
        });

        verify(client, times(1)).retrieveFile(eq(fileName), any(OutputStream.class));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void retrieveFileFail() throws IOException {
        String fileName = "fileName";
        when(client.retrieveFile(eq(fileName), any(OutputStream.class))).thenReturn(false);

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpRetrieveFileException.class, () -> {
            agent.retrieveFile(fileName);
        });

        verify(client, times(1)).retrieveFile(eq(fileName), any(OutputStream.class));
        verify(client, times(1)).getReplyCode();
        verify(client, times(1)).getReplyString();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void retrieveFileSuccess() throws IOException, FtpRetrieveFileException {
        String fileName = "fileName";
        when(client.retrieveFile(eq(fileName), any(OutputStream.class))).thenReturn(true);

        FtpAgent agent = new FtpAgent(client);
        agent.retrieveFile(fileName);

        verify(client, times(1)).retrieveFile(eq(fileName), any(OutputStream.class));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void storeFileSuccess() throws IOException, FtpStoreFileException {
        String fileName = "fileName";
        when(client.storeFile(eq(fileName), any(InputStream.class))).thenReturn(true);

        FtpAgent agent = new FtpAgent(client);
        agent.storeFile(fileName, new byte[] {});

        verify(client, times(1)).storeFile(eq(fileName), any(InputStream.class));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void storeFileError() throws IOException {
        String fileName = "fileName";
        doThrow(IOException.class).when(client).storeFile(eq(fileName), any(InputStream.class));

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpStoreFileException.class, () -> {
            agent.storeFile(fileName, new byte[] {});
        });

        verify(client, times(1)).storeFile(eq(fileName), any(InputStream.class));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void storeFileFail() throws IOException {
        String fileName = "fileName";
        when(client.storeFile(eq(fileName), any(InputStream.class))).thenReturn(false);

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpStoreFileException.class, () -> {
            agent.storeFile(fileName, new byte[] {});
        });

        verify(client, times(1)).storeFile(eq(fileName), any(InputStream.class));
        verify(client, times(1)).getReplyCode();
        verify(client, times(1)).getReplyString();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void deleteFileError() throws IOException {
        String fileName = "fileName";
        doThrow(IOException.class).when(client).deleteFile(eq(fileName));

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpDeleteFileException.class, () -> {
            agent.deleteFile(fileName);
        });

        verify(client, times(1)).deleteFile(eq(fileName));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void deleteFileSuccess() throws IOException, FtpDeleteFileException {
        String fileName = "fileName";
        when(client.deleteFile(eq(fileName))).thenReturn(true);

        FtpAgent agent = new FtpAgent(client);
        agent.deleteFile(fileName);

        verify(client, times(1)).deleteFile(eq(fileName));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void deleteFileFail() throws IOException {
        String fileName = "fileName";
        when(client.deleteFile(eq(fileName))).thenReturn(false);

        FtpAgent agent = new FtpAgent(client);
        assertThrows(FtpDeleteFileException.class, () -> {
            agent.deleteFile(fileName);
        });

        verify(client, times(1)).deleteFile(eq(fileName));
        verify(client, times(1)).getReplyCode();
        verify(client, times(1)).getReplyString();
        verifyNoMoreInteractions(client);
    }
}
