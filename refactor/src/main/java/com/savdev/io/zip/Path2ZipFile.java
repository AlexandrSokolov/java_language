package com.savdev.io.zip;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class Path2ZipFile {

    /*
        fileNameEntryInsideZip2ExternalFilePath
            - a mapping between entry name inside of zip arhchive
            - and Path of external real file that is zipped with the function
     */
    public static void zipFileFromPath(
            final String zipFilePath, final Charset encoding,
            final Map<String, Path> fileNameEntryInsideZip2ExternalFilePath) {

        ZipUtils.archiveZip(zipFilePath, encoding, zipfs -> {
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
        });
    }
}
