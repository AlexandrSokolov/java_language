package com.savdev.io.inputStream;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class File2InputStream {

    public static InputStream fromFile(File file){
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static InputStream fromFileViaApacheCommons(File file){
        try {
            return FileUtils.openInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream fromFileViaGuava(File file){
        try {
            return Files.asByteSource(file).openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
