package com.example.ftpintegration.ftp;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.net.ftp.FTPFile;
import org.junit.Before;
import org.junit.Test;

import com.example.ftpintegration.ftp.exception.FtpConnectionException;
import com.example.ftpintegration.ftp.exception.FtpLoginException;
import com.example.ftpintegration.ftp.exception.FtpModeSwitchException;
import com.example.ftpintegration.ftp.exception.FtpRetrieveFileException;
import com.example.ftpintegration.ftp.exception.FtpStoreFileException;
import com.example.ftpintegration.ftp.handler.FileHandler;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

public class FtpTemplateTest {

    private String host = "mock-host";
    private int port = 123;
    private String username = "mock-user";
    private String password = "mock-password";

    private FtpServer server;
    private FtpAgent agent;
    private FtpTemplate template;

    @Before
    public void before() {
        agent = mock(FtpAgent.class);
        server = mock(FtpServer.class);
        when(server.getFtpAgent()).thenReturn(agent);
        when(server.getHost()).thenReturn(host);
        when(server.getPort()).thenReturn(port);
        when(server.getUsername()).thenReturn(username);
        when(server.getPassword()).thenReturn(password);
        when(server.isPassiveMode()).thenReturn(false);
        template = new FtpTemplate(server);
    }

    @Test
    public void connectionTemplateNoOperation() throws Throwable {
        FtpTemplate.DoWithConnectionTemplate tmp = template.new DoWithConnectionTemplate(null);
        tmp.execute(null);

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);
    }

    @Test
    public void connectionTemplateConnectionException() throws Throwable {
        doThrow(FtpConnectionException.class).when(agent).connect(eq(host), eq(port));

        FtpTemplate.DoWithConnectionTemplate tmp = template.new DoWithConnectionTemplate(null);
        FtpOperationResult result = new FtpOperationResult();
        assertThrows(FtpConnectionException.class, () -> {
            tmp.execute(result);
        });
        assertFalse(result.isSuccess());
        assertEquals(FtpConnectionException.class, result.getError().getClass());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);
    }

    @Test
    public void connectionTemplateOperationError() throws Throwable {
        FtpOperationResult result = new FtpOperationResult();
        FtpTemplate.FtpOperation op = mock(FtpTemplate.FtpOperation.class);
        doThrow(Throwable.class).when(op).execute(result);
        FtpTemplate.DoWithConnectionTemplate tmp = template.new DoWithConnectionTemplate(op);

        assertThrows(Throwable.class, () -> {
            tmp.execute(result);
        });
        assertFalse(result.isSuccess());
        assertNull(result.getError());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);
    }

    @Test
    public void loginTemplateNoOperation() throws Throwable {
        FtpTemplate.DoWithLoginTemplate tmp = template.new DoWithLoginTemplate(null);
        tmp.execute(null);

        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).logout();
        verifyNoMoreInteractions(agent);
    }

    @Test
    public void loginTemplateLoginException() throws Throwable {
        doThrow(FtpLoginException.class).when(agent).login(eq(username), eq(password));

        FtpTemplate.DoWithLoginTemplate tmp = template.new DoWithLoginTemplate(null);
        FtpOperationResult result = new FtpOperationResult();
        assertThrows(FtpLoginException.class, () -> {
            tmp.execute(result);
        });
        assertFalse(result.isSuccess());
        assertEquals(FtpLoginException.class, result.getError().getClass());

        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).logout();
        verifyNoMoreInteractions(agent);
    }

    @Test
    public void loginTemplateOperationError() throws Throwable {
        FtpOperationResult result = new FtpOperationResult();
        FtpTemplate.FtpOperation op = mock(FtpTemplate.FtpOperation.class);
        doThrow(Throwable.class).when(op).execute(result);
        FtpTemplate.DoWithLoginTemplate tmp = template.new DoWithLoginTemplate(op);

        assertThrows(Throwable.class, () -> {
            tmp.execute(result);
        });
        assertFalse(result.isSuccess());
        assertNull(result.getError());

        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).logout();
        verifyNoMoreInteractions(agent);
    }

    @Test
    public void defaultTemplateConnectionException() throws Throwable {
        doThrow(FtpConnectionException.class).when(agent).connect(eq(host), eq(port));

        FtpOperationResult result = new FtpOperationResult();
        FtpTemplate.DefaultTemplate tmp = template.new DefaultTemplate();

        result = tmp.run(null);
        assertFalse(result.isSuccess());
        assertEquals(FtpConnectionException.class, result.getError().getClass());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);
    }

    @Test
    public void defaultTemplateModeSwitchException() throws Throwable {
        when(server.isPassiveMode()).thenReturn(true);
        doThrow(FtpModeSwitchException.class).when(agent).enterPassiveMode();

        FtpOperationResult result = new FtpOperationResult();
        FtpTemplate.DefaultTemplate tmp = template.new DefaultTemplate();

        result = tmp.run(null);
        assertFalse(result.isSuccess());
        assertEquals(FtpModeSwitchException.class, result.getError().getClass());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).enterPassiveMode();
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);
    }

    @Test
    public void defaultTemplateLoginException() throws Throwable {
        doThrow(FtpLoginException.class).when(agent).login(eq(username), eq(password));

        FtpOperationResult result = new FtpOperationResult();
        FtpTemplate.DefaultTemplate tmp = template.new DefaultTemplate();

        result = tmp.run(null);
        assertFalse(result.isSuccess());
        assertEquals(FtpLoginException.class, result.getError().getClass());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);
    }

    @Test
    public void defaultTemplateOperationSuccess() throws Throwable {
        FtpTemplate.FtpOperation op = result -> {
            result.setSuccess(true);
        };

        FtpTemplate.DefaultTemplate tmp = template.new DefaultTemplate();
        FtpOperationResult result = tmp.run(op);
        assertTrue(result.isSuccess());
        assertNull(result.getError());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);
    }

    @Test
    public void defaultTemplateOperationException1() throws Throwable {
        FtpTemplate.FtpOperation op = mock(FtpTemplate.FtpOperation.class);
        doThrow(Throwable.class).when(op).execute(any(FtpOperationResult.class));

        FtpTemplate.DefaultTemplate tmp = template.new DefaultTemplate();
        FtpOperationResult result = tmp.run(op);
        assertFalse(result.isSuccess());
        assertEquals(Throwable.class, result.getError().getClass());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);
    }

    @Test
    public void defaultTemplateOperationException2() throws Throwable {
        FtpTemplate.FtpOperation op = result -> {
            result.setSuccess(true);
            throw new IOException();
        };

        FtpTemplate.DefaultTemplate tmp = template.new DefaultTemplate();
        FtpOperationResult result = tmp.run(op);
        assertFalse(result.isSuccess());
        assertEquals(IOException.class, result.getError().getClass());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);
    }

    @Test
    public void retrieveFileSuccess() throws Throwable {
        String fileName = "fileName";
        String message = "messsage";
        byte[] bytes = new byte[] { 0x11, 0x12 };
        when(agent.retrieveFile(eq(fileName))).thenReturn(bytes);
        FileHandler handler = mock(FileHandler.class);
        when(handler.handleFile(any(byte[].class))).thenReturn(message);

        FtpOperationResult result = template.retrieveFile(fileName, handler);
        assertTrue(result.isSuccess());
        assertEquals(message, result.getMessage());
        assertNull(result.getError());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).retrieveFile(eq(fileName));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);

        verify(handler, times(1)).handleFile(any(byte[].class));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void retrieveFileError() throws Throwable {
        String fileName = "fileName";
        String message = "messsage";
        byte[] bytes = new byte[] { 0x11, 0x12 };
        when(agent.retrieveFile(eq(fileName))).thenReturn(bytes);
        FileHandler handler = mock(FileHandler.class);

        doThrow(Throwable.class).when(handler).handleFile(any(byte[].class));

        FtpOperationResult result = template.retrieveFile(fileName, handler);
        assertFalse(result.isSuccess());
        assertNotSame(message, result.getMessage());
        assertEquals(Throwable.class, result.getError().getClass());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).retrieveFile(eq(fileName));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);

        verify(handler, times(1)).handleFile(any(byte[].class));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void retrieveThenDeleteRetrieveError() throws Throwable {
        String fileName = "fileName";
        doThrow(FtpRetrieveFileException.class).when(agent).retrieveFile(eq(fileName));
        FileHandler handler = mock(FileHandler.class);

        FtpOperationResult result = template.retrieveThenDelete(fileName, handler);
        assertFalse(result.isSuccess());
        assertEquals(FtpRetrieveFileException.class, result.getError().getClass());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).retrieveFile(eq(fileName));
        // agent.deleteFile(fileName) should not be not called.
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void retrieveThenDeleteHandlingError() throws Throwable {
        String fileName = "fileName";
        String message = "messsage";
        byte[] bytes = new byte[] { 0x11, 0x12 };
        when(agent.retrieveFile(eq(fileName))).thenReturn(bytes);
        FileHandler handler = mock(FileHandler.class);

        doThrow(Throwable.class).when(handler).handleFile(any(byte[].class));

        FtpOperationResult result = template.retrieveThenDelete(fileName, handler);
        assertFalse(result.isSuccess());
        assertNotSame(message, result.getMessage());
        assertEquals(Throwable.class, result.getError().getClass());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).retrieveFile(eq(fileName));
        // agent.deleteFile(fileName) should not be not called.
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);

        verify(handler, times(1)).handleFile(any(byte[].class));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void retrieveThenDeleteHandlingSuccess() throws Throwable {
        String fileName = "fileName";
        String message = "messsage";
        byte[] bytes = new byte[] { 0x11, 0x12 };
        when(agent.retrieveFile(eq(fileName))).thenReturn(bytes);
        FileHandler handler = mock(FileHandler.class);
        when(handler.handleFile(any(byte[].class))).thenReturn(message);

        FtpOperationResult result = template.retrieveThenDelete(fileName, handler);
        assertTrue(result.isSuccess());
        assertEquals(message, result.getMessage());
        assertNull(result.getError());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).retrieveFile(eq(fileName));
        verify(agent, times(1)).deleteFile(eq(fileName));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);

        verify(handler, times(1)).handleFile(any(byte[].class));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void retrieveThenMoveRetrieveError() throws Throwable {
        String inputFileName = "inputFileName";
        String archiveFileName = "archiveFileName";
        doThrow(FtpRetrieveFileException.class).when(agent).retrieveFile(eq(inputFileName));
        FileHandler handler = mock(FileHandler.class);

        FtpOperationResult result = template.retrieveThenMove(inputFileName, archiveFileName, handler);
        assertFalse(result.isSuccess());
        assertEquals(FtpRetrieveFileException.class, result.getError().getClass());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).retrieveFile(eq(inputFileName));
        // agent.storeFile(...) should not be called.
        // agent.deleteFile(...) should not be not called.
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void retrieveThenMoveHandlingError() throws Throwable {
        String inputFileName = "inputFileName";
        String archiveFileName = "archiveFileName";
        byte[] bytes = new byte[] { 0x11, 0x12 };
        when(agent.retrieveFile(eq(inputFileName))).thenReturn(bytes);
        FileHandler handler = mock(FileHandler.class);

        doThrow(Throwable.class).when(handler).handleFile(any(byte[].class));

        FtpOperationResult result = template.retrieveThenMove(inputFileName, archiveFileName, handler);
        assertFalse(result.isSuccess());
        assertEquals(Throwable.class, result.getError().getClass());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).retrieveFile(eq(inputFileName));
        // agent.storeFile(...) should not be called.
        // agent.deleteFile(...) should not be not called.
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);

        verify(handler, times(1)).handleFile(any(byte[].class));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void retrieveThenMoveStoreError() throws Throwable {
        String inputFileName = "inputFileName";
        String archiveFileName = "archiveFileName";
        String message = "message";
        byte[] bytes = new byte[] { 0x11, 0x12 };
        when(agent.retrieveFile(eq(inputFileName))).thenReturn(bytes);
        FileHandler handler = mock(FileHandler.class);
        when(handler.handleFile(any(byte[].class))).thenReturn(message);

        doThrow(FtpStoreFileException.class).when(agent).storeFile(eq(archiveFileName), any(byte[].class));

        FtpOperationResult result = template.retrieveThenMove(inputFileName, archiveFileName, handler);
        assertFalse(result.isSuccess());
        assertNotSame(message, result.getMessage());
        assertEquals(FtpStoreFileException.class, result.getError().getClass());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).retrieveFile(eq(inputFileName));
        verify(agent, times(1)).storeFile(eq(archiveFileName), any(byte[].class));
        // agent.deleteFile(...) should not be not called.
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);

        verify(handler, times(1)).handleFile(any(byte[].class));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void retrieveThenMoveSuccess() throws Throwable {
        String inputFileName = "inputFileName";
        String archiveFileName = "archiveFileName";
        String message = "message";
        byte[] bytes = new byte[] { 0x11, 0x12 };
        when(agent.retrieveFile(eq(inputFileName))).thenReturn(bytes);
        FileHandler handler = mock(FileHandler.class);
        when(handler.handleFile(any(byte[].class))).thenReturn(message);

        FtpOperationResult result = template.retrieveThenMove(inputFileName, archiveFileName, handler);
        assertTrue(result.isSuccess());
        assertEquals(message, result.getMessage());
        assertNull(result.getError());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).retrieveFile(eq(inputFileName));
        verify(agent, times(1)).storeFile(eq(archiveFileName), any(byte[].class));
        verify(agent, times(1)).deleteFile(eq(inputFileName));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);

        verify(handler, times(1)).handleFile(any(byte[].class));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void retrieveThenDeleteAllException() throws Throwable {
        String inputDirectory = "inputDirectory";
        String fileName2 = "fileName2";
        String fileName3 = "fileName3";
        String message = "messsage";

        // 1. directory
        FTPFile file1 = mock(FTPFile.class);
        when(file1.isDirectory()).thenReturn(true);

        // 2. file (fail)
        FTPFile file2 = mock(FTPFile.class);
        when(file2.isDirectory()).thenReturn(false);
        when(file2.getName()).thenReturn(fileName2);
        doThrow(FtpRetrieveFileException.class).when(agent).retrieveFile(eq(inputDirectory + "/" + fileName2));

        // 3. file (success)
        FTPFile file3 = mock(FTPFile.class);
        when(file3.isDirectory()).thenReturn(false);
        when(file3.getName()).thenReturn(fileName3);
        byte[] bytes = new byte[] { 0x11, 0x12 };
        when(agent.retrieveFile(eq(inputDirectory + "/" + fileName3))).thenReturn(bytes);

        when(agent.listFiles(eq(inputDirectory))).thenReturn(new FTPFile[] { file1, file2, file3 });

        FileHandler handler = mock(FileHandler.class);
        when(handler.handleFile(any(byte[].class))).thenReturn(message);

        List<FtpOperationResult> results = template.retrieveThenDeleteAll(inputDirectory, handler);
        assertEquals(2, results.size());

        assertFalse(results.get(0).isSuccess());
        assertEquals(FtpRetrieveFileException.class, results.get(0).getError().getClass());

        assertTrue(results.get(1).isSuccess());
        assertNull(results.get(1).getError());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).listFiles(eq(inputDirectory));
        verify(agent, times(1)).retrieveFile(eq(inputDirectory + "/" + fileName2));
        verify(agent, times(1)).retrieveFile(eq(inputDirectory + "/" + fileName3));
        // agent.deleteFile(...) on failed file should not be called.
        verify(agent, times(1)).deleteFile(eq(inputDirectory + "/" + fileName3));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);

        verify(handler, times(1)).handleFile(any(byte[].class));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void retrieveThenDeleteAllSuccess() throws Throwable {
        String inputDirectory = "inputDirectory";
        String fileName2 = "fileName2";
        String fileName3 = "fileName3";
        String message = "messsage";

        byte[] bytes = new byte[] { 0x11, 0x12 };
        when(agent.retrieveFile(anyString())).thenReturn(bytes);
        FileHandler handler = mock(FileHandler.class);
        when(handler.handleFile(any(byte[].class))).thenReturn(message);

        // 1. directory
        FTPFile file1 = mock(FTPFile.class);
        when(file1.isDirectory()).thenReturn(true);

        // 2. file (success)
        FTPFile file2 = mock(FTPFile.class);
        when(file2.isDirectory()).thenReturn(false);
        when(file2.getName()).thenReturn(fileName2);

        // 3. file (success)
        FTPFile file3 = mock(FTPFile.class);
        when(file3.isDirectory()).thenReturn(false);
        when(file3.getName()).thenReturn(fileName3);

        when(agent.listFiles(eq(inputDirectory))).thenReturn(new FTPFile[] { file1, file2, file3 });

        List<FtpOperationResult> results = template.retrieveThenDeleteAll(inputDirectory, handler);
        assertEquals(2, results.size());

        assertTrue(results.get(0).isSuccess());
        assertNull(results.get(0).getError());

        assertTrue(results.get(1).isSuccess());
        assertNull(results.get(1).getError());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).listFiles(eq(inputDirectory));
        verify(agent, times(1)).retrieveFile(eq(inputDirectory + "/" + fileName2));
        verify(agent, times(1)).retrieveFile(eq(inputDirectory + "/" + fileName3));
        verify(agent, times(1)).deleteFile(eq(inputDirectory + "/" + fileName2));
        verify(agent, times(1)).deleteFile(eq(inputDirectory + "/" + fileName3));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);

        verify(handler, times(2)).handleFile(any(byte[].class));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void retrieveThenMoveAllException1() throws Throwable {
        String inputDirectory = "inputDirectory";
        String archiveDirectory = "archiveDirectory";
        String fileName2 = "fileName2";
        String fileName3 = "fileName3";
        String message = "messsage";

        FileHandler handler = mock(FileHandler.class);
        when(handler.handleFile(any(byte[].class))).thenReturn(message);

        // 1. directory
        FTPFile file1 = mock(FTPFile.class);
        when(file1.isDirectory()).thenReturn(true);

        // 2. file (success)
        FTPFile file2 = mock(FTPFile.class);
        when(file2.isDirectory()).thenReturn(false);
        when(file2.getName()).thenReturn(fileName2);
        doThrow(FtpRetrieveFileException.class).when(agent).retrieveFile(eq(inputDirectory + "/" + fileName2));

        // 3. file (success)
        FTPFile file3 = mock(FTPFile.class);
        when(file3.isDirectory()).thenReturn(false);
        when(file3.getName()).thenReturn(fileName3);
        byte[] bytes = new byte[] { 0x11, 0x12 };
        when(agent.retrieveFile(eq(inputDirectory + "/" + fileName3))).thenReturn(bytes);

        when(agent.listFiles(eq(inputDirectory))).thenReturn(new FTPFile[] { file1, file2, file3 });

        List<FtpOperationResult> results = template.retrieveThenMoveAll(inputDirectory, archiveDirectory, handler);
        assertEquals(2, results.size());

        assertFalse(results.get(0).isSuccess());
        assertEquals(FtpRetrieveFileException.class, results.get(0).getError().getClass());

        assertTrue(results.get(1).isSuccess());
        assertNull(results.get(1).getError());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).listFiles(eq(inputDirectory));
        verify(agent, times(1)).retrieveFile(eq(inputDirectory + "/" + fileName2));
        verify(agent, times(1)).retrieveFile(eq(inputDirectory + "/" + fileName3));
        // agent.storeFile(archiveDirectory + "/" + fileName2) should not be
        // called.
        verify(agent, times(1)).storeFile(eq(archiveDirectory + "/" + fileName3), any(byte[].class));
        // agent.deleteFile(inputDirectory + "/" + fileName2) should not be
        // called.
        verify(agent, times(1)).deleteFile(eq(inputDirectory + "/" + fileName3));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);

        verify(handler, times(1)).handleFile(any(byte[].class));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void retrieveThenMoveAllException2() throws Throwable {
        String inputDirectory = "inputDirectory";
        String archiveDirectory = "archiveDirectory";
        String fileName2 = "fileName2";
        String fileName3 = "fileName3";
        String message = "messsage";

        byte[] bytes = new byte[] { 0x11, 0x12 };
        when(agent.retrieveFile(anyString())).thenReturn(bytes);
        FileHandler handler = mock(FileHandler.class);
        when(handler.handleFile(any(byte[].class))).thenReturn(message);

        // 1. directory
        FTPFile file1 = mock(FTPFile.class);
        when(file1.isDirectory()).thenReturn(true);

        // 2. file (success)
        FTPFile file2 = mock(FTPFile.class);
        when(file2.isDirectory()).thenReturn(false);
        when(file2.getName()).thenReturn(fileName2);
        doThrow(FtpStoreFileException.class).when(agent).storeFile(eq(archiveDirectory + "/" + fileName2),
                any(byte[].class));

        // 3. file (success)
        FTPFile file3 = mock(FTPFile.class);
        when(file3.isDirectory()).thenReturn(false);
        when(file3.getName()).thenReturn(fileName3);

        when(agent.listFiles(eq(inputDirectory))).thenReturn(new FTPFile[] { file1, file2, file3 });

        List<FtpOperationResult> results = template.retrieveThenMoveAll(inputDirectory, archiveDirectory, handler);
        assertEquals(2, results.size());

        assertFalse(results.get(0).isSuccess());
        assertEquals(FtpStoreFileException.class, results.get(0).getError().getClass());

        assertTrue(results.get(1).isSuccess());
        assertNull(results.get(1).getError());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).listFiles(eq(inputDirectory));
        verify(agent, times(1)).retrieveFile(eq(inputDirectory + "/" + fileName2));
        verify(agent, times(1)).retrieveFile(eq(inputDirectory + "/" + fileName3));
        verify(agent, times(1)).storeFile(eq(archiveDirectory + "/" + fileName2), any(byte[].class));
        verify(agent, times(1)).storeFile(eq(archiveDirectory + "/" + fileName3), any(byte[].class));
        // agent.deleteFile(inputDirectory + "/" + fileName2) should not run
        verify(agent, times(1)).deleteFile(eq(inputDirectory + "/" + fileName3));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);

        verify(handler, times(2)).handleFile(any(byte[].class));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void retrieveThenMoveAllSuccess() throws Throwable {
        String inputDirectory = "inputDirectory";
        String archiveDirectory = "archiveDirectory";
        String fileName2 = "fileName2";
        String fileName3 = "fileName3";
        String message = "messsage";

        byte[] bytes = new byte[] { 0x11, 0x12 };
        when(agent.retrieveFile(anyString())).thenReturn(bytes);
        FileHandler handler = mock(FileHandler.class);
        when(handler.handleFile(any(byte[].class))).thenReturn(message);

        // 1. directory
        FTPFile file1 = mock(FTPFile.class);
        when(file1.isDirectory()).thenReturn(true);

        // 2. file (success)
        FTPFile file2 = mock(FTPFile.class);
        when(file2.isDirectory()).thenReturn(false);
        when(file2.getName()).thenReturn(fileName2);

        // 3. file (success)
        FTPFile file3 = mock(FTPFile.class);
        when(file3.isDirectory()).thenReturn(false);
        when(file3.getName()).thenReturn(fileName3);

        when(agent.listFiles(eq(inputDirectory))).thenReturn(new FTPFile[] { file1, file2, file3 });

        List<FtpOperationResult> results = template.retrieveThenMoveAll(inputDirectory, archiveDirectory, handler);
        assertEquals(2, results.size());

        assertTrue(results.get(0).isSuccess());
        assertNull(results.get(0).getError());

        assertTrue(results.get(1).isSuccess());
        assertNull(results.get(1).getError());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).listFiles(eq(inputDirectory));
        verify(agent, times(1)).retrieveFile(eq(inputDirectory + "/" + fileName2));
        verify(agent, times(1)).retrieveFile(eq(inputDirectory + "/" + fileName3));
        verify(agent, times(1)).storeFile(eq(archiveDirectory + "/" + fileName2), any(byte[].class));
        verify(agent, times(1)).storeFile(eq(archiveDirectory + "/" + fileName3), any(byte[].class));
        verify(agent, times(1)).deleteFile(eq(inputDirectory + "/" + fileName2));
        verify(agent, times(1)).deleteFile(eq(inputDirectory + "/" + fileName3));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);

        verify(handler, times(2)).handleFile(any(byte[].class));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void storeFileException() throws Throwable {
        String fileName = "fileName";

        doThrow(FtpStoreFileException.class).when(agent).storeFile(eq(fileName), any(byte[].class));

        byte[] bytes = new byte[] { 0x11, 0x12 };
        FtpOperationResult result = template.storeFile(fileName, bytes);
        assertFalse(result.isSuccess());
        assertEquals(FtpStoreFileException.class, result.getError().getClass());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).storeFile(eq(fileName), any(byte[].class));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);
    }

    @Test
    public void storeFileSuccess() throws Throwable {
        String fileName = "fileName";
        byte[] bytes = new byte[] { 0x11, 0x12 };

        FtpOperationResult result = template.storeFile(fileName, bytes);
        assertTrue(result.isSuccess());
        assertNull(result.getError());

        verify(agent, times(1)).connect(eq(host), eq(port));
        verify(agent, times(1)).login(eq(username), eq(password));
        verify(agent, times(1)).storeFile(eq(fileName), any(byte[].class));
        verify(agent, times(1)).logout();
        verify(agent, times(1)).disconnect();
        verifyNoMoreInteractions(agent);
    }
}
