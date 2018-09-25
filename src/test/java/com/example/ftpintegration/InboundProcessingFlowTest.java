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

import com.example.ftpintegration.ftp.FtpOperation;
import com.example.ftpintegration.ftp.flow.FtpFlowSynchronizer;
import com.example.ftpintegration.ftp.flow.InboundProcessingFlow;
import com.example.ftpintegration.processor.FileProcessor;

public class InboundProcessingFlowTest {

    private FtpOperation op;
    private FileProcessor processor;
    private FtpFlowSynchronizer synchronizer;

    @Before
    public void before() {
        op = mock(FtpOperation.class);
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
    public void listFileFail() throws IOException {

        // throw exception while listing input folder
        doThrow(IOException.class).when(op).listFiles(eq("/input"));

        InboundProcessingFlow flow = new InboundProcessingFlow("/input", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        try {
            flow.execute(op);
            fail("should throw exception here but not.");
        } catch (IOException e) {
            // good. error will be thrown out and processed there...
        }

        verify(op, times(1)).listFiles("/input");

        // no more processing
        verifyNoMoreInteractions(op);
        verifyNoMoreInteractions(synchronizer);
    }

    /**
     * empty input folder.
     * 
     * @throws IOException
     */
    @Test
    public void emptyInputFolder() throws IOException {

        // nothing from listing. input folder is empty. do nothing.
        when(op.listFiles(eq("/input"))).thenReturn(new FTPFile[] {});

        InboundProcessingFlow flow = new InboundProcessingFlow("/input", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        flow.execute(op);

        verify(op, times(1)).listFiles("/input");
        verify(synchronizer, times(1)).onListing(0);
        verifyNoMoreInteractions(op);
        verifyNoMoreInteractions(synchronizer);
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
        when(op.listFiles(anyString())).thenReturn(new FTPFile[] { file });

        // this file is actually a directory, which will not be processed.
        when(file.isDirectory()).thenReturn(true);

        InboundProcessingFlow flow = new InboundProcessingFlow("/input", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        flow.execute(op);

        verify(op, times(1)).listFiles("/input");
        verify(synchronizer, times(1)).onListing(1);
        verifyNoMoreInteractions(op);
        verifyNoMoreInteractions(synchronizer);
    }

    /**
     * Single file correct processing flow without archive folder.
     * 
     * @throws Exception
     */
    @Test
    public void processFileNoArchive() throws Exception {

        // a real flow object
        InboundProcessingFlow flow = new InboundProcessingFlow("/input", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        // the mocked file to use
        FTPFile file = mock(FTPFile.class);
        when(file.getName()).thenReturn("mockFile");

        // list files and return one file.
        when(op.listFiles(anyString())).thenReturn(new FTPFile[] { file });
        when(file.isDirectory()).thenReturn(false);

        // test file content
        byte[] content = "test".getBytes(StandardCharsets.UTF_8);

        // retrieve file content success
        when(op.retrieveFile(eq("/input/mockFile"))).thenReturn(content);

        // run
        flow.execute(op);

        // list input folder
        verify(op, times(1)).listFiles("/input");
        verify(synchronizer, times(1)).onListing(1);

        // get file info
        verify(file, times(1)).isDirectory();
        verify(file, times(1)).getName();

        // get file content
        verify(op, times(1)).retrieveFile("/input/mockFile");

        // start processing...
        verify(synchronizer, times(1)).onStart("/input/mockFile");
        verify(processor, times(1)).processFile(content);

        // trigger success result
        verify(synchronizer, times(1)).onSuccess("/input/mockFile");

        verifyNoMoreInteractions(op);
        verifyNoMoreInteractions(processor);
        verifyNoMoreInteractions(synchronizer);
    }

    /**
     * Single file correct processing flow.
     * 
     * @throws Exception
     */
    @Test
    public void processFile() throws Exception {

        // a real flow object
        InboundProcessingFlow flow = new InboundProcessingFlow("/input", "/archive", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        // the mocked file to use
        FTPFile file = mock(FTPFile.class);
        when(file.getName()).thenReturn("mockFile");

        // list files and return one file.
        when(op.listFiles(anyString())).thenReturn(new FTPFile[] { file });
        when(file.isDirectory()).thenReturn(false);

        // test file content
        byte[] content = "test".getBytes(StandardCharsets.UTF_8);

        // retrieve file content success
        when(op.retrieveFile(eq("/input/mockFile"))).thenReturn(content);

        // run
        flow.execute(op);

        // list input folder
        verify(op, times(1)).listFiles("/input");
        verify(synchronizer, times(1)).onListing(1);

        // get file info
        verify(file, times(1)).isDirectory();
        verify(file, times(1)).getName();

        // get file content
        verify(op, times(1)).retrieveFile("/input/mockFile");

        // start processing...
        verify(synchronizer, times(1)).onStart("/input/mockFile");
        verify(processor, times(1)).processFile(content);

        // store file to archive folder
        verify(op, times(1)).storeFile("/archive/mockFile", content);

        // delete input file
        verify(op, times(1)).deleteFile("/input/mockFile");

        // trigger success result
        verify(synchronizer, times(1)).onSuccess("/input/mockFile");

        verifyNoMoreInteractions(op);
        verifyNoMoreInteractions(processor);
        verifyNoMoreInteractions(synchronizer);
    }

    /**
     * Single file correct processing flow in test mode (no intrusive)
     * 
     * @throws Exception
     */
    @Test
    public void processFileDryRun() throws Exception {

        // a real flow object
        InboundProcessingFlow flow = new InboundProcessingFlow("/input", "/archive", processor);
        flow.setFtpFlowSynchronizer(synchronizer);
        flow.setDryRun(true);

        // the mocked file to use
        FTPFile file = mock(FTPFile.class);
        when(file.getName()).thenReturn("mockFile");

        // list files and return one file.
        when(op.listFiles(anyString())).thenReturn(new FTPFile[] { file });
        when(file.isDirectory()).thenReturn(false);

        // test file content
        byte[] content = "test".getBytes(StandardCharsets.UTF_8);

        // retrieve file content success
        when(op.retrieveFile(eq("/input/mockFile"))).thenReturn(content);

        // run
        flow.execute(op);

        // list input folder
        verify(op, times(1)).listFiles("/input");
        verify(synchronizer, times(1)).onListing(1);

        // get file info
        verify(file, times(1)).isDirectory();
        verify(file, times(1)).getName();

        // get file content
        verify(op, times(1)).retrieveFile("/input/mockFile");

        // start processing...
        verify(synchronizer, times(1)).onStart("/input/mockFile");
        verify(processor, times(1)).processFile(content);

        // store file to archive folder will not run
        verify(op, times(0)).storeFile("/archive/mockFile", content);

        // delete input file will not run
        verify(op, times(0)).deleteFile("/input/mockFile");

        // trigger success result
        verify(synchronizer, times(1)).onSuccess("/input/mockFile");

        verifyNoMoreInteractions(op);
        verifyNoMoreInteractions(processor);
        verifyNoMoreInteractions(synchronizer);
    }

    /**
     * Test single file read error.
     * 
     * @throws Exception
     */
    @Test
    public void readFileError() throws Exception {

        // a real flow object
        InboundProcessingFlow flow = new InboundProcessingFlow("/input", "/archive", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        // the mocked file to use
        FTPFile file = mock(FTPFile.class);
        when(file.getName()).thenReturn("mockFile");

        // list files and return one file.
        when(op.listFiles(anyString())).thenReturn(new FTPFile[] { file });
        when(file.isDirectory()).thenReturn(false);

        // retrieve file content error
        doThrow(IOException.class).when(op).retrieveFile(eq("/input/mockFile"));

        // run
        flow.execute(op);

        // list input folder
        verify(op, times(1)).listFiles("/input");
        verify(synchronizer, times(1)).onListing(1);

        // get file info
        verify(file, times(1)).isDirectory();
        verify(file, times(1)).getName();

        // start processing...
        verify(synchronizer, times(1)).onStart("/input/mockFile");

        // get file content
        verify(op, times(1)).retrieveFile("/input/mockFile");

        // trigger success result
        verify(synchronizer, times(1)).onFtpError(any(), any());

        verifyNoMoreInteractions(op);
        verifyNoMoreInteractions(processor);
        verifyNoMoreInteractions(synchronizer);
    }

    /**
     * Test single file read error.
     * 
     * @throws Exception
     */
    @Test
    public void processingError() throws Exception {

        // a real flow object
        InboundProcessingFlow flow = new InboundProcessingFlow("/input", "/archive", processor);
        flow.setFtpFlowSynchronizer(synchronizer);

        // the mocked file to use
        FTPFile file = mock(FTPFile.class);
        when(file.getName()).thenReturn("mockFile");

        // list files and return one file.
        when(op.listFiles(anyString())).thenReturn(new FTPFile[] { file });
        when(file.isDirectory()).thenReturn(false);

        // test file content
        byte[] content = "test".getBytes(StandardCharsets.UTF_8);

        // retrieve file content success
        when(op.retrieveFile(eq("/input/mockFile"))).thenReturn(content);

        // processing error (usually, file content error)
        doThrow(IOException.class).when(processor).processFile(content);

        // run and has error. will not throw exception out because it will continue
        // processing next file.
        flow.execute(op);

        // list input folder
        verify(op, times(1)).listFiles("/input");
        verify(synchronizer, times(1)).onListing(1);

        // get file info
        verify(file, times(1)).isDirectory();
        verify(file, times(1)).getName();

        // start processing...
        verify(synchronizer, times(1)).onStart("/input/mockFile");

        // get file content
        verify(op, times(1)).retrieveFile("/input/mockFile");

        // trigger success result
        verify(synchronizer, times(1)).onFileError(eq("/input/mockFile"), eq(content), any(), any());

        verifyNoMoreInteractions(op);
        verifyNoMoreInteractions(processor);
        verifyNoMoreInteractions(synchronizer);
    }
}
