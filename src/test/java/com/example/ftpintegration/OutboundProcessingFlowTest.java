package com.example.ftpintegration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.example.ftpintegration.ftp.FtpAgent;
//import com.example.ftpintegration.ftp.flow.FtpFlowSynchronizer;
//import com.example.ftpintegration.ftp.flow.OutboundProcessingFlow;
import com.example.ftpintegration.processor.FileGenerator;

public class OutboundProcessingFlowTest {

//    private FtpOperation op;
//    private FileGenerator generator;
//    private FtpFlowSynchronizer synchronizer;
//
//    @Before
//    public void before() {
//        op = mock(FtpOperation.class);
//        generator = mock(FileGenerator.class);
//        synchronizer = mock(FtpFlowSynchronizer.class);
//    }
//
//    @Test
//    public void setupTest1() {
//        OutboundProcessingFlow flow = new OutboundProcessingFlow(generator);
//        assertEquals("default output folder is root.", "/", flow.getOutputPath());
//    }
//
//    @Test
//    public void setupTest2() {
//        OutboundProcessingFlow flow = new OutboundProcessingFlow("/output", generator);
//        assertEquals("output folder is wrong.", "/output", flow.getOutputPath());
//    }
//
//    /**
//     * operation is mandatory
//     */
//    @Test(expected = NullPointerException.class)
//    public void setupTest4() {
//        new OutboundProcessingFlow(null);
//    }
//
//    /**
//     * output path is mandatory
//     */
//    @Test(expected = NullPointerException.class)
//    public void setupTest5() {
//        new OutboundProcessingFlow(null, generator);
//    }
//
//    @Test
//    public void nullFileName() throws IOException {
//        OutboundProcessingFlow flow = new OutboundProcessingFlow("/output", generator);
//        flow.setFtpFlowSynchronizer(synchronizer);
//
//        when(generator.getFileName()).thenReturn(null);
//
//        try {
//            flow.execute(op);
//            fail("should die here...");
//        } catch (IOException e) {
//            // good.
//        }
//
//        verify(generator, times(1)).getFileName();
//        verify(synchronizer, times(1)).onFileError(eq(null), eq(null), any(), any());
//
//        verifyNoMoreInteractions(op);
//        verifyNoMoreInteractions(generator);
//        verifyNoMoreInteractions(synchronizer);
//    }
//
//    @Test
//    public void emptyFileName() throws IOException {
//        OutboundProcessingFlow flow = new OutboundProcessingFlow("/output", generator);
//        flow.setFtpFlowSynchronizer(synchronizer);
//
//        when(generator.getFileName()).thenReturn("");
//
//        try {
//            flow.execute(op);
//            fail("should die here...");
//        } catch (IOException e) {
//            // good.
//        }
//
//        verify(generator, times(1)).getFileName();
//        verify(synchronizer, times(1)).onFileError(eq(""), eq(null), any(), any());
//
//        verifyNoMoreInteractions(op);
//        verifyNoMoreInteractions(generator);
//        verifyNoMoreInteractions(synchronizer);
//    }
//
//    @Test
//    public void errorOnGeneratingFile() throws IOException {
//        OutboundProcessingFlow flow = new OutboundProcessingFlow("/output", generator);
//        flow.setFtpFlowSynchronizer(synchronizer);
//
//        when(generator.getFileName()).thenReturn("mockFile");
//        doThrow(IOException.class).when(generator).generateFile();
//
//        try {
//            flow.execute(op);
//            fail("should die here...");
//        } catch (IOException e) {
//            // good.
//        }
//
//        verify(generator, times(1)).getFileName();
//        verify(generator, times(1)).generateFile();
//        verify(synchronizer, times(1)).onFileError(eq("mockFile"), eq(null), any(), any());
//
//        verifyNoMoreInteractions(op);
//        verifyNoMoreInteractions(generator);
//        verifyNoMoreInteractions(synchronizer);
//    }
//
//    @Test
//    public void generateNullFile() throws IOException {
//        OutboundProcessingFlow flow = new OutboundProcessingFlow("/output", generator);
//        flow.setFtpFlowSynchronizer(synchronizer);
//
//        when(generator.getFileName()).thenReturn("mockFile");
//        when(generator.generateFile()).thenReturn(null);
//
//        try {
//            flow.execute(op);
//            fail("should die here...");
//        } catch (IOException e) {
//            // good.
//        }
//
//        verify(generator, times(1)).getFileName();
//        verify(generator, times(1)).generateFile();
//        verify(synchronizer, times(1)).onFileError(eq("mockFile"), eq(null), any(), any());
//
//        verifyNoMoreInteractions(op);
//        verifyNoMoreInteractions(generator);
//        verifyNoMoreInteractions(synchronizer);
//    }
//
//    @Test
//    public void uploadFail() throws IOException {
//        OutboundProcessingFlow flow = new OutboundProcessingFlow("/output", generator);
//        flow.setFtpFlowSynchronizer(synchronizer);
//
//        when(generator.getFileName()).thenReturn("mockFile");
//
//        byte[] content = "test".getBytes();
//        when(generator.generateFile()).thenReturn(content);
//
//        doThrow(IOException.class).when(op).storeFile(eq("/output/mockFile"), eq(content));
//
//        try {
//            flow.execute(op);
//            fail("should die here...");
//        } catch (IOException e) {
//            // good. ftp error will be captured and handled on upper level.
//        }
//
//        verify(generator, times(1)).getFileName();
//        verify(generator, times(1)).generateFile();
//        verify(op, times(1)).storeFile(eq("/output/mockFile"), eq(content));
//
//        verifyNoMoreInteractions(op);
//        verifyNoMoreInteractions(generator);
//        verifyNoMoreInteractions(synchronizer);
//    }
//
//    @Test
//    public void uploadSuccess() throws IOException {
//        OutboundProcessingFlow flow = new OutboundProcessingFlow("/output", generator);
//        flow.setFtpFlowSynchronizer(synchronizer);
//
//        when(generator.getFileName()).thenReturn("mockFile");
//
//        byte[] content = "test".getBytes();
//        when(generator.generateFile()).thenReturn(content);
//
//        flow.execute(op);
//
//        verify(generator, times(1)).getFileName();
//        verify(generator, times(1)).generateFile();
//        verify(op, times(1)).storeFile(eq("/output/mockFile"), eq(content));
//        verify(synchronizer, times(1)).onSuccess(eq("/output/mockFile"));
//
//        verifyNoMoreInteractions(op);
//        verifyNoMoreInteractions(generator);
//        verifyNoMoreInteractions(synchronizer);
//    }
}
