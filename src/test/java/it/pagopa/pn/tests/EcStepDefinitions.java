package it.pagopa.pn.tests;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import it.pagopa.pn.cucumber.Config;
import it.pagopa.pn.cucumber.ExternalChannelUtils;
import it.pagopa.pn.cucumber.SqsUtils;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;

@CustomLog
public class EcStepDefinitions {

    private String clientId;
    private String requestId;
    private String qos;
    private String channel;
    private String fileKey;
    private static String nomeCodaEc;


    @BeforeAll
    public static void loadPropertiesForQueue() {
        nomeCodaEc = Config.getInstance().getNomeCodaEc();
    }

    @Given("a {string} and {string} to send on")
    public void message_to_send(String clientId, String channel) {
        this.clientId = clientId;
        this.channel = channel;
        log.debug("CHANNEL: " + channel.toUpperCase());
        log.debug("CLIENTID: " + clientId);
    }

    @When("try to send a digital message")
    public void presaInCarico() {
        this.requestId = ExternalChannelUtils.generateRandomRequestId();
        //switch sul canale
        Response response = switch (channel.toUpperCase()) {
            case "SMS" -> ExternalChannelUtils.sendSmsCourtesySimpleMessage(clientId, requestId);
            case "EMAIL" -> ExternalChannelUtils.sendEmailCourtesySimpleMessage(clientId, requestId);
            case "PEC" -> ExternalChannelUtils.sendDigitalNotification(clientId, requestId);
            default -> throw new IllegalArgumentException();
        };
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Then("check if the message has been sent")
    public void checkStatusMessage() {
        boolean checked = SqsUtils.checkMessageInDebugQueue(requestId, nomeCodaEc);
        Assertions.assertTrue(checked);
    }

}
