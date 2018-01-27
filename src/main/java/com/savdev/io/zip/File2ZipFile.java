package com.savdev.io.zip;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class File2ZipFile {

    private static byte[] buffer = new byte[1024];

    /*
        Zips original files
            and adds them to original zip archive.

        fileNameEntryInsideZip2ExternalFile
            - a mapping between entry name inside of zip archive
            - and external real file that is zipped with the function

        Via Java 7 FileSystems.newFileSystem
     */
    public static void addFiles2ZipArchive(
            final File zipFile, final Charset encoding,
            final Map<String, File> fileNameEntryInsideZip2ExternalFile) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", encoding.name());

        URI uri = URI.create("jar:file:" + zipFile.getAbsolutePath());

        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            fileNameEntryInsideZip2ExternalFile
                    .forEach((fileNameEntryInsideZip, externalFile) -> {
                        Path externalTxtFile = externalFile.toPath();
                        Path pathInZipfile = zipfs.getPath(fileNameEntryInsideZip);
                        // copy a file into the zip file
                        try {
                            Files.copy(externalTxtFile, pathInZipfile,
                                    StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
        Zips original files
            and adds them to original zip archive.

        Inside of zip archive,
        entries have the same name as names in external original files

        Via Java 7 FileSystems.newFileSystem
     */
    public static void addFiles2ZipArchive(
            final File zipFile, final Charset encoding,
            final Iterable<File> externalFiles) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", encoding.name());

        URI uri = URI.create("jar:file:" + zipFile.getAbsolutePath());

        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            externalFiles
                    .forEach(externalFile -> {
                        Path externalTxtFile = externalFile.toPath();
                        Path pathInZipfile = zipfs.getPath(externalFile.getName());
                        // copy a file into the zip file
                        try {
                            Files.copy(externalTxtFile, pathInZipfile,
                                    StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
        Uses original Java features to copy bytes,
            via ZipOutputStream and ZipEntry
            without usage of Files.copy() function

        Inside of zip archive,
        entries have the same name as names in external original files
    */
    public static void zipFileFromFileBeforeJava7(
            final File zipFile, final Charset encoding,
            final Iterable<File> externalFiles) {
        FileOutputStream fos;
        ZipOutputStream zos = null;
        try {

            fos = new FileOutputStream(zipFile.getAbsoluteFile());
            zos = new ZipOutputStream(fos, encoding);
            for(File file : externalFiles){
                ZipEntry ze = new ZipEntry(file.getName());
                //ze.setMethod(ZipEntry.DEFLATED);
                //ze.setCrc();
                zos.putNextEntry(ze);
                FileInputStream in = new FileInputStream(file);
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                in.close();
                zos.closeEntry();
            }
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

    /*

        NOTE: does not work!!! API is broken!
        exception:
        net.lingala.zip4j.exception.ZipException:
            java.lang.ClassCastException:
                java.io.File cannot be cast to
                    net.lingala.zip4j.core.NativeStorage


        Via com.github.axet.zip4j

        This method allows to configure zip with different parameters
        see configureZipParameters() function

        fileNameEntryInsideZip2ExternalFile
        - a mapping between entry name inside of zip arhchive
        - and external real file that is zipped with the function
    */
    public static void zipFileFromFileViaZip4J(
            final File zipFile, final Charset encoding,
            final Map<String, File> fileNameEntryInsideZip2ExternalFile) {
        if (true)
            throw new RuntimeException("Not fixed bug:"
                + "");
        try {
            ZipFile zipFileWrapper = new ZipFile(zipFile.getAbsolutePath());
            zipFileWrapper.setFileNameCharset(encoding.name());
            fileNameEntryInsideZip2ExternalFile
                    .forEach((entryName, externalfile) -> {
                        try {
                            zipFileWrapper.addFile(externalfile,
                                    configureZipParameters(entryName));
                        } catch (ZipException e) {
                            throw new RuntimeException(e);
                        }
                    });


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }



    private static ZipParameters configureZipParameters(
            final String entryName) {
        //Initiate Zip Parameters which define various properties
        ZipParameters parameters = new ZipParameters();
        parameters.setFileNameInZip(entryName);

        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

        //DEFLATE_LEVEL_FASTEST     - Lowest compression level but higher speed of compression
        //DEFLATE_LEVEL_FAST        - Low compression level but higher speed of compression
        //DEFLATE_LEVEL_NORMAL  - Optimal balance between compression level/speed
        //DEFLATE_LEVEL_MAXIMUM     - High compression level with a compromise of speed
        //DEFLATE_LEVEL_ULTRA       - Highest compression level but low speed
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

        //Set the encryption flag to true
        parameters.setEncryptFiles(false);

        //Set the encryption method to AES Zip Encryption
        //parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

        //AES_STRENGTH_128 - For both encryption and decryption
        //AES_STRENGTH_192 - For decryption only
        //AES_STRENGTH_256 - For both encryption and decryption
        //Key strength 192 cannot be used for encryption. But if a zip file already has a
        //file encrypted with key strength of 192, then Zip4j can decrypt this file
        //parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

        //Set password
        //parameters.setPassword("howtodoinjava");

        return parameters;
    }
}
