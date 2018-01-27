package com.savdev.io.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class String2File {


    //wrong does not support encoding!
    public static void string2File(
            final File file,
            final String fileContent,
            final CharSequence encoding){
        try(PrintWriter out = new PrintWriter(file)) {
            out.println(fileContent);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
