package it.pagopa.pn.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.AfterAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import it.pagopa.pn.configuration.Config;
import it.pagopa.pn.cucumber.RequestTemplate;
import it.pagopa.pn.cucumber.dto.pojo.Checksum;
import it.pagopa.pn.cucumber.dto.pojo.PnAttachment;
import it.pagopa.pn.cucumber.poller.PnEcQueuePoller;
import it.pagopa.pn.cucumber.utils.*;
import it.pagopa.pn.ec.rest.v1.api.*;
import jakarta.jms.JMSException;
import lombok.CustomLog;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static it.pagopa.pn.configuration.TestVariablesConfiguration.getValueIfTagged;
import static it.pagopa.pn.cucumber.utils.CommonUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@CustomLog
public class EcStepDefinitions {

    private String clientId;
    private String apiKey;
    private String requestId;
    private String channel;
    private String receiver;
    private int sendPaperMessageStatusCode;
    private final List<PnAttachment> attachmentsList = new ArrayList<>();
    private final Set<String> statusesToCheck = new HashSet<>();
    private int sendPaperProgressStatusRespCode = 0;
    private String sendPaperProgressStatusResultCode;
    private String sendPaperProgressStatusResultDescription;
    private List<String> sendPaperProgressStatusErrorList;
    private List<ConsolidatoreIngressPaperProgressStatusEventAttachments> paperProgressStatusEventAttachments = new ArrayList<>();
    private static PnEcQueuePoller queuePoller;
    private String sRC;
    private Response response;
    private OffsetDateTime testStartTime;


    static {
        try {
            Config.getInstance().loadProperties();
            queuePoller = new PnEcQueuePoller();
            queuePoller.startPolling();
        } catch (JMSException e) {
            throw new RuntimeException("Error initializing queue poller", e);
        }
    }

    //GIVEN
    @Given("a {string} and {string} to send on")
    public void messageToSend(String clientId, String channel) {
        this.clientId = getValueIfTagged(clientId);
        this.channel = getValueIfTagged(channel);
        log.info("ClientId {}", this.clientId);
        log.debug("Channel {}", this.channel);

    }

    @Given("{string} authenticated by {string}")
    public void authenticatedBy(String clientId, String apiKey) {
        this.clientId = getValueIfTagged(clientId);
        this.apiKey = getValueIfTagged(apiKey);
    }

    @Given("a {string} to send request")
    public void aClientToSendRequest(String clientId) {
        this.clientId = getValueIfTagged(clientId);
        log.info("ClientId {}",this.clientId);
    }


    //WHEN
    @When("try to send a paper message")
    public void tryToSendAPaperMessage() {
        this.requestId = ExternalChannelUtils.generateRandomRequestId(clientId);
        Response response = ExternalChannelUtils.sendPaperMessage(clientId, requestId, attachmentsList);
        this.sendPaperMessageStatusCode = response.getStatusCode();
    }

    @When("try to send a digital message to {string}")
    public void presaInCarico(String receiver) {
        this.requestId = ExternalChannelUtils.generateRandomRequestId(clientId);
        this.receiver = getValueIfTagged(receiver);
        log.info("receiver address {}", this.receiver);
        //switch sul canale
        Response response = switch (channel.toUpperCase()) {
            case "SMS" -> ExternalChannelUtils.sendSmsCourtesySimpleMessage(clientId, requestId, this.receiver);
            case "EMAIL" -> ExternalChannelUtils.sendEmailCourtesySimpleMessage(clientId, requestId, this.receiver);
            case "PEC" ->
                    ExternalChannelUtils.sendDigitalNotification(clientId, requestId, attachmentsList, this.receiver);
            default -> throw new IllegalArgumentException();
        };
        assertEquals(200, response.getStatusCode());
    }

    @When("try to send a paper message to {string}")
    public void tryToSendAPaperMessage(String receiver) {
        this.requestId = ExternalChannelUtils.generateRandomRequestId(clientId);
        this.receiver = getValueIfTagged(receiver);
        Response response = ExternalChannelUtils.sendPaperMessage(clientId, requestId, attachmentsList);
        this.sendPaperMessageStatusCode = response.getStatusCode();
    }

    @When("try to get client configurations")
    public void tryToGetClientConfigurations() {
        this.response = ExternalChannelUtils.getClient(this.clientId);
        this.sRC = String.valueOf(response.getStatusCode());
    }

    @When("try to get all client configurations")
    public void tryToGetAllClientConfigurations() {
        this.response = ExternalChannelUtils.getClientConfigurations(this.clientId);
        this.sRC = String.valueOf(response.getStatusCode());
    }

    @When("try to get request by {string}")
    public void tryToGetRequestByRequestId(String requestId) {
        this.response = ExternalChannelUtils.getRequest(clientId, getValueIfTagged(requestId));
        this.sRC = String.valueOf(response.getStatusCode());
    }

    @When("try to get request by messageId {string}")
    public void tryToGetRequestByMessageId(String messageId) {

        if(Objects.equals(messageId, "messageIdNotFound")) {
            this.response = ExternalChannelUtils.getRequestByMessageId(ExternalChannelUtils.encodeMessageId(clientId,
                    ExternalChannelUtils.generateRandomRequestId(clientId)));

        } else {
            this.response = ExternalChannelUtils.getRequestByMessageId(getValueIfTagged(messageId));
        }
        this.sRC = String.valueOf(response.getStatusCode());
    }

    @When("try to get attachment with a {string}")
    public void tryToGetAttachmentWithFileKey(String fileKey) {
        fileKey = getValueIfTagged(fileKey);
        this.response = ExternalChannelUtils.getAttachmentsByFileKey(fileKey, clientId, apiKey);
        this.sRC = String.valueOf(response.getStatusCode());
    }

    @When("try to get result")
    public void getResultRequest() {
        Response response = switch (channel.toUpperCase()) {
            case "SMS" -> ExternalChannelUtils.getSmsByRequestId(clientId, requestId);
            case "EMAIL" -> ExternalChannelUtils.getEmailByRequestId(clientId, requestId);
            case "PEC" -> ExternalChannelUtils.getPecByRequestId(clientId, requestId);
            case "PAPER" -> ExternalChannelUtils.getPaperByRequestId(clientId, requestId);
            default -> throw new IllegalArgumentException();
        };
        log.info("Channel {}",channel);
        this.sRC = String.valueOf(response.getStatusCode());
    }

    @When("try to get result with a {string}")
    public void getResultRequestByRequestId(String requestId) {
        Response response = switch (channel.toUpperCase()) {
            case "SMS" -> ExternalChannelUtils.getSmsByRequestId(clientId, getValueIfTagged(requestId));
            case "EMAIL" -> ExternalChannelUtils.getEmailByRequestId(clientId, getValueIfTagged(requestId));
            case "PEC" -> ExternalChannelUtils.getPecByRequestId(clientId, getValueIfTagged(requestId));
            case "PAPER" -> ExternalChannelUtils.getPaperByRequestId(clientId, getValueIfTagged(requestId));
            default -> throw new IllegalArgumentException();
        };
        log.info("Channel {}",channel);
        this.sRC = String.valueOf(response.getStatusCode());
    }

    @When("try to send digital message to {string} with {string}")
    public void tryToSendDigitalMessageTo(String receiver, String requestId) {
        this.requestId = getValueIfTagged(requestId);
        this.receiver = getValueIfTagged(receiver);
        log.info("receiver address {}", this.receiver);
        //switch sul canale
        Response response = switch (channel.toUpperCase()) {
            case "SMS" -> this.response = ExternalChannelUtils.sendSmsCourtesySimpleMessage(clientId, requestId, this.receiver);
            case "EMAIL" -> this.response = ExternalChannelUtils.sendEmailCourtesySimpleMessage(clientId, requestId, this.receiver);
            case "PEC" ->
                    this.response = ExternalChannelUtils.sendDigitalNotification(clientId, requestId, attachmentsList, this.receiver);
            default -> throw new IllegalArgumentException();
        };
        this.sRC = String.valueOf(response.getStatusCode());
    }

    @When("try to send a digital message to {string} with same requestId")
    public void tryToSendADigitalMessageToWithSameRequestId(String receiver) {
        this.receiver = getValueIfTagged(receiver);
        Response response = switch (channel.toUpperCase()) {
            case "SMS" -> this.response = ExternalChannelUtils.sendSmsCourtesySimpleMessageErr(clientId, requestId, this.receiver);
            case "EMAIL" -> this.response = ExternalChannelUtils.sendEmailCourtesySimpleMessage(clientId, requestId, this.receiver);
            case "PEC" ->
                    this.response = ExternalChannelUtils.sendDigitalNotificationErr(clientId, requestId, attachmentsList, this.receiver);
            default -> throw new IllegalArgumentException();

        };
        this.sRC = String.valueOf(response.getStatusCode());
        log.info("sRC {}", this.sRC);

    }

    @When("try to send a digital message to {string} with no authorization")
    public void tryToSendADigitalMessageToWithNoAuthorization(String receiver) {
        this.receiver = getValueIfTagged(receiver);
        this.requestId = ExternalChannelUtils.generateRandomRequestId(clientId);
        Response response = switch (channel.toUpperCase()) {
            case "SMS" -> this.response = ExternalChannelUtils.sendSmsCourtesySimpleMessage(clientId, requestId, receiver);
            case "EMAIL" -> this.response = ExternalChannelUtils.sendEmailCourtesySimpleMessage(clientId, requestId, receiver);
            case "PEC" ->
                    this.response = ExternalChannelUtils.sendDigitalNotificationErr(clientId, requestId, attachmentsList, receiver);
            default -> throw new IllegalArgumentException();

        };
        this.sRC = String.valueOf(response.getStatusCode());
    }


    //AND
    @And("I prepare the following paper progress status event attachments:")
    public void iPrepareTheFollowingPaperProgressStatusEventAttachments(DataTable dataTable) {
        List<Map<String, String>> attachmentsList = dataTable.asMaps();
        attachmentsList.forEach(map -> {
            ConsolidatoreIngressPaperProgressStatusEventAttachments attachment = new ConsolidatoreIngressPaperProgressStatusEventAttachments()
                    .uri(map.get("attachmentUri"))
                    .sha256(RandomStringUtils.randomAlphanumeric(45))
                    .documentType(map.get("attachmentDocumentType"))
                    .id("id")
                    .date(OffsetDateTime.now())
                    .documentId("documentId");
            this.paperProgressStatusEventAttachments.add(attachment);
        });
    }

    @And("I upload the following attachments:")
    public void uploadAttachments(DataTable dataTable) throws NoSuchAlgorithmException, IOException {
        List<List<String>> rows = dataTable.asLists(String.class);
        var sPNClient = getValueIfTagged("@clientId-delivery");
        var sPNClient_AK = getValueIfTagged("@delivery_api_key");
        for (List<String> row : rows.subList(1, rows.size())) {
            String documentType = getValueIfTagged(row.get(0));
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

            PnAttachment pnAttachment = new PnAttachment();
            pnAttachment.setUri("safestorage://" + sKey);
            pnAttachment.setDate(OffsetDateTime.now());
            pnAttachment.setDocumentType("AAR");
            pnAttachment.setSha256(sha256);
            pnAttachment.setDocumentId(UUID.randomUUID().toString());
            pnAttachment.setId(RandomStringUtils.randomAlphanumeric(10));
            attachmentsList.add(pnAttachment);

            Response uploadResp = CommonUtils.uploadFile(sURL, file, sha256, md5, mimeType, sSecret, Checksum.SHA256);
            assertEquals(200, uploadResp.getStatusCode());
        }
    }

    @And("I upload the following paper progress status event attachments:")
    public void iUploadTheFollowingPaperProgressStatusEventAttachments(DataTable dataTable) {
        var sPNClient = getValueIfTagged("@clientId-delivery");
        var sPNClient_AK = getValueIfTagged("@delivery_api_key");

        List<Map<String, String>> attachmentsList = dataTable.asMaps();
        attachmentsList.forEach(map -> {
            String documentType = getValueIfTagged(map.get("documentType"));
            String fileName = map.get("fileName");
            String mimeType = map.get("mimeType");
            File file = new File(fileName);
            var sha256 = getSHA256(file);
            var md5 = getMD5(file);
            Response getPresignedUrlResp = SafeStorageUtils.getPresignedURLUpload(sPNClient, sPNClient_AK, mimeType, documentType, getSHA256(file), getMD5(file), "SAVED", true, Checksum.SHA256);
            assertEquals(200, getPresignedUrlResp.getStatusCode());
            String sURL = getPresignedUrlResp.then().extract().path("uploadUrl");
            String sKey = getPresignedUrlResp.then().extract().path("key");
            String sSecret = getPresignedUrlResp.then().extract().path("secret");
            Response uploadResp = CommonUtils.uploadFile(sURL, file, sha256, md5, mimeType, sSecret, Checksum.SHA256);
            assertEquals(200, uploadResp.getStatusCode());

            ConsolidatoreIngressPaperProgressStatusEventAttachments attachment = new ConsolidatoreIngressPaperProgressStatusEventAttachments()
                    .uri("safestorage://" + sKey)
                    .sha256(sha256)
                    .documentType(map.get("attachmentDocumentType"))
                    .id("id")
                    .date(OffsetDateTime.now())
                    .documentId("documentId");
            this.paperProgressStatusEventAttachments.add(attachment);
        });
    }

    @And("check if paper progress status requests have been accepted")
    public void checkIfPaperProgressStatusRequestsHaveBeenAccepted() {
        Assertions.assertEquals(200, sendPaperProgressStatusRespCode);
        Assertions.assertEquals("200.00", sendPaperProgressStatusResultCode);
        Assertions.assertEquals("Accepted", sendPaperProgressStatusResultDescription);
        Assertions.assertNull(sendPaperProgressStatusErrorList);
        Assertions.assertTrue(queuePoller.checkMessageAvailability(requestId, new ArrayList<>(statusesToCheck)));
    }

    @And("waiting for scheduling")
    public void waitingForScheduling() {
        LocalDateTime now = LocalDateTime.now();
        int newMinute = (now.getMinute() / 5 + 1) * 5 + 1;
        LocalDateTime nextSchedule;
        if (newMinute < 60) {
            nextSchedule = now.withMinute((now.getMinute() / 5 + 1) * 5).withSecond(0).withNano(0);
        } else {
            nextSchedule = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        }
        Duration duration = Duration.between(now, nextSchedule);
        try {
            Thread.sleep(duration.toMillis() + 5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    //THEN
    @Then("check if the message has been sent")
    public void checkStatusMessage() {
        boolean checked = switch (this.channel.toUpperCase()) {
            case "SMS" ->
                    queuePoller.checkMessageAvailability(requestId, List.of(CourtesyMessageProgressEvent.EventCodeEnum.S003.getValue()));
            case "EMAIL" ->
                    queuePoller.checkMessageAvailability(requestId, List.of(CourtesyMessageProgressEvent.EventCodeEnum.M003.getValue()));
            case "PEC" ->
                    queuePoller.checkMessageAvailability(requestId, List.of(LegalMessageSentDetails.EventCodeEnum.C000.getValue()));
            case "PAPER" -> queuePoller.checkMessageAvailability(requestId, List.of("P000"));
            default ->
                    throw new IllegalArgumentException(String.format("The given channel '%s' is not valid.", this.channel));
        };
        Assertions.assertTrue(checked);
    }


    @Then("I send the following paper progress status requests:")
    public void sendPaperProgressStatusRequests(DataTable dataTable) {
        {
            log.info("requestId {}", this.requestId);
            if (testStartTime == null) {
                testStartTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            }
            List<ConsolidatoreIngressPaperProgressStatusEvent> events = new ArrayList<>();
            List<Map<String, String>> eventsList = dataTable.asMaps();
            eventsList.forEach(map -> {
                ConsolidatoreIngressPaperProgressStatusEvent event = new ConsolidatoreIngressPaperProgressStatusEvent();
                event.setRequestId(requestId);

                String statusCode = map.get("statusCode");
                event.setStatusCode(statusCode);
                statusesToCheck.add(statusCode);

                String iun = map.get("iun");
                event.setIun(iun.equals("@requestId") ? this.requestId : iun);

                event.setStatusDescription("Test description");
                event.setProductType("AR");
                event.setDeliveryFailureCause(getValueOrDefault(map, "deliveryFailureCause", null));

                OffsetDateTime now = OffsetDateTime.now();
                String statusDateTime = map.get("statusDateTime");
                switch (statusDateTime) {
                    case "@testStartTime":
                        event.setStatusDateTime(testStartTime);
                        break;
                    case "@now":
                        event.setStatusDateTime(now);
                        break;
                    default:
                        event.setStatusDateTime(OffsetDateTime.parse(statusDateTime));

                }
                event.setClientRequestTimeStamp(now);

                if (!this.paperProgressStatusEventAttachments.isEmpty())
                    event.setAttachments(this.paperProgressStatusEventAttachments);

                events.add(event);
            });
            Response response = ExternalChannelUtils.sendRequestConsolidatore(this.clientId, this.apiKey, events);
            OperationResultCodeResponse operationResultCodeResponse = response.as(OperationResultCodeResponse.class);
            sendPaperProgressStatusRespCode = response.getStatusCode();
            sendPaperProgressStatusResultCode = operationResultCodeResponse.getResultCode();
            sendPaperProgressStatusResultDescription = operationResultCodeResponse.getResultDescription();
            sendPaperProgressStatusErrorList = operationResultCodeResponse.getErrorList();
        }
    }

    @Then("check if the message has been accepted and has been delivered")
    public void checkIfTheMessageIsAcceptedAndDelivered() {
        Assertions.assertTrue(queuePoller.checkMessageAvailability(requestId, List.of(LegalMessageSentDetails.EventCodeEnum.C001.getValue(), LegalMessageSentDetails.EventCodeEnum.C003.getValue())));
    }

    @Then("check if the message has event code error {string}")
    public void checkIfTheMessageHasEventCodeError(String sRc) {
        Assertions.assertTrue(queuePoller.checkMessageAvailability(requestId, List.of(sRc)));
    }

    @Then("I get {string} status code")
    public void i_get_status_code(String sRC) {
        Assertions.assertEquals(Integer.parseInt(sRC), sendPaperMessageStatusCode);
    }

    @Then("I get {string} result code")
    public void i_get_result_code(String sRC) {
        Assertions.assertEquals(sRC, sendPaperProgressStatusResultCode);
        log.info("Error list: " + sendPaperProgressStatusErrorList);
    }


    @Then("i get response {string}")
    public void iGetResponse(String sRC) {
        Assertions.assertEquals(sRC, this.sRC);
    }

    @Then("i get an error code {string}")
    public void getError(String errorCode) {
        log.info("Error code {}", errorCode);
        Assertions.assertEquals(errorCode, String.valueOf(response.getStatusCode()));
    }



    @AfterAll
    public static void doFinally() throws JMSException {
        queuePoller.close();
    }


}
