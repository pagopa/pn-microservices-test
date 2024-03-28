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

    private String baseUrl;
    private String nomeCoda;
    private Long documentAvailabilityTimeout;

    private static final String PROPERTY_FILE = "application-test.properties";
    private static final String FILE_NOT_FOUND = "File properties non trovato";
    private static final String BASE_URL = "baseURL";
    private static final String NOME_CODA = "gestore.disponibilita.queue.name";
    private static final String DOCUMENT_AVAILABILITY_TIMEOUT = "document.availability.timeout.millis";

    private Config(){
        try {

        	baseUrl = System.getProperty(BASE_URL);
            nomeCoda = System.getProperty(NOME_CODA);
            String documentAvailabilityTimeoutString = System.getProperty(DOCUMENT_AVAILABILITY_TIMEOUT);

        	if( Objects.isNull(baseUrl)) {
	            Properties prop = new Properties();
	            InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
	            if(fileStream == null){
	                log.error(FILE_NOT_FOUND);
	                System.exit(1);
	            }
	            prop.load(fileStream);
	            this.baseUrl = prop.getProperty(BASE_URL);
        	}

            if (Objects.isNull(nomeCoda)) {
                Properties prop = new Properties();
                InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
                if(fileStream == null){
                    log.error(FILE_NOT_FOUND);
                    System.exit(1);
                }
                prop.load(fileStream);
                this.nomeCoda = prop.getProperty(NOME_CODA);
            }

            if (Objects.isNull(documentAvailabilityTimeout)) {
                Properties prop = new Properties();
                InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
                if(fileStream == null){
                    log.error(FILE_NOT_FOUND);
                    System.exit(1);
                }
                prop.load(fileStream);
                this.documentAvailabilityTimeout = Long.parseLong(prop.getProperty(DOCUMENT_AVAILABILITY_TIMEOUT));
            } else {
                this.documentAvailabilityTimeout = Long.parseLong(documentAvailabilityTimeoutString);
            }
            
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
