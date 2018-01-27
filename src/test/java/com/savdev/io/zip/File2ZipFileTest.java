package com.savdev.io.zip;

import com.google.common.collect.ImmutableMap;
import com.savdev.io.BaseTest;
import com.savdev.utils.ZipFileAssert;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class File2ZipFileTest extends BaseTest {

    File textFileFromResource = getFileFromTestResource(TEXT_FILE_NAME);

    /*
        we explicit specify zip entry inside of zip archive
     */
    @Test
    public void testAddFiles2ZipArchive() {
        File zipFile2Archive = createFileTemporary(ZIP_FILE_NAME);
        File2ZipFile.addFiles2ZipArchive(zipFile2Archive,
                StandardCharsets.UTF_8,
                ImmutableMap.of(ZIP_ENTRY_NAME, textFileFromResource));

        File zipFileFromResource = getFileFromTestResource(
                ZIP_FILE_NAME_EXPLICIT_ENTRY);

        Assert.assertFalse(zipFile2Archive == zipFileFromResource);
        ZipFileAssert.assertEquals(zipFile2Archive, zipFileFromResource);
    }

    /*
        in this case a name of entry inside of zip file
            is taken from original file
     */
    @Test
    public void testAddFiles2ZipArchiveNoExplicitName() {

        File zipFile2Archive = createFileTemporary(ZIP_FILE_NAME);
        File2ZipFile.addFiles2ZipArchive(zipFile2Archive,
                StandardCharsets.UTF_8,
                Collections.singleton(textFileFromResource));

        File zipTarget2Compare =
                getFileFromTestResource(ZIP_FILE_NAME_DEFAULT_ENTRY);

        Assert.assertFalse(zipFile2Archive == zipTarget2Compare);
        ZipFileAssert.assertEquals(zipFile2Archive, zipTarget2Compare);
    }

    /*
        it does not matter how file for zip archive is named
        even if it has no extension, it will be zip archive
    */
    @Test
    public void testAddZippedFiles2TextFile() {
        File zipFile2Archive = createFileTemporary(WRONG_ZIP_EXTENSION_FILE_NAME);
        File2ZipFile.addFiles2ZipArchive(zipFile2Archive,
                StandardCharsets.UTF_8,
                Collections.singleton(textFileFromResource));

        File zipTarget2Compare =
                getFileFromTestResource(ZIP_FILE_NAME_DEFAULT_ENTRY);

        Assert.assertFalse(zipFile2Archive == zipTarget2Compare);
        ZipFileAssert.assertEquals(zipFile2Archive, zipTarget2Compare);
    }

    /*
        before java 7
     */
    @Test
    public void testZipFileFromFileBeforeJava7() {

        File zipFile2Archive = createFileTemporary(ZIP_FILE_NAME);
        File2ZipFile.zipFileFromFileBeforeJava7(zipFile2Archive,
                StandardCharsets.UTF_8,
                Collections.singleton(textFileFromResource));

        File zipTarget2Compare =
                getFileFromTestResource(ZIP_FILE_NAME_DEFAULT_ENTRY);

        Assert.assertFalse(zipFile2Archive == zipTarget2Compare);
        ZipFileAssert.assertEquals(zipFile2Archive, zipTarget2Compare);
    }

    /*
        via com.github.axet.zip4j
    */
    @Test
    @Ignore
    public void testZipFileFromFileViaZip4J() {

        File zipFile2Archive = createFileTemporary(ZIP_FILE_NAME);
        File2ZipFile.zipFileFromFileViaZip4J(zipFile2Archive,
                StandardCharsets.UTF_8,
                ImmutableMap.of(ZIP_ENTRY_NAME, textFileFromResource));

        File zipFileFromResource = getFileFromTestResource(
                ZIP_FILE_NAME_EXPLICIT_ENTRY);

        Assert.assertFalse(zipFile2Archive == zipFileFromResource);
        ZipFileAssert.assertEquals(zipFile2Archive, zipFileFromResource);

    }
}
