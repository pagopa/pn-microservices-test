package it.pagopa.pn.cucumber;

import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("tests/io")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "html:target/cucumber-reports/pn-io-cucumber-report.html")
@ConfigurationParameter(key = "cucumber.execution.exclusive-resources.ioActivationMockIo.read-write", value = "IO_ACTIVATION_MOCK_IO")
@ExcludeTags({"ignore", "wip"})
public class IoCucumberTest {
}
