package it.pagopa.pn.tests;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import it.pagopa.pn.cucumber.Config;
import it.pagopa.pn.cucumber.ExternalChannelUtils;
import it.pagopa.pn.cucumber.SqsUtils;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;

import javax.validation.constraints.AssertTrue;

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
    public void message_to_send(String clientId, String channel){
        this.clientId = clientId;
        this.channel = channel;

        System.out.println("CHANNEL: "+ channel.toUpperCase());
        System.out.println("CLIENTID: "+ clientId);
    }

    @When("try to send a digital message")
    public void presaInCarico(){
        this.requestId = ExternalChannelUtils.generateRandomRequestId();
        //switch sul canale
        switch (channel.toUpperCase()){
            case "SMS" -> ExternalChannelUtils.sendSmsCourtesySimpleMessage(clientId, requestId);
            case "EMAIL" -> ExternalChannelUtils.sendEmailCourtesySimpleMessage(clientId, requestId);
            case "PEC" -> ExternalChannelUtils.sendDigitalNotification(clientId, requestId);
            default -> throw new IllegalArgumentException();

        }


    }
    @Then("check if the message has been sent")
    public void checkStatusMessage(){
        String fileKey = null;
        boolean checked = SqsUtils.checkIfDocumentIsAvailable(requestId,nomeCodaEc);


        Assertions.assertTrue(checked);
    }

}
