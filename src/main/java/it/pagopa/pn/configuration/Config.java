package it.pagopa.pn.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.CustomLog;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@CustomLog
public class Config {

    private static Config instance = null;

    private static final String APPLICATION_TEST_PROPERTIES = "application.properties";
    private static final String FILE_NOT_FOUND = "File properties non trovato";
    private static final String SPRING_PROFILE = "spring.profiles.active";
    private static final String PROFILE_PROPERTIES_FILE_PREFIX = "application-";
    private static final String PROFILE_PROPERTIES_FILE_SUFFIX = ".properties";
    private static final Pattern ENV_VARIABLE_PLACEHOLDER = Pattern.compile("^\\$\\{([A-Za-z0-9_]+)}$");

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
                log.error("{}: {}", FILE_NOT_FOUND, propertyFileName);
                System.exit(1);
            }
            prop.load(fileStream);
            prop.forEach((key, value) -> System.setProperty((String) key, resolveValue((String) key, (String) value)));
        } catch (IOException ex) {
            log.error("Errore nel caricamento del file properties {}", propertyFileName, ex);
            System.exit(1);
        }
    }

    private String resolveValue(String key, String value) {
        Matcher matcher = ENV_VARIABLE_PLACEHOLDER.matcher(value.trim());
        if (!matcher.matches()) {
            return value;
        }
        String variableName = matcher.group(1);
        String variableValue = System.getenv(variableName);
        if (variableValue == null || variableValue.isBlank()) {
            throw new IllegalStateException(String.format(
                    "La property %s richiede la variabile d'ambiente %s, che non è impostata", key, variableName));
        }
        return variableValue;
    }

    public static Config getInstance() {
        if (Config.instance == null) {
            Config.instance = new Config();
        }

        return Config.instance;
    }


}
