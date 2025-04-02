package it.pagopa.pn.cucumber;

import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("tests/ec")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value="html:target/cucumber-reports/cucumber.html" )
@ExcludeTags({"ignore"})
@IncludeTags({"PnEcSendMessage", "PnEcGetMessage"})
public class EcCucumberTest {
}
