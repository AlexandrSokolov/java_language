package com.savdev.io.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;


public class InputStream2ZipFile {

    /*
        Java 7 solution via FileSystem
    */
    public static void fromUnzippedInputStream(
            final File zipFile, final String zipEntryName,
            final Charset encoding,
            final InputStream inputStream) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", encoding.name());

        URI uri = URI.create("jar:file:" + zipFile.getAbsolutePath());

        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            Path pathInZipfile = zipfs.getPath(zipEntryName);
            // copy InputStream into the zip file
            Files.copy(inputStream, pathInZipfile,
                        StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
