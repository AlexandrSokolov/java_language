package com.savdev.io.zip;

import com.savdev.io.BaseTest;
import com.savdev.utils.ZipFileAssert;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class InputStream2ZipFileTest extends BaseTest {

    public static final String CONTENT = "Test data to be zipped";

    @Test
    public void testFromUnzippedInputStream() throws IOException {
        InputStream inputStream =
                IOUtils.toInputStream(CONTENT,
                        StandardCharsets.UTF_8.name());

        File zipFile2Archive = createFileTemporary(ZIP_FILE_NAME);

        InputStream2ZipFile.fromUnzippedInputStream(
                zipFile2Archive,
                ZIP_ENTRY_NAME,
                StandardCharsets.UTF_8,
                inputStream);

        File zipFileFromResource = getFileFromTestResource(
                ZIP_FILE_NAME_EXPLICIT_ENTRY);
        ZipFileAssert.assertEquals(zipFile2Archive, zipFileFromResource);
    }
}
