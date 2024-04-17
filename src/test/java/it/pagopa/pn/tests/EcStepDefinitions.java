package it.pagopa.pn.tests;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import it.pagopa.pn.configuration.TestVariablesConfiguration;
import it.pagopa.pn.cucumber.Checksum;
import it.pagopa.pn.cucumber.utils.CommonUtils;
import it.pagopa.pn.cucumber.utils.ExternalChannelUtils;
import it.pagopa.pn.cucumber.utils.SafeStorageUtils;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static it.pagopa.pn.cucumber.utils.CommonUtils.getMD5;
import static it.pagopa.pn.cucumber.utils.CommonUtils.getSHA256;
import static it.pagopa.pn.cucumber.utils.SqsUtils.checkMessageInSsDebugQueue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@CustomLog
public class EcStepDefinitions {

    private String clientId;
    private String requestId;
    private String qos;
    private String channel;

    private SsStepDefinitions ssStepDefinitions;
    private final List<String> fileKeyList = new ArrayList<>();
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
            case "PEC" -> ExternalChannelUtils.sendDigitalNotification(clientId, requestId, fileKeyList);
            default -> throw new IllegalArgumentException();
        };
        log.info(String.valueOf(response.getStatusCode()));
        log.info(response.getBody().asString());
        log.info(channel);
        assertEquals(200, response.getStatusCode());
    }



    @When("try to send a paper message")
    public void tryToSendAPaperMessage() {
        this.requestId = ExternalChannelUtils.generateRandomRequestId();
        Response response = ExternalChannelUtils.sendPaperMessage(clientId, requestId);
        assertEquals(200, response.getStatusCode());
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

    @And("I upload the following attachments:")
    public void uploadAttachments(DataTable dataTable) throws NoSuchAlgorithmException, IOException {
        List<List<String>> rows = dataTable.asLists(String.class);
        var sPNClient = parseIfTagged("@clientId-delivery");
        var sPNClient_AK = parseIfTagged("@delivery_api_key");
        for (List<String> row : rows.subList(1, rows.size())) {
            String documentType = parseIfTagged(row.get(0));
            String fileName = row.get(1);
            String mimeType = row.get(2);
            File file = new File(fileName);
            var sha256 = getSHA256(file);
            var md5 = getMD5(file);
            Response getPresignedUrlResp = SafeStorageUtils.getPresignedURLUpload(sPNClient, sPNClient_AK, mimeType, documentType, getSHA256(file), getMD5(file), "SAVED", true, Checksum.SHA256);
            assertEquals(200, getPresignedUrlResp.getStatusCode());
            String sURL = getPresignedUrlResp.then().extract().path("uploadUrl");
            String sKey = getPresignedUrlResp.then().extract().path("key");
            String sSecret = getPresignedUrlResp.then().extract().path("secret");
            fileKeyList.add("safestorage://" + sKey);
            Response uploadResp = CommonUtils.uploadFile(sURL, file, sha256, md5, mimeType, sSecret, Checksum.SHA256);
            assertEquals(200, uploadResp.getStatusCode());
        }
    }

    @After
    public void doFinally() throws IOException {
    }

    private String parseIfTagged(String value) {
        return TestVariablesConfiguration.getInstance().getValueIfTagged(value);
    }



}
