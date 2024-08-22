package it.pagopa.pn.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;


public class TestVariablesConfiguration {
    private static final String TAG = "@";

    public static String getValueIfTagged(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return isTagged(value) ? getValue(value.substring(1)) : value;
    }

    private static String getValue(String key) {
        return System.getProperty(key);
    }

    private static boolean isTagged(String value) {
        return value.startsWith(TAG);
    }

}