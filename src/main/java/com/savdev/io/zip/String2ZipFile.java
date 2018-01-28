package com.savdev.io.zip;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;

public class String2ZipFile {

    /*
        Zips string,
        puts as an entry into zip archive root
     */
    public static void zipFileFromString(
            final String zipFilePath,
            final Charset encoding,
            final String fileNameEntryInsideZip,
            final String content) {

        ZipUtils.archiveZipBeforeJava7(zipFilePath, encoding,
                zos -> {
            try {
                ZipEntry e = new ZipEntry(fileNameEntryInsideZip);
                zos.putNextEntry(e);
                byte[] data = content.getBytes(encoding);
                zos.write(data, 0, data.length);
                zos.closeEntry();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
