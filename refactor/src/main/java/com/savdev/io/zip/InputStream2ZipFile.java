package com.savdev.io.zip;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;


public class InputStream2ZipFile {

    private static byte[] buffer = new byte[1024];

    /*
        Java 7 solution via FileSystem

        thows IllegalStateException if zip file is not zip,
            for instance if File is created via:
            new File(filePath).createNewFile();

        Changes permission of the file, leaves permissions only to the owner
    */
    public static void fromUnzippedInputStream(
            final String zipFilePath,
            final Charset encoding,
            final String zipEntryName,
            final InputStream inputStream) {

        ZipUtils.archiveZip(zipFilePath, encoding, zipfs -> {
            Path pathInZipfile = zipfs.getPath(zipEntryName);
            // copy InputStream into the zip file
            try {
                Files.copy(inputStream, pathInZipfile,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /*
        Before Java 7 solution via FileSystem

        Allows to use files, created via
            new File(filePath).createNewFile();

        Does not change permissions
    */
    public static void fromUnzippedInputStreamBefore7Java(
            final String zipFilePath, final String zipEntryName,
            final Charset encoding,
            final InputStream inputStream) {

        ZipUtils.archiveZipBeforeJava7(zipFilePath,
                encoding, zos -> {
            try {
                ZipEntry ze = new ZipEntry(zipEntryName);
                //ze.setMethod(ZipEntry.DEFLATED);
                //ze.setCrc();
                zos.putNextEntry(ze);
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                zos.closeEntry();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
