package com.savdev.io.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class String2ZipFile {

    /*
        Zips string,
        puts as an entry into zip archive root
     */
    public static void zipFileFromString(
            final File zipFile,
            final String fileNameEntryInsideZip,
            final String content, Charset encoding) {
        try (ZipOutputStream out = new ZipOutputStream(
                new FileOutputStream(zipFile));) {

            ZipEntry e = new ZipEntry(fileNameEntryInsideZip);
            out.putNextEntry(e);

            byte[] data = content.getBytes(encoding);
            out.write(data, 0, data.length);
            out.closeEntry();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
