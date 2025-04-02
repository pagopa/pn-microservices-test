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

    private static final String APPLICATION_TEST_PROPERTIES = "application.properties";
    private static final String FILE_NOT_FOUND = "File properties non trovato";
    private static final String SPRING_PROFILE = "spring.profiles.active";
    private static final String PROFILE_PROPERTIES_FILE_PREFIX = "application-";
    private static final String PROFILE_PROPERTIES_FILE_SUFFIX = ".properties";

    private Config() {}

    public void loadProperties() {
        // Load the application.properties file
        loadPropertiesIntoSystem(APPLICATION_TEST_PROPERTIES);
        // Load the application-{profile}.properties file
        loadPropertiesIntoSystem(PROFILE_PROPERTIES_FILE_PREFIX + System.getProperty(SPRING_PROFILE) + PROFILE_PROPERTIES_FILE_SUFFIX);
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

    public static Config getInstance() {
        if (Config.instance == null) {
            Config.instance = new Config();
        }

        return Config.instance;
    }


}
