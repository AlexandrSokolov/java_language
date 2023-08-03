package com.savdev.io.properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class File2Properties {

    /**
     * Loads properties from a property file, located in
     * a folder, defined via commandline property setting
     *
     * @param configFolderProperty Folder location defined in a property via commandline:
     *                             -Dconfig=/home/alexandr/opt/brandmaker/mediasystem/config
     * @param fileName             property file name
     * @param properties
     * @return InputStream to load into properties
     */
    public static void loadProperties(
            final String configFolderProperty,
            final String fileName,
            final Properties properties) {

        if (properties == null) {
            throw new IllegalStateException("Properties is null.");
        }

        String folderViaSetting = System.getProperty(configFolderProperty);
        if (StringUtils.isEmpty(folderViaSetting)) {
            throw new IllegalStateException("Not defined: " + configFolderProperty);
        }

        File configFolder = new File(folderViaSetting);
        validateDirectory(configFolder);

        File configFile = new File(configFolder, fileName);
        validateFile(configFile);

        try {
            properties.load(new FileInputStream(configFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void validateUrlViaOptionsHttpMethod(String strUrl){
        try {
            URL url = new URL(strUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("OPTIONS");
            int status = con.getResponseCode();
            if (status != 200){
                throw new IllegalStateException("Not expected response code: "
                        + status + ", url: " + strUrl);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Not valid url: " + strUrl, e);
        }
    }

    //did not work correctly for actions, not always return correct result
    public static void validateUrlViaApacheValidator(String url){
        String[] schemes = {"http","https"}; // DEFAULT schemes = "http", "https", "ftp"
        UrlValidator urlValidator = new UrlValidator(schemes);
        if (!urlValidator.isValid(url)){
            throw new IllegalStateException("Url is not valid: " + url);
        }
    }

    public static void validateDirectory(File file) {
        if (!file.exists()) {
            throw new IllegalStateException("Directory not exist: "
                    + file.getAbsolutePath());
        }
        if (!file.isDirectory()) {
            throw new IllegalStateException("Directory, not file is expected: "
                    + file.getAbsolutePath());
        }
    }

    public static void validateFile(File file) {
        if (!file.exists()) {
            throw new IllegalStateException("File not exist: "
                    + file.getAbsolutePath());
        }
        if (file.isDirectory()) {
            throw new IllegalStateException("File, but not directory is expected: "
                    + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IllegalStateException("File exists, but cannot be read: "
                    + file.getAbsolutePath());
        }
    }

}
