package it.pagopa.pn.cucumber;

import lombok.CustomLog;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

@Getter
@CustomLog
public class Config {

    private static Config instance = null;

    private static final String PROPERTY_FILE = "application-test.properties";
    private static final String FILE_NOT_FOUND = "File properties non trovato";

    private Config() {
        try {
            Properties prop = new Properties();
            InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
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
