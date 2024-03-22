package it.pagopa.pn.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;


public class TestVariablesConfiguration {
    private final String envKey;
    private static final String TAG = "@";
    private static final String ENV_PROPERTY = "spring.profiles.active";
    private static final String APPLICATION_PROPERTIES = "application-test.properties";
    private static final String CLASSPATH_ERROR = "Cannot find file on classpath: ";
    private static final String FILE_LOAD_ERROR = "Failed to load :";

    private static final String PROPERTIES_PREFIX = "test-variables-";
    private static final String PROPERTIES_SUFFIX = ".json";

    private Map<String, String> properties;
    private String propertiesFile = "";


    private static TestVariablesConfiguration instance = null;

    public static TestVariablesConfiguration getInstance() {
        if (instance == null) TestVariablesConfiguration.instance = new TestVariablesConfiguration();
        return TestVariablesConfiguration.instance;
    }

    private TestVariablesConfiguration() {
        this.envKey = loadActiveProfile();
        propertiesFile = PROPERTIES_PREFIX + envKey + PROPERTIES_SUFFIX;
        properties = loadProperties(propertiesFile);
    }

    public String getValueIfTagged(String value) {
        return isTagged(value) ? getValue(value.substring(1)) : value;
    }

    private String getValue(String key) {
        return properties.get(key);
    }

    private Map<String, String> loadProperties(String propertiesFile) {
        try (InputStream environmentInputStream = TestVariablesConfiguration.class.getClassLoader().getResourceAsStream(propertiesFile)) {
            if (environmentInputStream == null) {
                throw new IOException(CLASSPATH_ERROR + propertiesFile);
            }
            return jsonStreamToMap(environmentInputStream);
        } catch (IOException e) {
            throw new RuntimeException(FILE_LOAD_ERROR + propertiesFile, e);
        }
    }

    private Map<String, String> jsonStreamToMap(InputStream environmentInputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(environmentInputStream, new TypeReference<Map<String, String>>() {
        });
    }


    public static String loadActiveProfile() {
        try (InputStream applicationPropertiesInputStream = TestVariablesConfiguration.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES)) {
            Properties applicationProperties = new Properties();
            applicationProperties.load(applicationPropertiesInputStream);
            String property = applicationProperties.getProperty(ENV_PROPERTY);
            return property;
        } catch (IOException e) {
            throw new RuntimeException(FILE_LOAD_ERROR + APPLICATION_PROPERTIES, e);
        }
    }

    private boolean isTagged(String value) {
        return value.startsWith(TAG);
    }

}