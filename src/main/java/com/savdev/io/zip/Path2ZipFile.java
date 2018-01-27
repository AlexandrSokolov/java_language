package com.savdev.io.zip;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class Path2ZipFile {

    /*
        fileNameEntryInsideZip2ExternalFilePath
            - a mapping between entry name inside of zip arhchive
            - and Path of external real file that is zipped with the function
     */
    public static void zipFileFromPath(
            final File zipFile,
            final Map<String, Path> fileNameEntryInsideZip2ExternalFilePath) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        URI uri = URI.create("jar:file:" + zipFile.getAbsolutePath());

        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            fileNameEntryInsideZip2ExternalFilePath
                    .forEach((fileNameEntryInsideZip, externalFilePath) -> {
                        Path pathInZipfile = zipfs.getPath(fileNameEntryInsideZip);

                        try {
                            // copy a file into the zip file
                            Files.copy(externalFilePath, pathInZipfile,
                                    StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
