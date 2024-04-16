package it.pagopa.pn.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class Attachments {
    private final String envKey;
    private static final String TAG = "@";
    private static final String ENV_PROPERTY = "spring.profiles.active";
    private static final String APPLICATION_PROPERTIES = "application-test.properties";
    private static final String CLASSPATH_ERROR = "Cannot find file on classpath: ";
    private static final String FILE_LOAD_ERROR = "Failed to load :";

    private static final String PROPERTIES_PREFIX = "test-attachments-";
    private static final String PROPERTIES_SUFFIX = ".json";

    private Map<String, List<String>> properties;
    private String propertiesFile = "";

    private static Attachments instance = null;

    public static Attachments getInstance() {
        if (instance == null) Attachments.instance = new Attachments();
        return Attachments.instance;
    }

    private Attachments() {
        this.envKey = loadActiveProfile();
        propertiesFile = PROPERTIES_PREFIX + envKey + PROPERTIES_SUFFIX;
        properties = loadProperties(propertiesFile);
    }

    public static String loadActiveProfile() {
        try (InputStream applicationInputStream = Attachments.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES)) {
            Properties applicationProp = new Properties();
            applicationProp.load(applicationInputStream);
            String property = applicationProp.getProperty(ENV_PROPERTY);
            return property;
        } catch (IOException e) {
            throw new RuntimeException(FILE_LOAD_ERROR + APPLICATION_PROPERTIES, e);
        }
    }

    //
    private Map<String, List<String>> loadProperties(String propertiesFile) {
        try(InputStream envInputStream = Attachments.class.getClassLoader().getResourceAsStream(propertiesFile)) {
            if(envInputStream == null){
                throw new IOException(CLASSPATH_ERROR + propertiesFile);
            }

            // Read JSON content from the input stream
            String jsonContent = new BufferedReader(new InputStreamReader(envInputStream))
                    .lines().collect(Collectors.joining("\n"));

            // Convert JSON content to map
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println("map: " +jsonContent);
            return objectMapper.readValue(jsonContent, new TypeReference<Map<String, List<String>>>() {});
        } catch (IOException e) {
            throw new RuntimeException(FILE_LOAD_ERROR + propertiesFile, e);
        }
    }

    private Map<String, String> jsonStreamToMap(InputStream environmentInputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(environmentInputStream, new TypeReference<Map<String, String>>() {
        });
    }

    private boolean isTagged(String value) {
        return value.startsWith(TAG);
    }

    public String getValueIfTagged(String value) {
        return isTagged(value) ? getValue(value.substring(1)) : value;
    }

    private String getValue(String key) {
        return properties.get(key).toString();
    }
}
