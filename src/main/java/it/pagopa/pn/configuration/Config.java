package it.pagopa.pn.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.CustomLog;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@Getter
@CustomLog
public class Config {

    private static Config instance = null;

    private static final String APPLICATION_TEST_PROPERTIES = "application-test.properties";
    private static final String FILE_NOT_FOUND = "File properties non trovato";
    private static final String SPRING_PROFILE = "spring.profiles.active";
    private static final String PROFILE_PROPERTIES_FILE_PREFIX = "test-variables-";
    private static final String PROFILE_PROPERTIES_FILE_SUFFIX = ".json";

    private Config() {}

    public void loadProperties() {
        loadPropertiesIntoSystem(APPLICATION_TEST_PROPERTIES);
      //  loadPropertiesIntoSystem(PROFILE_PROPERTIES_FILE_PREFIX + System.getProperty(SPRING_PROFILE) + PROFILE_PROPERTIES_FILE_SUFFIX);
        loadJsonPropertiesIntoSystem(PROFILE_PROPERTIES_FILE_PREFIX + System.getProperty(SPRING_PROFILE) + PROFILE_PROPERTIES_FILE_SUFFIX);

    }

    private void loadPropertiesIntoSystem(String propertyFileName) {
        try {
            Properties prop = new Properties();
            InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(propertyFileName);
            if (fileStream == null) {
                log.error(FILE_NOT_FOUND);
                System.exit(1);
            }
            prop.load(fileStream);
            prop.forEach((key, value) -> System.setProperty((String) key, (String) value));
        } catch (IOException ex) {
            log.error("Errore nel caricamento delle properties -> " + ex.getMessage());
            System.exit(1);
        }
    }

    private void loadJsonPropertiesIntoSystem(String propertyFileName) {
        try (InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(propertyFileName)) {
            if (fileStream == null) {
                log.error("JSON "+FILE_NOT_FOUND);
                System.exit(1);
            }
            Map<String, String> properties = jsonStreamToMap(fileStream);
            properties.forEach(log::debug);
            loadPropertiesIntoSystem(properties);
        } catch (IOException ex) {
            log.error("Errore nel caricamento delle properties JSON -> " + ex.getMessage());
            System.exit(1);
        }
    }
    private void loadPropertiesIntoSystem(Map<String, String> properties) {
        properties.forEach(System::setProperty);
    }

    private Map<String, String> jsonStreamToMap(InputStream environmentInputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(environmentInputStream, new TypeReference<Map<String, String>>() {
        });
    }

    public static Config getInstance() {
        if (Config.instance == null) {
            Config.instance = new Config();
        }

        return Config.instance;
    }


}
