package com.savdev.io.zip;

import com.savdev.io.BaseTest;
import com.savdev.utils.ZipFileAssert;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.savdev.io.zip.InputStream2ZipFileTest.CONTENT;

public class String2ZipFileTest extends BaseTest {

    @Test
    public void testZipFileFromString() throws IOException {
        String zipFile2ArchivePath = filePathInTestTempFolder(ZIP_FILE_NAME);

        String2ZipFile.zipFileFromString(
                zipFile2ArchivePath,
                StandardCharsets.UTF_8,
                ZIP_ENTRY_NAME,
                CONTENT);

        File zipFileFromResource = getFileFromTestResource(
                ZIP_FILE_NAME_EXPLICIT_ENTRY);
        ZipFileAssert.assertEquals(new File(zipFile2ArchivePath),
                zipFileFromResource);
    }

}
