package com.savdev.io.file;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Url2File {

    /*
        Using transferFrom() is potentially much more efficient
        than a simple loop that reads from the source channel
        and writes to this channel.
        Many operating systems can transfer bytes directly
        from the source channel into the filesystem cache
        without actually copying them.
     */
    public static void url2FileViaJavaNio(
            final URL url, final File file) {
        try {
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    public static void url2FileViaJava8Files(
            final URL url, final File file) {
        try (InputStream in = url.openStream()) {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void url2FileViaApacheCommons(
            final URL url, final File file) {
        try {
            FileUtils.copyURLToFile(url, file);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

    /*
        not clear how it works, tests are needed:
     */
    public static File url2FileViaApacheCommons(
            final URL url) {
        try {
            return Paths.get(url.toURI()).toFile();
            //vs
            //return new File(url.getFile());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
