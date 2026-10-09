package com.savdev.io.zip;

import com.google.common.collect.ImmutableMap;
import com.savdev.io.BaseTest;
import com.savdev.utils.ZipFileAssert;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class File2ZipFileTest extends BaseTest {

    File textFileFromResource = getFileFromTestResource(TEXT_FILE_NAME);

    /*
        we explicit specify zip entry inside of zip archive
     */
    @Test
    public void testCreateZipArchive() {
        String zipFilePath2Archive = filePathInTestTempFolder(ZIP_FILE_NAME);

        File2ZipFile.createZipArchive(zipFilePath2Archive,
                StandardCharsets.UTF_8,
                ImmutableMap.of(ZIP_ENTRY_NAME, textFileFromResource));

        File zipFileFromResource = getFileFromTestResource(
                ZIP_FILE_NAME_EXPLICIT_ENTRY);

        File zipFile = new File(zipFilePath2Archive);
        ZipFileAssert.assertEquals(zipFile, zipFileFromResource);
    }

    /*
        we explicit specify zip entry inside of zip archive
            file for zip is created incorrectly
    */
    @Test(expected = IllegalStateException.class )
    public void testCreateZipArchiveWrongZip() throws IOException {
        String zipFilePath2Archive =
                filePathInTestTempFolder(ZIP_FILE_NAME);

        new File(zipFilePath2Archive).createNewFile();

        try {
            File2ZipFile.createZipArchive(zipFilePath2Archive,
                    StandardCharsets.UTF_8,
                    ImmutableMap.of(ZIP_ENTRY_NAME,
                            textFileFromResource));
            Assert.fail();
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().startsWith(
                    "File for zip exists, but it is not zip:"
            ));
            throw e;
        }
    }

    /*
        in this case a name of entry inside of zip file
            is taken from original file
     */
    @Test
    public void testCreateZipArchiveNoExplicitName() {

        String zipFile2ArchivePath =
                filePathInTestTempFolder(ZIP_FILE_NAME);
        File2ZipFile.createZipArchive(zipFile2ArchivePath,
                StandardCharsets.UTF_8,
                Collections.singleton(textFileFromResource));

        File zipTarget2Compare =
                getFileFromTestResource(ZIP_FILE_NAME_DEFAULT_ENTRY);

        ZipFileAssert.assertEquals(new File(zipFile2ArchivePath),
                zipTarget2Compare);
    }

    /*
        in this case a name of entry inside of zip file
            is taken from original file
    */
    @Test(expected = IllegalStateException.class)
    public void testCreateZipArchiveNoExplicitNameWrongZipFile()
            throws IOException {

        String zipFile2ArchivePath =
                filePathInTestTempFolder(ZIP_FILE_NAME);
        new File(zipFile2ArchivePath).createNewFile();

        try {
            File2ZipFile.createZipArchive(zipFile2ArchivePath,
                    StandardCharsets.UTF_8,
                    Collections.singleton(textFileFromResource));
            Assert.fail();
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().startsWith(
                    "File for zip exists, but it is not zip:"
            ));
            throw e;
        }
    }

    /*
        it does not matter how file for zip archive is named
        even if it has no extension, it will be zip archive
    */
    @Test
    public void testCreateZipArchiveWrongZipFileExtension() {
        String zipFile2ArchivePath = filePathInTestTempFolder(WRONG_ZIP_EXTENSION_FILE_NAME);
        File2ZipFile.createZipArchive(zipFile2ArchivePath,
                StandardCharsets.UTF_8,
                Collections.singleton(textFileFromResource));

        File zipTarget2Compare =
                getFileFromTestResource(ZIP_FILE_NAME_DEFAULT_ENTRY);
        ZipFileAssert.assertEquals(new File(zipFile2ArchivePath),
                zipTarget2Compare);
    }

    /*
        before java 7
     */
    @Test
    public void testCreateZipArchiveBeforeJava7() {

        String zipFile2Archive = filePathInTestTempFolder(ZIP_FILE_NAME);
        File2ZipFile.createZipArchiveBeforeJava7(zipFile2Archive,
                StandardCharsets.UTF_8,
                Collections.singleton(textFileFromResource));

        File zipTarget2Compare =
                getFileFromTestResource(ZIP_FILE_NAME_DEFAULT_ENTRY);

        ZipFileAssert.assertEquals(new File(zipFile2Archive),
                zipTarget2Compare);
    }

    /**
     * In this case, if a zip file is created via
     *      new File().createNewFile();
     *      it is totally legal, and can be used for zipping
     *
     * @throws IOException
     */
    @Test
    public void testCreateZipArchiveBeforeJava7WrongZipFile()
            throws IOException {

        String zipFile2Archive = filePathInTestTempFolder(ZIP_FILE_NAME);
        File newFile = new File(zipFile2Archive);
        newFile.createNewFile();

        File2ZipFile.createZipArchiveBeforeJava7(zipFile2Archive,
                StandardCharsets.UTF_8,
                Collections.singleton(textFileFromResource));

        File zipTarget2Compare =
                getFileFromTestResource(ZIP_FILE_NAME_DEFAULT_ENTRY);
        ZipFileAssert.assertEquals(newFile, zipTarget2Compare);
    }

    /*
        via com.github.axet.zip4j
    */
    @Test
    @Ignore
    public void testZipFileFromFileViaZip4J() {

        String zipFile2ArchivePath = filePathInTestTempFolder(ZIP_FILE_NAME);
        File2ZipFile.zipFileFromFileViaZip4J(zipFile2ArchivePath,
                StandardCharsets.UTF_8,
                ImmutableMap.of(ZIP_ENTRY_NAME, textFileFromResource));

        File zipFileFromResource = getFileFromTestResource(
                ZIP_FILE_NAME_EXPLICIT_ENTRY);

        ZipFileAssert.assertEquals(new File(zipFile2ArchivePath),
                zipFileFromResource);

    }
}
