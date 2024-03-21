package it.pagopa.pn.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class ApiKeysConfiguration {
    private final Map<String, Map<String, String>> environment;
    private final String envKey;
    private static ApiKeysConfiguration instance = null;
    private static final String TAG = "@";
    private static final String ENV_PROPERTY = "spring.profiles.active";
    private static final String ENV_CONFIG = "environment-config.json";
    private static final String APPLICATION_PROPERTIES = "application-test.properties";
    private static final String CLASSPATH_ERROR = "Cannot find file on classpath: ";
    private static final String FILE_LOAD_ERROR = "Failed to load :";

    public static ApiKeysConfiguration getInstance() {
        return instance == null ? new ApiKeysConfiguration() : instance;
    }

    private ApiKeysConfiguration() {
        this.environment = loadEnvironmentConfig();
        this.envKey = loadActiveProfile();
    }

    private Map<String, Map<String, String>> loadEnvironmentConfig() {
        try (InputStream environmentInputStream = getClass().getClassLoader().getResourceAsStream(ENV_CONFIG)) {
            if (environmentInputStream == null) {
                throw new IOException(CLASSPATH_ERROR + ENV_CONFIG);
            }
            return jsonStreamToMap(environmentInputStream);
        } catch (IOException e) {
            throw new RuntimeException(FILE_LOAD_ERROR + ENV_CONFIG, e);
        }
    }

    private String loadActiveProfile() {
        try (InputStream applicationPropertiesInputStream = getClass().getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES)) {
            Properties applicationProperties = new Properties();
            applicationProperties.load(applicationPropertiesInputStream);
            return applicationProperties.getProperty(ENV_PROPERTY);
        } catch (IOException e) {
            throw new RuntimeException(FILE_LOAD_ERROR + APPLICATION_PROPERTIES, e);
        }
    }

    public  String getValueIfTagged(String value){
        return isTagged(value) ? getValue(value.substring(1)) : value;
    }

    private String getValue(String key){
        return environment.get(envKey).get(key);
    }

    private boolean isTagged(String value){
        return value.startsWith(TAG);
    }

    private Map<String, Map<String, String>> jsonStreamToMap(InputStream inputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(inputStream, new TypeReference<Map<String, Map<String, String>>>() {});
    }
}