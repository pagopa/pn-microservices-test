package it.pagopa.pn.cucumber;

import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("tests/ss")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value="html:target/cucumber-reports/cucumber.html" )
@ExcludeTags({"ignore"})
@IncludeTags({"PnSsUpload", "PnSsUpdateMetadata", "GetConfigurations"})
public class SsCucumberTest {
}
