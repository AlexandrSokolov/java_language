package com.savdev.io.inputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Url2InputStreamTest {

    public static final String FILE_URL = "re";
    public static final String HTTP_URL = "re";
    public static final String CONTENT = "test data";

    @Test
    public void testInputStreamViaHttpUrl() throws IOException {
        InputStream inputStream =
                Url2InputStream.inputStream(HTTP_URL);
        Assert.assertEquals(CONTENT,
                IOUtils.toString(inputStream, StandardCharsets.UTF_8));
    }

    @Test
    public void testInputStreamViaFileUrl() throws IOException {
        InputStream inputStream =
                Url2InputStream.inputStream(FILE_URL);
        Assert.assertEquals(CONTENT,
                IOUtils.toString(inputStream, StandardCharsets.UTF_8));
    }

    @Test
    public void testInputStreamViaURLConnectionViaHttpUrl() throws IOException {
        InputStream inputStream =
                Url2InputStream.inputStream(HTTP_URL);
        Assert.assertEquals(CONTENT,
                IOUtils.toString(inputStream, StandardCharsets.UTF_8));
    }

    @Test
    public void testInputStreamViaURLConnectionViaFileUrl() throws IOException {
        InputStream inputStream =
                Url2InputStream.inputStream(FILE_URL);
        Assert.assertEquals(CONTENT,
                IOUtils.toString(inputStream, StandardCharsets.UTF_8));
    }
}
