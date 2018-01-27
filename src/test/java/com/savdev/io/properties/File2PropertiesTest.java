package com.savdev.io.properties;

import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class File2PropertiesTest {

    //defined via maven plugin:
    public static final String CONFIG_FOLDER_SETTING = "configFolder";

    public static final String PROP_FILE_NAME = "test.properties";
    public static final String INTEGER_PROP_NAME = "intProp";
    public static final String INTEGER_PROP_VALUE = "12";
    public static final String STRING_PROP_NAME = "stringProp";
    public static final String STRING_PROP_VALUE = "Hello, how are you?";

    @Test
    public void testLoadProperties(){
        Properties properties = new Properties();
        File2Properties.loadProperties(
                CONFIG_FOLDER_SETTING,
                PROP_FILE_NAME,
                properties);

        Assert.assertEquals(2, properties.size());
        Assert.assertEquals(INTEGER_PROP_VALUE,
                properties.getProperty(INTEGER_PROP_NAME));
        Assert.assertEquals(STRING_PROP_VALUE,
                properties.getProperty(STRING_PROP_NAME));
    }
}
