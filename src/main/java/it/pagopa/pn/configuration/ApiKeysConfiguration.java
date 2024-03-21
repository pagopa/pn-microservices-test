package it.pagopa.pn.configuration;

import java.util.Map;

import static util.ConfigUtils.loadActiveProfile;
import static util.ConfigUtils.loadEnvironmentConfig;

public class ApiKeysConfiguration {
    private final Map<String, Map<String, String>> environment;
    private final String envKey;
    private static ApiKeysConfiguration instance = null;
    private static final String TAG = "@";

    public static ApiKeysConfiguration getInstance() {
        return instance == null ? new ApiKeysConfiguration() : instance;
    }

    private ApiKeysConfiguration() {
        this.environment = loadEnvironmentConfig();
        this.envKey = loadActiveProfile();
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


}