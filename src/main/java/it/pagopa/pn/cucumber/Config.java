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

    private Config(){
        try {

        	baseUrl = System.getProperty("baseURL");
            nomeCoda = System.getProperty("gestore.disponibilita.queue.name");
        	
        	if( Objects.isNull(baseUrl)) {
	            Properties prop = new Properties();
	            InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream("application-test.properties");
	            if(fileStream == null){
	                log.error("File properties non trovato");
	                System.exit(1);
	            }
	            prop.load(fileStream);
	            this.baseUrl = prop.getProperty("baseURL");
        	}

            if (Objects.isNull(nomeCoda)) {
                Properties prop = new Properties();
                InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream("application-test.properties");
                if(fileStream == null){
                    log.error("File properties non trovato");
                    System.exit(1);
                }
                prop.load(fileStream);
                this.nomeCoda = prop.getProperty("gestore.disponibilita.queue.name");
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
