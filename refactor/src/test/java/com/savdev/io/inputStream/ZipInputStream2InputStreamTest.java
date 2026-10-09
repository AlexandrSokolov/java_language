package com.savdev.io.inputStream;

import com.google.common.collect.Lists;
import com.savdev.io.BaseTest;
import com.savdev.io.string.InputStream2String;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipInputStream;

public class ZipInputStream2InputStreamTest extends BaseTest {

    @Test
    public void testFromZipInputStream() throws FileNotFoundException {
        File zipFileFromResource = getFileFromTestResource(
                ZIP_FILE_NAME_2ENTRIES);
        ZipInputStream zipInputStream =
                new ZipInputStream(
                        new FileInputStream(
                                zipFileFromResource));
        List<String> contentOfEachEntries = Lists.newArrayList();
        ZipInputStream2InputStream.fromZipInputStream(
                zipInputStream,
                inputStream -> {
                    contentOfEachEntries.add(
                            InputStream2String.fromInputStreamViaApacheCommons(
                                    inputStream, StandardCharsets.UTF_8));
                });
        Assert.assertEquals(2, contentOfEachEntries.size());
        Assert.assertEquals(FILE1_CONTENT, contentOfEachEntries.get(0));
        Assert.assertEquals(FILE2_CONTENT, contentOfEachEntries.get(1));
    }
}
