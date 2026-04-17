package it.pagopa.pn.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import it.pagopa.pn.configuration.Config;
import it.pagopa.pn.cucumber.dto.pojo.Checksum;
import it.pagopa.pn.cucumber.dto.pojo.PnAttachment;
import it.pagopa.pn.cucumber.poller.PnEcQueuePoller;
import it.pagopa.pn.cucumber.utils.*;
import it.pagopa.pn.ec.rest.v1.api.*;
import it.pagopa.pn.safestorage.generated.openapi.server.v1.dto.FileCreationRequest;
import it.pagopa.pn.safestorage.generated.openapi.server.v1.dto.FileCreationResponse;
import it.pagopa.pn.service.DynamoDbService;
import it.pagopa.pn.service.impl.DynamoDbServiceImpl;
import jakarta.jms.JMSException;
import lombok.CustomLog;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.slf4j.MDC;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static it.pagopa.pn.configuration.TestVariablesConfiguration.getValueIfTagged;
import static it.pagopa.pn.cucumber.utils.CommonUtils.*;
import static it.pagopa.pn.cucumber.utils.ExternalChannelUtils.generateRandomRequestId;
import static it.pagopa.pn.cucumber.utils.LogUtils.MDC_CORR_ID_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;

@CustomLog
public class EcStepDefinitions {

    public static final String NOW_PARAMETER = "@now";
    private String clientId;
    private String apiKey;
    private String requestId;
    private String channel;
    private String receiver;
    private String transformationDocumentType;
    private String paId;
    private String messageText;
    private int sendPaperMessageStatusCode;
    private final List<PnAttachment> attachmentsList = new ArrayList<>();
    private final Set<String> statusesToCheck = new HashSet<>();
    private int sendPaperProgressStatusRespCode = 0;
    private String sendPaperProgressStatusResultCode;
    private String sendPaperProgressStatusResultDescription;
    private List<String> sendPaperProgressStatusErrorList;
    private final List<ConsolidatoreIngressPaperProgressStatusEventAttachmentsInner> paperProgressStatusEventAttachments = new ArrayList<>();
    private static PnEcQueuePoller queuePoller;
    private String sRC;
    private Response response;
    private OffsetDateTime testStartTime;
    private DynamoDbService dynamoDbService = new DynamoDbServiceImpl();
    private String courier;
    private String statusCode;
    private String sentMessageId;


    @BeforeAll
    public static void init() {
        try {
            MDC.clear();
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

    @Given("a {string} authenticated by {string} and {string} to send on")
    public void messageToSendWithAuthentication(String clientId, String apiKey, String channel) {
        this.clientId = getValueIfTagged(clientId);
        this.apiKey = getValueIfTagged(apiKey);
        this.channel = getValueIfTagged(channel);
        log.info("ClientId {}", this.clientId);
        log.info("ApiKey {}", this.apiKey);
        log.debug("Channel {}", this.channel);
    }

    @Given("the ExternalChannel client {string} authenticated by {string}")
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
        this.requestId = ExternalChannelUtils.generateRandomRequestId();
        MDC.put(MDC_CORR_ID_KEY, requestId);
        Response response = ExternalChannelUtils.sendPaperMessage(clientId, requestId, attachmentsList);
        this.sendPaperMessageStatusCode = response.getStatusCode();
    }

    @When("try to send a paper message to {string}")
    public void tryToSendAPaperMessage(String receiver) {
        this.requestId = ExternalChannelUtils.generateRandomRequestId();
        MDC.put(MDC_CORR_ID_KEY, requestId);
        this.receiver = getValueIfTagged(receiver);
        Response response = ExternalChannelUtils.sendPaperMessage(clientId, requestId, attachmentsList);
        this.sendPaperMessageStatusCode = response.getStatusCode();
    }

    @When("try to get client configurations")
    public void tryToGetClientConfigurations() {
        this.response = ExternalChannelUtils.getClient(this.clientId);
        System.out.println("Response: "+response.asString());
        this.sRC = String.valueOf(response.getStatusCode());
    }

    @When("try to get all client configurations")
    public void tryToGetAllClientConfigurations() {
        this.response = ExternalChannelUtils.getClientConfigurations(this.clientId);
        System.out.println("Response: "+this.response.asString());
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
                    ExternalChannelUtils.generateRandomRequestId()));

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
        sendDigitalMessage(receiver, requestId, "Test message");
    }

    @When("try to send digital message to {string} with a requestId")
    public void tryToSendDigitalMessageToReceiverWithARequestId(String receiver) {
        sendDigitalMessage(receiver, generateRandomRequestId(), "Test message");
    }

    @When("try to send a digital message to {string} with same requestId")
    public void tryToSendADigitalMessageToWithSameRequestId(String receiver) {
        sendDigitalMessage(receiver, this.requestId, "Message with same requestId");
    }

    @When("try to send a digital message to {string}")
    public void presaInCarico(String receiver) {
        sendDigitalMessage(receiver, ExternalChannelUtils.generateRandomRequestId(), "Test message");
    }


    @When("I try to PATCH request metadata with a messageId")
    public void patchRequestMetadataByMessageId() {
        MessageIdRequestMetadataDto body = new MessageIdRequestMetadataDto();
        this.sentMessageId = ExternalChannelUtils.generateRandomMessageId();
        //this.sentMessageId=getValueIfTagged(messageId);
        System.out.println("MESSAGE ID PATCH: "+this.sentMessageId);
        System.out.println("Request ID PATCH: "+ ExternalChannelUtils.concatRequestId(clientId,requestId));
        body.setMessageId(sentMessageId);
        requestId = ExternalChannelUtils.concatRequestId(clientId,this.requestId);
        response = ExternalChannelUtils.setRequestMetadataMessageId(clientId, requestId, body);
    }

    @When("I try to GET request metadata by messageId")
    public void getRequestMetadataByMessageId() {
        System.out.println("MESSAGE ID GET: "+this.sentMessageId);
        response = ExternalChannelUtils.getRequestMetadataByMessageId(this.sentMessageId);
    }

    //AND
    @And("I prepare the following paper progress status event attachments:")
    public void iPrepareTheFollowingPaperProgressStatusEventAttachments(DataTable dataTable) {
        List<Map<String, String>> attachmentsList = dataTable.asMaps();
        attachmentsList.forEach(map -> {
            ConsolidatoreIngressPaperProgressStatusEventAttachmentsInner attachment = new ConsolidatoreIngressPaperProgressStatusEventAttachmentsInner()
                    .uri(map.get("attachmentUri"))
                    .sha256(RandomStringUtils.randomAlphanumeric(45))
                    .documentType(map.get("attachmentDocumentType"))
                    .id("id")
                    .date(OffsetDateTime.now())
                    .documentId("documentId");
            this.paperProgressStatusEventAttachments.add(attachment);
        });
    }

    @And("{string} authenticated by {string} uploads the following attachments:")
    public void uploadAttachments(String clientId, String apiKey, DataTable dataTable) {
        String ssClientId = getValueIfTagged(clientId);
        String ssApiKey = getValueIfTagged(apiKey);
        List<List<String>> rows = dataTable.asLists(String.class);
        for (List<String> row : rows.subList(1, rows.size())) {
            String documentType = getValueIfTagged(row.get(0));
            String fileName = row.get(1);
            String mimeType = row.get(2);

            File file = new File(fileName);
            var sha256 = getSHA256(file);
            var md5 = getMD5(file);
            FileCreationRequest fileCreationRequest = new FileCreationRequest().status("SAVED").contentType(mimeType).documentType(documentType);
            Response getPresignedUrlResp = SafeStorageUtils.getPresignedURLUpload(ssClientId, ssApiKey, fileCreationRequest, getSHA256(file), getMD5(file), true, Checksum.SHA256, true);
            assertEquals(200, getPresignedUrlResp.getStatusCode());
            FileCreationResponse fileCreationResponse = getPresignedUrlResp.as(FileCreationResponse.class);
            String sURL = fileCreationResponse.getUploadUrl();
            String sKey = fileCreationResponse.getKey();
            String sSecret = fileCreationResponse.getSecret();
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

    @And("{string} authenticated by {string} uploads the following paper progress status event attachments:")
    public void iUploadTheFollowingPaperProgressStatusEventAttachments(String clientId, String apiKey, DataTable dataTable) {
        String ssClientId = getValueIfTagged(clientId);
        String ssApiKey = getValueIfTagged(apiKey);
        List<Map<String, String>> attachmentsList = dataTable.asMaps();
        attachmentsList.forEach(map -> {
            String documentType = getValueIfTagged(map.get("documentType"));
            String fileName = map.get("fileName");
            String mimeType = map.get("mimeType");
            File file = new File(fileName);
            var sha256 = getSHA256(file);
            var md5 = getMD5(file);
            FileCreationRequest fileCreationRequest = new FileCreationRequest().status("SAVED").contentType(mimeType).documentType(documentType);
            Response getPresignedUrlResp = SafeStorageUtils.getPresignedURLUpload(ssClientId, ssApiKey, fileCreationRequest, getSHA256(file), getMD5(file), true, Checksum.SHA256, true);
            assertEquals(200, getPresignedUrlResp.getStatusCode());
            FileCreationResponse fileCreationResponse = getPresignedUrlResp.as(FileCreationResponse.class);
            String sURL = fileCreationResponse.getUploadUrl();
            String sKey = fileCreationResponse.getKey();
            String sSecret = fileCreationResponse.getSecret();
            Response uploadResp = CommonUtils.uploadFile(sURL, file, sha256, md5, mimeType, sSecret, Checksum.SHA256);
            assertEquals(200, uploadResp.getStatusCode());

            ConsolidatoreIngressPaperProgressStatusEventAttachmentsInner attachment = new ConsolidatoreIngressPaperProgressStatusEventAttachmentsInner()
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

    @And("try to send a paper message to {string} with {string} as documentType")
    public void tryToSendAPaperMessageToWithDocumentType(String receiver, String transformationDocumentType) {
        tryToSendAPaperMessageToWithDocumentType(receiver, transformationDocumentType, "@null");
    }

    @And("try to send a paper message to {string} with {string} as documentType and {string} as PaId")
    public void tryToSendAPaperMessageToWithDocumentType(String receiver, String transformationDocumentType, String paId) {
        log.info("nel  send -  receiver: {}, transformationDocumentType: {}, paId: {} ",receiver,transformationDocumentType,paId);
        this.requestId = ExternalChannelUtils.generateRandomRequestId();
        MDC.put(MDC_CORR_ID_KEY, requestId);
        this.receiver = getValueIfTagged(receiver);
        this.transformationDocumentType = getValueIfTagged(transformationDocumentType);
        this.paId=getValueIfTagged(paId);
        Response response = ExternalChannelUtils.sendPaperMessageWithDocumentTransformationType(clientId, requestId, attachmentsList, this.transformationDocumentType, this.paId);
        this.sendPaperMessageStatusCode = response.getStatusCode();
    }

    @When("try to send a paper message to {string} with {string} and {string}")
    public void tryToSendAPaperMessageToWithAnd(String receiver, String requestPaId, String applyRasterization) {
        this.requestId = ExternalChannelUtils.generateRandomRequestId();
        this.receiver = getValueIfTagged(receiver);
        response = ExternalChannelUtils.sendPaperMessageRasterFlag(clientId, requestId, requestPaId, applyRasterization, attachmentsList);
        this.sendPaperMessageStatusCode = response.getStatusCode();
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
            case "SERCQ" ->
                    queuePoller.checkMessageAvailability(requestId, List.of(LegalMessageSentDetails.EventCodeEnum.Q003.getValue()));
            case "PAPER" -> queuePoller.checkMessageAvailability(requestId, List.of("P000"));
            default ->
                    throw new IllegalArgumentException(String.format("The given channel '%s' is not valid.", this.channel));
        };
        Assertions.assertTrue(checked);
    }

    @Then("check SES event {string} is {string}")
    public void checkSesEvent(String expectedEvent, String expectedResult ) {
        boolean checked = queuePoller.checkMessageAvailability(requestId, List.of(expectedEvent));
        boolean expected = Boolean.parseBoolean(expectedResult);
        Assertions.assertEquals(expected, checked);
    }

    @Then("check if the message has status {string}")
    public void checkStatusMessage(String status) {
        boolean checked = queuePoller.checkMessageAvailability(requestId,List.of(status));
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

                String iun = getValueOrDefault(map, "iun", null);
                event.setIun(iun != null && iun.equals("@requestId") ? this.requestId : iun);

                event.setStatusDescription("Test description");
                event.setProductType(getValueOrDefault(map, "productType", "AR"));
                event.setDeliveryFailureCause(getValueOrDefault(map, "deliveryFailureCause", null));

                OffsetDateTime now = OffsetDateTime.now();
                String statusDateTime = map.get("statusDateTime");
                switch (statusDateTime) {
                    case "@testStartTime" -> event.setStatusDateTime(testStartTime);
                    case NOW_PARAMETER -> event.setStatusDateTime(now);
                    default -> event.setStatusDateTime(OffsetDateTime.parse(statusDateTime));
                }
                String clientRequestTimestampStr = map.get("clientRequestTimestamp");
                OffsetDateTime clientRequestTimestamp = clientRequestTimestampStr != null && !clientRequestTimestampStr.equals(NOW_PARAMETER) ?
                        OffsetDateTime.parse(clientRequestTimestampStr) :
                        now;
                event.setClientRequestTimeStamp(clientRequestTimestamp);

                if (!this.paperProgressStatusEventAttachments.isEmpty())
                    event.setAttachments(this.paperProgressStatusEventAttachments);
                //event.setCourier("recapitista");
                String courier = getValueOrDefault(map, "courier", null);
                event.setCourier(courier);

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
        log.debug("Error list: " + sendPaperProgressStatusErrorList);
    }

    @Then("i get response {string}")
    public void iGetResponse(String sRC) {
        Assertions.assertEquals(sRC, this.sRC);
    }

    @Then("i get an error code {string}")
    public void getError(String errorCode) {
        log.debug("Error code {}", errorCode);
        log.debug("Response : {}", response.asString());
        Assertions.assertEquals(errorCode, String.valueOf(response.getStatusCode()));
    }

    @Then("I verify the record in pn-EcScartiConsolidatore")
    public void i_verify_the_record_in_pn_ecScartiConsolidatore(){
        QueryResponse response = dynamoDbService.queryByRequestId(System.getProperty("pn.ec.scarti-consolidatore.table.name"),requestId);
        Assertions.assertTrue(response.hasItems());

        Optional<Map<String, AttributeValue>> matchingItem = response.items().stream()
                .filter(item -> item.get("requestId").s().equals(requestId))
                .findFirst();

        Assertions.assertTrue(matchingItem.isPresent());
    }

    @Then("I get {string} courier and I get {string} statusCode:")
    public void iGetCourier(String courier, String statusCode) throws Exception{
        this.courier = getValueIfTagged(courier);
        this.statusCode = getValueIfTagged(statusCode);
        log.info("Courier {}", this.courier, " - statusCode {} ", this.statusCode);
        boolean result = false;
        if(this.courier.equals("null")) {
            Assertions.assertNull(this.courier);
        } else {
            String clientId = "pn-cons-000~" + requestId;
            log.info("clientId {}", clientId);
            QueryResponse response = dynamoDbService.queryByRequestId(System.getProperty("pn.ec.richieste-metadati.table.name"),clientId);
            Optional<Map<String, AttributeValue>> matchingItem = response.items().stream()
                    .filter(item -> item.get("requestId").s().equals(clientId))
                    .findFirst();

            if(matchingItem != null && !matchingItem.isEmpty()){
                Map<String, AttributeValue> itemsMap = matchingItem.get(); //record
                log.info("Record trovato: {} ", itemsMap);
                List<AttributeValue> itemsValue = itemsMap.get("eventsList").l(); //eventList
                if(itemsValue != null)
                    log.info("itemsValue size: {} ", itemsValue.size());
                for (AttributeValue eventValue : itemsValue) {
                    if (eventValue != null && eventValue.m() != null && eventValue.m().containsKey("paperProgrStatus") ){
                        AttributeValue paperProgrStatusMap = eventValue.m().get("paperProgrStatus");
                        if(paperProgrStatusMap != null && !paperProgrStatusMap.m().isEmpty()){
                            log.info("paperProgrStatusMap trovato: {} ", paperProgrStatusMap);
                            if ( paperProgrStatusMap.m().containsKey("statusCode") && paperProgrStatusMap.m().get("statusCode").s() != null &&
                                    paperProgrStatusMap.m().get("statusCode").s().equals(statusCode)){
                                String paperProgrStatusCourier = paperProgrStatusMap.m().get("courier").s();
                                log.info("paperProgrStatusCourier trovato: {} ", paperProgrStatusCourier);
                                result = true;
                                //Assertions.assertEquals(paperProgrStatusCourier, this.courier);
                            }
                        }
                    }
                }
            }else
                throw new Exception("Record non trovato");

            Assertions.assertTrue(result);
        }
    }

    @AfterAll
    public static void doFinally() throws JMSException {
        if (queuePoller != null)
            queuePoller.close();
    }

    private void sendDigitalMessage(String receiver, String requestId, String messageText) {
        this.requestId = getValueIfTagged(requestId);
        MDC.put(MDC_CORR_ID_KEY, this.requestId);
        this.clientId = getValueIfTagged(clientId);
        this.receiver = getValueIfTagged(receiver);
        this.messageText = messageText;
        log.info("receiver address {}", this.receiver);
        //switch sul canale
        this.response = switch (this.channel) {
            case "SMS" -> ExternalChannelUtils.sendSmsCourtesySimpleMessage(clientId, requestId, this.receiver, this.messageText);
            case "EMAIL" -> ExternalChannelUtils.sendEmailCourtesySimpleMessage(clientId, requestId, attachmentsList, this.receiver);
            case "PEC", "SERCQ" -> ExternalChannelUtils.sendDigitalNotification(clientId, requestId, attachmentsList, this.receiver, this.channel, this.messageText);
            default -> throw new IllegalArgumentException();
        };
        log.debug("RESPONSE : {}", response.getStatusCode());
        this.sRC = String.valueOf(this.response.getStatusCode());
    }

    @And("{string} authenticated by {string} uploads the following attachments to reject:")
    public void uploadAttachmentsToReject(String clientId, String apiKey, DataTable dataTable) throws IOException, NoSuchAlgorithmException {

        String ssClientId = getValueIfTagged(clientId);
        String ssApiKey = getValueIfTagged(apiKey);

        List<List<String>> rows = dataTable.asLists(String.class);

        for (List<String> row : rows.subList(1, rows.size())) {
            String documentType = getValueIfTagged(row.get(0));
            String mimeType = row.get(1);

            String eicar = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*";

            byte[] fileBytes = eicar.getBytes(StandardCharsets.US_ASCII);

            String sha256 = getSHA256Byte(fileBytes);
            String md5 = getMD5Byte(fileBytes);

            FileCreationRequest fileCreationRequest = new FileCreationRequest()
                    .status("SAVED")
                    .contentType(mimeType)
                    .documentType(documentType);

            Response presignedResp = SafeStorageUtils.getPresignedURLUpload(
                    ssClientId, ssApiKey, fileCreationRequest,
                    sha256, md5, true, Checksum.SHA256, true
            );
            assertEquals(200, presignedResp.getStatusCode());

            FileCreationResponse fileCreationResponse = presignedResp.as(FileCreationResponse.class);
            String uploadUrl = fileCreationResponse.getUploadUrl();
            String sKey = fileCreationResponse.getKey();
            String secret = fileCreationResponse.getSecret();

            PnAttachment pnAttachment = new PnAttachment();
            pnAttachment.setUri("safestorage://" + sKey);
            pnAttachment.setDate(OffsetDateTime.now());
            pnAttachment.setDocumentType(documentType);
            pnAttachment.setSha256(sha256);
            pnAttachment.setDocumentId(UUID.randomUUID().toString());
            pnAttachment.setId(RandomStringUtils.randomAlphanumeric(10));
            attachmentsList.add(pnAttachment);
            Response uploadResp = CommonUtils.uploadFileByte(uploadUrl, fileBytes, sha256, md5, mimeType, secret, Checksum.SHA256);
            assertEquals(200, uploadResp.getStatusCode());
        }
    }

    public static String getSHA256Byte(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        return Base64.getEncoder().encodeToString(hash);
    }

    public static String getMD5Byte(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hash = digest.digest(bytes);
        return Base64.getEncoder().encodeToString(hash);
    }


}
