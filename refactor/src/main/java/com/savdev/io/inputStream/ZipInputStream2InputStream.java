package com.savdev.io.inputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipInputStream2InputStream {


    public static void fromZipInputStream(
            final ZipInputStream zipInputStream,
            final Consumer<InputStream> consumer){
        try {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null){
                consumer.accept(zipInputStream);
                zipEntry = zipInputStream.getNextEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
