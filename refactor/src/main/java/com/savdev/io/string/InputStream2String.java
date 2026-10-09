package com.savdev.io.string;


import com.google.common.io.CharStreams;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.stream.Collectors;

/*
    https://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string/35446009#35446009
    https://habrahabr.ru/company/luxoft/blog/278233/
 */
public class InputStream2String {

    /*
        Fast and clear solution
    */
    public static String fromInputStreamViaApacheCommons(
            final InputStream inputStream,
            final Charset encoding) {
        try {
            return IOUtils.toString(inputStream, encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String fromInputStreamViaGuava(
            final InputStream inputStream,
            final Charset encoding) {
        try (InputStreamReader reader
                     = new InputStreamReader(
                inputStream, encoding)) {
            return CharStreams.toString(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The FASTEST solution
     *
     * @param inputStream
     * @param encoding
     * @return
     */
    public static String fromInputStreamViaByteArrayOutputStream(
            final InputStream inputStream,
            final Charset encoding) {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString(encoding.name());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
        Similar to the FASTEST solution with ByteArrayOutputStream
        but additionally wraps input stream into BufferedInputStream
        It is slower than the solition without BufferedInputStream
     */
    public static String fromInputStreamViaBufferedInputStreamAndByteArrayOutputStream(
            final InputStream inputStream,
            final Charset encoding) {
        try (BufferedInputStream bis =
                     new BufferedInputStream(inputStream);
             ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            int result = bis.read();
            while (result != -1) {
                buf.write((byte) result);
                result = bis.read();
            }
            return buf.toString(encoding.name());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String fromInputStreamViaSdkScanner(
            final InputStream inputStream,
            final Charset encoding) {
        try (Scanner s = new Scanner(inputStream, encoding.name())
                .useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    /*
        NOTE: changes \r\n to \n, it could be critical
     */
    public static String fromInputStreamViaSdkStreamApi(
            final InputStream inputStream,
            final Charset encoding) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, encoding))) {
            return br.lines().collect(
                    Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
        NOTE: changes \r\n to \n, it could be critical
     */
    public static String fromInputStreamViaSdkParallelStreamApi(
            final InputStream inputStream,
            final Charset encoding) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, encoding))) {
            return br.lines().parallel().collect(
                    Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param inputStream
     * @param encoding
     * @return
     */
    public static String fromInputStreamViaInputStreamReader(
            final InputStream inputStream,
            final Charset encoding) {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        try (Reader in = new InputStreamReader(inputStream, encoding.name())) {
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            return out.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
        Does not support unicode. Not recommended
     */
    public static String fromInputStreamViaStringBuilder(
            final InputStream inputStream,
            final Charset encoding) {
        try {
            int ch;
            StringBuilder sb = new StringBuilder();
            while ((ch = inputStream.read()) != -1)
                sb.append((char) ch);
            //reset(); what is it?
            return sb.toString();
        } catch (IOException e){
            throw new RuntimeException(e);
        }

    }


    /*

        This solution convert different line breaks (like \n\r)
        to line.separator system property
        (for example, in Windows to "\r\n")
     */
    public static String fromInputStreamViaBufferedReader(
            final InputStream inputStream,
            final Charset encoding) {
        String newLine = System.getProperty("line.separator");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, encoding))) {
            StringBuilder result = new StringBuilder();
            String line;
            boolean flag = false;
            while ((line = reader.readLine()) != null) {
                result.append(flag ? newLine : "").append(line);
                flag = true;
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
