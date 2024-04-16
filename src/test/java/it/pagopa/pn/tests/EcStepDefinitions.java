package it.pagopa.pn.tests;

import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import it.pagopa.pn.configuration.TestVariablesConfiguration;
import it.pagopa.pn.cucumber.utils.ExternalChannelUtils;
import it.pagopa.pn.cucumber.utils.SqsUtils;
import it.pagopa.pn.ec.rest.v1.api.CourtesyMessageProgressEvent;
import it.pagopa.pn.ec.rest.v1.api.LegalMessageSentDetails;
import it.pagopa.pn.ec.rest.v1.api.PaperProgressStatusEvent;
import it.pagopa.pn.safestorage.generated.openapi.server.v1.dto.UpdateFileMetadataRequest;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static it.pagopa.pn.cucumber.utils.SqsUtils.checkMessageInSsDebugQueue;

@CustomLog
public class EcStepDefinitions {

    private String clientId;
    private String requestId;
    private String qos;
    private String channel;

    private SsStepDefinitions ssStepDefinitions;
    private List<String> fileKeyList;
    private String fileKey;
    private static String nomeCodaNotifiche;


    @BeforeAll
    public static void loadPropertiesForQueue() {
        //nomeCodaEc = Config.getInstance().getNomeCodaEc();
      //  nomeCodaNotifiche = System.getProperty("notifiche.esterne.queue.name");
    }

    @Given("a {string} and {string} to send on")
    public void messageToSend(String clientId, String channel) {
        this.clientId = parseIfTagged(clientId);
        this.channel = parseIfTagged(channel);
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
        log.info(String.valueOf(response.getStatusCode()));
        log.info(response.getBody().asString());
        log.info(channel);
        Assertions.assertEquals(200, response.getStatusCode());
    }



    @When("try to send a paper message")
    public void tryToSendAPaperMessage() {
        this.requestId = ExternalChannelUtils.generateRandomRequestId();
        Response response = ExternalChannelUtils.sendPaperMessage(clientId, requestId);
        Assertions.assertEquals(200, response.getStatusCode());
    }


    @Then("check if the message has been sent")
    public void checkStatusMessage() {
        log.info("requestId {}", requestId);
        String queueName = System.getProperty("notifiche.esterne.queue.name");
        boolean checked = switch (this.channel.toUpperCase()) {
            case "SMS" -> SqsUtils.checkMessageInEcDebugQueue(requestId, queueName, CourtesyMessageProgressEvent.EventCodeEnum.S003.getValue());
            case "EMAIL" -> SqsUtils.checkMessageInEcDebugQueue(requestId, queueName, CourtesyMessageProgressEvent.EventCodeEnum.M003.getValue());
            case "PEC" -> SqsUtils.checkMessageInEcDebugQueue(requestId, queueName, LegalMessageSentDetails.EventCodeEnum.C000.getValue());
            case "PAPER" -> SqsUtils.checkMessageInEcDebugQueue(requestId, queueName, "P000");
            default -> throw new IllegalArgumentException();
        };
        Assertions.assertTrue(checked);
    }


    @Then("check ricezione esiti")
    public void checkRicezioneEsiti() {
    }

    @And("{string} authenticated by {string} try to upload a document of type {string} with content type {string} using {string}")
    public void a_file_to_upload(String sPNClient, String sPNClient_AK, String sDocumentType, String sMimeType, String sFileName) throws NoSuchAlgorithmException, FileNotFoundException, IOException {

        sPNClient = parseIfTagged(sPNClient);
        sPNClient_AK = parseIfTagged(sPNClient_AK);
        sDocumentType = parseIfTagged(sDocumentType);
        sMimeType = parseIfTagged(sMimeType);
        sFileName = parseIfTagged(sFileName);

        ssStepDefinitions.a_file_to_upload(sPNClient, sPNClient_AK, sDocumentType, sMimeType, sFileName);
    }

    @After
    public void doFinally() throws IOException {
    }

    private String parseIfTagged(String value) {
        return TestVariablesConfiguration.getInstance().getValueIfTagged(value);
    }



}
