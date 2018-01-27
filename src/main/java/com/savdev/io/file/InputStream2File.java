package com.savdev.io.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class InputStream2File {

    public static void getFile(
            final File file, final InputStream inputStream){
        try {
            Files.copy(inputStream, file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
