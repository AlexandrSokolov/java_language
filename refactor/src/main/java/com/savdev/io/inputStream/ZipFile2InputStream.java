package com.savdev.io.inputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFile2InputStream {

    public static void fromZipFile(
            final String zipFilePath,
            final Charset encoding,
            final Consumer<InputStream> consumer) {
        try {
            if (!Files.exists(Paths.get(zipFilePath))) {
                throw new IllegalStateException(
                        "ZipFile does not exist: "
                                + zipFilePath);
            }
            ZipFile zipFile = new ZipFile(zipFilePath, encoding);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                consumer.accept(zipFile.getInputStream(zipEntry));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
