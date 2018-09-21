package com.example.ftpintegration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.example.ftpintegration.ftp.flow.InboundProcessingFlow;
import com.example.ftpintegration.processor.FileProcessor;

public class InboundProcessingFlowTest {

    private FileProcessor processor;

    @Before
    public void before() {
        processor = mock(FileProcessor.class);
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

    @Test(expected = NullPointerException.class)
    public void setupTest4() {
        new InboundProcessingFlow(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setupTest5() {
        new InboundProcessingFlow("/abc", "/abc", processor);
    }
}
