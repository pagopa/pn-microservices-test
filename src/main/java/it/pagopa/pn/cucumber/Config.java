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
    private String nomeCodaEc;
    private String smsReceiverDigitalAddress;
    private Long documentAvailabilityTimeout;

    private static final String PROPERTY_FILE = "application-test.properties";
    private static final String FILE_NOT_FOUND = "File properties non trovato";
    private static final String BASE_URL = "baseURL";
    private static final String NOME_CODA = "gestore.disponibilita.queue.name";
    private static final String NOME_CODA_EC = "notifiche.esterne.queue.name";
    private static final String DOCUMENT_AVAILABILITY_TIMEOUT = "document.availability.timeout.millis";
    private static final String SMS_RECEIVER_DIGITAL_ADDRESS = "sms.receiver.digital.address";

    private Config(){
        try {

        	baseUrl = System.getProperty(BASE_URL);
            nomeCoda = System.getProperty(NOME_CODA);
            nomeCodaEc = System.getProperty(NOME_CODA_EC);
            smsReceiverDigitalAddress = System.getProperty(SMS_RECEIVER_DIGITAL_ADDRESS);
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

            if (Objects.isNull(nomeCodaEc)) {
                Properties prop = new Properties();
                InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
                if(fileStream == null){
                    log.error(FILE_NOT_FOUND);
                    System.exit(1);
                }
                prop.load(fileStream);
                System.out.println(prop);
                System.out.println(System.getProperties());
                this.nomeCodaEc = prop.getProperty(NOME_CODA_EC);
            }

            if (Objects.isNull(smsReceiverDigitalAddress)) {
                Properties prop = new Properties();
                InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
                if(fileStream == null){
                    log.error(FILE_NOT_FOUND);
                    System.exit(1);
                }
                prop.load(fileStream);
                this.smsReceiverDigitalAddress = prop.getProperty(SMS_RECEIVER_DIGITAL_ADDRESS);
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
