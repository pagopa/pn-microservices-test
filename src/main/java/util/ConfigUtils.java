package util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class ConfigUtils {

    private static final String ENV_PROPERTY = "spring.profiles.active";
    private static final String ENV_CONFIG = "environment-config.json";
    private static final String APPLICATION_PROPERTIES = "application-test.properties";

    private static final String CLASSPATH_ERROR = "Cannot find file on classpath: ";
    private static final String FILE_LOAD_ERROR = "Failed to load :";

    public static Map<String, Map<String, String>> loadEnvironmentConfig() {
        try (InputStream environmentInputStream = ConfigUtils.class.getClassLoader().getResourceAsStream(ENV_CONFIG)) {
            if (environmentInputStream == null) {
                throw new IOException(CLASSPATH_ERROR + ENV_CONFIG);
            }
            return jsonStreamToMap(environmentInputStream);
        } catch (IOException e) {
            throw new RuntimeException(FILE_LOAD_ERROR + ENV_CONFIG, e);
        }
    }

    public static String loadActiveProfile() {
        try (InputStream applicationPropertiesInputStream = ConfigUtils.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES)) {
            Properties applicationProperties = new Properties();
            applicationProperties.load(applicationPropertiesInputStream);
            return applicationProperties.getProperty(ENV_PROPERTY);
        } catch (IOException e) {
            throw new RuntimeException(FILE_LOAD_ERROR + APPLICATION_PROPERTIES, e);
        }
    }

    private static Map<String, Map<String, String>> jsonStreamToMap(InputStream environmentInputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(environmentInputStream, new TypeReference<Map<String, Map<String, String>>>() {
        });
    }
}
