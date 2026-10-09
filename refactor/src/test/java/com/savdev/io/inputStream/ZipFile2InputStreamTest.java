package com.savdev.io.inputStream;

import com.google.common.collect.Lists;
import com.savdev.io.BaseTest;
import com.savdev.io.string.InputStream2String;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ZipFile2InputStreamTest extends BaseTest {

    @Test
    public void testFromZipFile(){
        File zipFileFromResource = getFileFromTestResource(
                ZIP_FILE_NAME_2ENTRIES);
        List<String> contentOfEachEntries = Lists.newArrayList();
        ZipFile2InputStream.fromZipFile(
                zipFileFromResource.getAbsolutePath(),
                StandardCharsets.UTF_8,
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
