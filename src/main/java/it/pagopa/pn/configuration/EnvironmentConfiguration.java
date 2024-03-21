package it.pagopa.pn.configuration;

import java.util.Map;

import static util.ConfigUtils.loadActiveProfile;
import static util.ConfigUtils.loadEnvironmentConfig;

public class EnvironmentConfiguration {
    private final Map<String, Map<String, String>> environment;
    private final String envKey;
    private static EnvironmentConfiguration instance = null;

    private static final String TIMING= "timing";

    public static EnvironmentConfiguration getInstance() {
        return instance == null ? new EnvironmentConfiguration() : instance;
    }

    private EnvironmentConfiguration() {
        this.environment = loadEnvironmentConfig();
        this.envKey = loadActiveProfile();
    }


    public String getValue(String key){
        return environment.get(envKey).get(key);
    }

    public Long getTiming(){
        return Long.parseLong(environment.get(envKey).get(TIMING));
    }




}






