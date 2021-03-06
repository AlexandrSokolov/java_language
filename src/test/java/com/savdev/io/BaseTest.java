package com.savdev.io;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class BaseTest {

    public static final String TEXT_FILE_NAME = "text.txt";
    public static final String ZIP_FILE_NAME = "temp.zip";
    public static final String WRONG_ZIP_EXTENSION_FILE_NAME
            = "wrong.txt";
    public static final String ZIP_FILE_NAME_EXPLICIT_ENTRY
            = "withExplicitEntryName.zip";
    public static final String ZIP_ENTRY_NAME = "entryName.txt";
    public static final String ZIP_FILE_NAME_DEFAULT_ENTRY
            = "withDefaultEntryName.zip";
    public static final String ZIP_FILE_NAME_2ENTRIES =
            "with2ZipEntries.zip";
    public static final String FILE1_CONTENT =
            "content of the 1st file\n";
    public static final String FILE2_CONTENT =
            "content of the 2nd file\n";

    public File getFileFromTestResource(String fileName){
        URL fileUrl = BaseTest.class.getClassLoader()
                .getResource(fileName);
        try {
            return new File(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public String filePathInTestTempFolder(final String fileName){
        try {
            File testTempFolder = folder.newFolder();
            return new File(testTempFolder, fileName).getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
