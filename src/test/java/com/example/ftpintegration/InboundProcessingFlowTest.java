package com.example.ftpintegration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Before;
import org.junit.Test;
import com.example.ftpintegration.ftp.flow.FtpFlowSynchronizer;
import com.example.ftpintegration.ftp.flow.InboundProcessingFlow;
import com.example.ftpintegration.processor.FileProcessor;

public class InboundProcessingFlowTest {

    private FTPClient client;
    private FileProcessor processor;
    private FtpFlowSynchronizer synchronizer;

    @Before
    public void before() {
        client = mock(FTPClient.class);
        processor = mock(FileProcessor.class);
        synchronizer = mock(FtpFlowSynchronizer.class);
    }

    @Test
    public void setupTest1() {
        InboundProcessingFlow flow = new InboundProcessingFlow(processor);
        assertEquals("default input folder is root.", "/", flow.getInputPath());
        assertNull("archive folder is optional", flow.getArchivePath());
        assertFalse("DryRun is default to false", flow.isDryRun());
    }

    @Test
    public void setupTest2() {
        InboundProcessingFlow flow = new InboundProcessingFlow("/input", processor);
        assertEquals("input folder is wrong.", "/input", flow.getInputPath());
        assertNull("archive folder is optional", flow.getArchivePath());
        assertFalse("DryRun is default to false", flow.isDryRun());
    }

    @Test
    public void setupTest3() {
        InboundProcessingFlow flow = new InboundProcessingFlow("/input", "/archive", processor);
        assertEquals("input folder is wrong.", "/input", flow.getInputPath());
        assertEquals("archive folder is wrong.", "/archive", flow.getArchivePath());
        assertFalse("DryRun is default to false", flow.isDryRun());
    }

    /**
     * operation is mandatory
     */
    @Test(expected = NullPointerException.class)
    public void setupTest4() {
        new InboundProcessingFlow(null);
    }

    /**
     * input and archive folder cannot be the same
     */
    @Test(expected = IllegalArgumentException.class)
    public void setupTest5() {
        new InboundProcessingFlow("/abc", "/abc", processor);
    }

    /**
     * error occur while listing input folder.
     * 
     * @throws IOException
     */
    @Test
    public void listFileFail1() throws IOException {

        // throw exception while listing input folder
        doThrow(IOException.class).when(client).listFiles(anyString());

        InboundProcessingFlow flow = new InboundProcessingFlow("/input", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        flow.execute(client);

        verify(client, times(1)).listFiles("/input");

        // must notify when error occurs
        verify(synchronizer, times(1)).onFtpError(anyString(), any());
    }

    /**
     * error occur while listing input folder.
     * 
     * @throws IOException
     */
    @Test
    public void listFileFail2() throws IOException {

        // listing command return error code. ftp reply code 2xx is success,
        // other is fail.
        when(client.getReplyCode()).thenReturn(0);

        InboundProcessingFlow flow = new InboundProcessingFlow("/input", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        flow.execute(client);

        verify(client, times(1)).listFiles("/input");
        verify(client, times(1)).getReplyCode();

        // this is not a big deal. don't notify.
        verify(synchronizer, times(1)).onFtpError(anyString(), any());
        verifyNoMoreInteractions(client);
    }

    /**
     * empty input folder.
     * 
     * @throws IOException
     */
    @Test
    public void emptyInputFolder1() throws IOException {

        // return null when listing folder. this is strange.
        when(client.listFiles(anyString())).thenReturn(null);

        // listing command return success code.
        when(client.getReplyCode()).thenReturn(200);

        InboundProcessingFlow flow = new InboundProcessingFlow("/input", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        flow.execute(client);

        verify(client, times(1)).listFiles("/input");
        verify(client, times(1)).getReplyCode();
        verify(synchronizer, times(1)).onListing(0);
        verifyNoMoreInteractions(client);
    }

    /**
     * empty input folder.
     * 
     * @throws IOException
     */
    @Test
    public void emptyInputFolder2() throws IOException {

        // nothing from listing. input folder is empty. do nothing.
        when(client.listFiles(anyString())).thenReturn(new FTPFile[] {});

        // listing command return success code.
        when(client.getReplyCode()).thenReturn(200);

        InboundProcessingFlow flow = new InboundProcessingFlow("/input", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        flow.execute(client);

        verify(client, times(1)).listFiles("/input");
        verify(client, times(1)).getReplyCode();
        verify(synchronizer, times(1)).onListing(0);
        verifyNoMoreInteractions(client);
    }

    /**
     * directory will not be processed in input folder.
     * 
     * @throws IOException
     */
    @Test
    public void processDirectory() throws IOException {
        FTPFile file = mock(FTPFile.class);

        // see one file in the input folder
        when(client.listFiles(anyString())).thenReturn(new FTPFile[] { file });

        // listing command success
        when(client.getReplyCode()).thenReturn(200);

        // this file is actually a directory, which will not be processed.
        when(file.isDirectory()).thenReturn(true);

        InboundProcessingFlow flow = new InboundProcessingFlow("/input", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        flow.execute(client);

        verify(client, times(1)).listFiles("/input");
        verify(client, times(1)).getReplyCode();
        verify(synchronizer, times(1)).onListing(1);
        verifyNoMoreInteractions(client);
    }

    /**
     * Single file correct processing flow.
     * @throws Exception 
     */
    @Test
    public void processFile() throws Exception {

        // a real flow object
        InboundProcessingFlow flow = new InboundProcessingFlow("/input", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        // a mocked flow object that cover the real flow object
//        InboundProcessingFlow mockedFlow = PowerMockito.spy(flow);
//        System.err.println(mockedFlow.getInputPath());

        // the mocked file to use
        FTPFile file = mock(FTPFile.class);
        when(file.getName()).thenReturn("mockFile");

        // list files and return one file.
        when(client.listFiles(anyString())).thenReturn(new FTPFile[] { file });
        when(client.getReplyCode()).thenReturn(200);
        when(file.isDirectory()).thenReturn(false);

        // retrieve file content success
        when(client.retrieveFile(anyString(), any(OutputStream.class))).thenReturn(true);
        
        // test file content
        byte[] content = "test".getBytes(StandardCharsets.UTF_8);

        // return file content
//        PowerMockito.doReturn(content).when(mockedFlow, "retrieveFile", client, flow.getInputPath());
        
        //.witharArguments(client, flow.getInputPath());

        flow.execute(client);

        // list input folder
        verify(client, times(1)).listFiles("/input");
        verify(client, times(1)).getReplyCode();
        verify(synchronizer, times(1)).onListing(1);
        
        // read file
        verify(synchronizer, times(1)).onStart("/input/mockFile");
        
        
        verifyNoMoreInteractions(client);
    }

    /**
     * 
     * @param e
     *            FTPConnectionClosedException, IOException,
     *            CopyStreamException, IOExceptioncausing
     * @throws IOException
     */
    public void processFileHelper(Class<? extends Throwable> e, boolean isSuccess) throws IOException {
        FTPFile file1 = mock(FTPFile.class);
        FTPFile file2 = mock(FTPFile.class);

        //
        when(client.listFiles(anyString())).thenReturn(new FTPFile[] { file1, file2 });

        when(client.getReplyCode()).thenReturn(200);
        when(file1.isDirectory()).thenReturn(false);
        when(file2.isDirectory()).thenReturn(false);
        if (e == null) {
            // no exception
            when(client.retrieveFile(anyString(), any(OutputStream.class))).thenReturn(isSuccess);
        } else {
            doThrow(e).when(client).retrieveFile(anyString(), any(OutputStream.class));
        }
        doThrow(e).when(client).retrieveFile(anyString(), any(OutputStream.class));

        InboundProcessingFlow flow = new InboundProcessingFlow("/input", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        flow.execute(client);

        verify(client, times(1)).listFiles(anyString());
        verify(client, times(1)).getReplyCode();
        verify(synchronizer, times(1)).onListing(2);
        verify(synchronizer, times(2)).onStart(anyString());
        verifyNoMoreInteractions(client);
    }

}
