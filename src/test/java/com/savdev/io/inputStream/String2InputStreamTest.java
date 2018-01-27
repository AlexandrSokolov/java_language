package com.savdev.io.inputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class String2InputStreamTest {

    public static final String CONTENT = "test string data";

    @Test
    public void testGetInputStream() throws IOException {
        InputStream inputStream =
                String2InputStream.getInputStream(
                        CONTENT, StandardCharsets.UTF_8);
        Assert.assertEquals(CONTENT,
                IOUtils.toString(inputStream, StandardCharsets.UTF_8));
    }

    @Test
    public void testGetInputStreamViaApacheCommons() throws IOException {
        InputStream inputStream =
                String2InputStream.getInputStreamViaApacheCommons(
                        CONTENT, StandardCharsets.UTF_8);
        Assert.assertEquals(CONTENT,
                IOUtils.toString(inputStream, StandardCharsets.UTF_8));
    }
}
