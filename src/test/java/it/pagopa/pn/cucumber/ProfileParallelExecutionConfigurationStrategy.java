package it.pagopa.pn.cucumber;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

public class ProfileParallelExecutionConfigurationStrategy implements ParallelExecutionConfigurationStrategy {

    static final String FIXED_PARALLELISM_PARAMETER = "fixed.parallelism";
    static final String PARALLELISM_PROPERTY = "test.cucumber.parallelism";
    static final String SPRING_PROFILE = "spring.profiles.active";
    static final String APPLICATION_PROPERTIES = "application.properties";
    static final String PROFILE_PROPERTIES_FILE_PREFIX = "application-";
    static final String PROFILE_PROPERTIES_FILE_SUFFIX = ".properties";
    static final int DEFAULT_PARALLELISM = 30;

    private final Function<String, Properties> propertiesLoader;

    public ProfileParallelExecutionConfigurationStrategy() {
        this(ProfileParallelExecutionConfigurationStrategy::loadFromClasspath);
    }

    ProfileParallelExecutionConfigurationStrategy(Function<String, Properties> propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
    }

    @Override
    public ParallelExecutionConfiguration createConfiguration(ConfigurationParameters configurationParameters) {
        if (configurationParameters.get(FIXED_PARALLELISM_PARAMETER).isPresent()) {
            return DefaultParallelExecutionConfigurationStrategy.FIXED.createConfiguration(configurationParameters);
        }
        int parallelism = resolveProfileParallelism();
        return DefaultParallelExecutionConfigurationStrategy.FIXED.createConfiguration(withFixedParallelism(configurationParameters, parallelism));
    }

    private int resolveProfileParallelism() {
        String profile = resolveProfile();
        if (profile == null) {
            return DEFAULT_PARALLELISM;
        }
        String profileFile = PROFILE_PROPERTIES_FILE_PREFIX + profile + PROFILE_PROPERTIES_FILE_SUFFIX;
        Properties profileProperties = propertiesLoader.apply(profileFile);
        String value = profileProperties == null ? null : profileProperties.getProperty(PARALLELISM_PROPERTY);
        if (value == null) {
            return DEFAULT_PARALLELISM;
        }
        int parallelism;
        try {
            parallelism = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw invalidParallelism(profileFile, value);
        }
        if (parallelism <= 0) {
            throw invalidParallelism(profileFile, value);
        }
        return parallelism;
    }

    private static JUnitException invalidParallelism(String profileFile, String value) {
        return new JUnitException(String.format("Invalid value for %s in %s: '%s' (expected a positive integer)", PARALLELISM_PROPERTY, profileFile, value));
    }

    private String resolveProfile() {
        Properties applicationProperties = propertiesLoader.apply(APPLICATION_PROPERTIES);
        String profile = applicationProperties == null ? null : applicationProperties.getProperty(SPRING_PROFILE);
        return profile != null ? profile : System.getProperty(SPRING_PROFILE);
    }

    private static ConfigurationParameters withFixedParallelism(ConfigurationParameters delegate, int parallelism) {
        return new ConfigurationParameters() {
            @Override
            public Optional<String> get(String key) {
                return FIXED_PARALLELISM_PARAMETER.equals(key) ? Optional.of(String.valueOf(parallelism)) : delegate.get(key);
            }

            @Override
            public Optional<Boolean> getBoolean(String key) {
                return delegate.getBoolean(key);
            }

            @Override
            @SuppressWarnings("deprecation")
            public int size() {
                return keySet().size();
            }

            @Override
            public Set<String> keySet() {
                Set<String> keys = new HashSet<>(delegate.keySet());
                keys.add(FIXED_PARALLELISM_PARAMETER);
                return keys;
            }
        };
    }

    private static Properties loadFromClasspath(String fileName) {
        try (InputStream stream = ProfileParallelExecutionConfigurationStrategy.class.getClassLoader().getResourceAsStream(fileName)) {
            if (stream == null) {
                return null;
            }
            Properties properties = new Properties();
            properties.load(stream);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
