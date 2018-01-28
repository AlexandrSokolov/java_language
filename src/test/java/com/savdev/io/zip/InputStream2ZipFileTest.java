package com.savdev.io.zip;

import com.savdev.io.BaseTest;
import com.savdev.utils.ZipFileAssert;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
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

        String zipFile2ArchivePath = filePathInTestTempFolder(ZIP_FILE_NAME);

        InputStream2ZipFile.fromUnzippedInputStream(
                zipFile2ArchivePath,
                StandardCharsets.UTF_8,
                ZIP_ENTRY_NAME,
                inputStream);

        File zipFileFromResource = getFileFromTestResource(
                ZIP_FILE_NAME_EXPLICIT_ENTRY);
        ZipFileAssert.assertEquals(new File(zipFile2ArchivePath),
                zipFileFromResource);
    }

    @Test(expected = IllegalStateException.class)
    public void testFromUnzippedInputStreamWrongZipFile() throws IOException {
        InputStream inputStream =
                IOUtils.toInputStream(CONTENT,
                        StandardCharsets.UTF_8.name());

        String zipFile2ArchivePath = filePathInTestTempFolder(ZIP_FILE_NAME);

        new File(zipFile2ArchivePath).createNewFile();

        try {
            InputStream2ZipFile.fromUnzippedInputStream(
                    zipFile2ArchivePath,
                    StandardCharsets.UTF_8,
                    ZIP_ENTRY_NAME,
                    inputStream);
            Assert.fail();
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().startsWith(
                    "File for zip exists, but it is not zip:"
            ));
            throw e;
        }
    }

    @Test
    public void testFromUnzippedInputStreamBefore7Java() throws IOException {
        InputStream inputStream =
                IOUtils.toInputStream(CONTENT,
                        StandardCharsets.UTF_8.name());

        String zipFile2ArchivePath = filePathInTestTempFolder(ZIP_FILE_NAME);

        InputStream2ZipFile.fromUnzippedInputStreamBefore7Java(
                zipFile2ArchivePath,
                ZIP_ENTRY_NAME,
                StandardCharsets.UTF_8,
                inputStream);

        File zipFileFromResource = getFileFromTestResource(
                ZIP_FILE_NAME_EXPLICIT_ENTRY);
        ZipFileAssert.assertEquals(new File(zipFile2ArchivePath),
                zipFileFromResource);
    }

    /*
        This way of zipping allows to create a file using:
            new File(zipFile2ArchivePath).createNewFile();
     */
    @Test
    public void testFromUnzippedInputStreamBefore7JavaWrongZip() throws IOException {
        InputStream inputStream =
                IOUtils.toInputStream(CONTENT,
                        StandardCharsets.UTF_8.name());

        String zipFile2ArchivePath = filePathInTestTempFolder(ZIP_FILE_NAME);

        File zipFile = new File(zipFile2ArchivePath);
        zipFile.createNewFile();

        InputStream2ZipFile.fromUnzippedInputStreamBefore7Java(
                zipFile2ArchivePath,
                ZIP_ENTRY_NAME,
                StandardCharsets.UTF_8,
                inputStream);

        File zipFileFromResource = getFileFromTestResource(
                ZIP_FILE_NAME_EXPLICIT_ENTRY);
        ZipFileAssert.assertEquals(zipFile,
                zipFileFromResource);
    }
}
