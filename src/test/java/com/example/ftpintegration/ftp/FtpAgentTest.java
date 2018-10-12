package com.example.ftpintegration.ftp;

import static org.junit.Assert.*;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FtpAgentTest {

    private FTPClient client;

    @Before
    public void before() {
        client = mock(FTPClient.class);
    }

    @Test(expected = NullPointerException.class)
    public void setup() {
        new FtpAgent(null);
    }

    @Test
    public void listFilesReturnNull() throws IOException {
        String pathname = "pathname";
        when(client.listFiles(pathname)).thenReturn(null);

        FtpAgent op = new FtpAgent(client);
        try {
            op.listFiles(pathname);
            fail("expect to see exception if list command return null.");
        } catch (FtpException e) {
            // good.
        }

        verify(client, times(1)).listFiles(pathname);
        verifyNoMoreInteractions(client);
    }

    @Test
    public void listFilesUnsuccess() throws IOException {
        String pathname = "pathname";
        when(client.listFiles(pathname)).thenReturn(new FTPFile[] {});
        when(client.getReplyCode()).thenReturn(0);

        FtpAgent op = new FtpAgent(client);
        try {
            op.listFiles(pathname);
            fail("expect to see exception if list command return null.");
        } catch (FtpException e) {
            // good.
        }

        verify(client, times(1)).listFiles(pathname);
        verify(client, times(1)).getReplyCode();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void listFilesSuccess1() throws IOException, FtpException {
        String pathname = "pathname";
        when(client.listFiles(pathname)).thenReturn(new FTPFile[] {});
        when(client.getReplyCode()).thenReturn(200);

        FtpAgent op = new FtpAgent(client);
        FTPFile[] files = op.listFiles(pathname);
        assertEquals("returned files not match.", 0, files.length);

        verify(client, times(1)).listFiles(pathname);
        verify(client, times(1)).getReplyCode();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void listFilesSuccess2() throws IOException, FtpException {
        String pathname = "pathname";
        when(client.listFiles(pathname)).thenReturn(new FTPFile[] { mock(FTPFile.class), mock(FTPFile.class) });
        when(client.getReplyCode()).thenReturn(200);

        FtpAgent op = new FtpAgent(client);
        FTPFile[] files = op.listFiles(pathname);
        assertEquals("returned files not match.", 2, files.length);

        verify(client, times(1)).listFiles(pathname);
        verify(client, times(1)).getReplyCode();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void retrieveFileFail() throws IOException {
        String fileName = "file";
        when(client.retrieveFile(eq(fileName), any(OutputStream.class))).thenReturn(false);

        FtpAgent op = new FtpAgent(client);
        try {
            op.retrieveFile(fileName);
            fail("should die because byte array is null");
        } catch (FtpException e) {
            // good.
        }

        verify(client, times(1)).retrieveFile(eq(fileName), any(OutputStream.class));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void retrieveFileSuccess() throws IOException, FtpException {
        String fileName = "file";
        when(client.retrieveFile(eq(fileName), any(OutputStream.class))).thenReturn(true);

        FtpAgent op = new FtpAgent(client);
        op.retrieveFile(fileName);

        verify(client, times(1)).retrieveFile(eq(fileName), any(OutputStream.class));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void storeFileSuccess() throws IOException, FtpException {
        String fileName = "file";
        when(client.storeFile(eq(fileName), any(InputStream.class))).thenReturn(true);

        FtpAgent op = new FtpAgent(client);
        op.storeFile(fileName, new byte[] {});

        verify(client, times(1)).storeFile(eq(fileName), any(InputStream.class));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void storeFileFail() throws IOException {
        String fileName = "file";
        when(client.storeFile(eq(fileName), any(InputStream.class))).thenReturn(false);

        FtpAgent op = new FtpAgent(client);
        try {
            op.storeFile(fileName, new byte[] {});
            fail("should die because command return false.");
        } catch (FtpException e) {
            // good.
        }

        verify(client, times(1)).storeFile(eq(fileName), any(InputStream.class));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void deleteFileSuccess() throws IOException, FtpException {
        String fileName = "file";
        when(client.deleteFile(eq(fileName))).thenReturn(true);

        FtpAgent op = new FtpAgent(client);
        op.deleteFile(fileName);

        verify(client, times(1)).deleteFile(eq(fileName));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void deleteFileFail() throws IOException {
        String fileName = "file";
        when(client.deleteFile(eq(fileName))).thenReturn(false);

        FtpAgent op = new FtpAgent(client);
        try {
            op.deleteFile(fileName);
            fail("should die because command return false.");
        } catch (FtpException e) {
            // good.
        }

        verify(client, times(1)).deleteFile(eq(fileName));
        verifyNoMoreInteractions(client);
    }
}
