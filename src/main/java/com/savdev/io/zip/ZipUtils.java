package com.savdev.io.zip;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static final String NOT_ZIP_FILE_ERROR
            = "zip END header not found";

    public static void archiveZip(
            final String filePath, final Charset encoding,
            final Consumer<FileSystem> consumer){
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", encoding.name());
        URI uri = URI.create("jar:file:" + filePath);
        //if new empty file is created as a text file, we get an exception:
        //Caused by: java.util.zip.ZipError: zip END header not found
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            consumer.accept(zipfs);
        } catch (java.util.zip.ZipError zipError) {
            if (NOT_ZIP_FILE_ERROR.equals(zipError.getMessage())){
                //when file is created via:
                //new File(zipFilePath2Archive).createNewFile();
                //it is considered as not a zip file
                throw new IllegalStateException(
                        "File for zip exists, but it is not zip: "
                                + filePath);
            } else {
                throw zipError;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  It is used for java before version 7,
     *      old style try/catch is used
     * @param zipFilePath
     * @param encoding
     * @param consumer
     */
    public static void archiveZipBeforeJava7(
            final String zipFilePath,
            final Charset encoding,
            final Consumer<ZipOutputStream> consumer){
        FileOutputStream fos;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFilePath);
            zos = new ZipOutputStream(fos, encoding);
            consumer.accept(zos);
        } catch(IOException e){
            throw new RuntimeException(e);
        } finally {
            if (zos != null){
                try {
                    zos.close();
                } catch (IOException e) {
                    throw new RuntimeException("Could not close archive", e);
                }
            }
        }
    }
}
