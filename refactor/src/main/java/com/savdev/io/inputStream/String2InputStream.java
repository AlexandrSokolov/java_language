package com.savdev.io.inputStream;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class String2InputStream {

    public static InputStream getInputStream(
            final String content,
            final Charset encoding){
        try {
            return new ByteArrayInputStream(content.getBytes(encoding.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getInputStreamViaApacheCommons(
            final String content,
            final Charset encoding){
        try {
            return IOUtils.toInputStream(content, encoding.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
