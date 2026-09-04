package it.pagopa.pn.cucumber.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import it.pagopa.pn.cucumber.RequestTemplate;
import it.pagopa.pn.cucumber.dto.pojo.PnAttachment;
import it.pagopa.pn.ec.rest.v1.api.*;
import it.pagopa.pn.exception.MessageIdException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.Base64Utils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.cucumber.utils.RequestEndpoint.GET_REQUEST_METADATA_BY_MESSAGEID_ENDPOINT;
import static it.pagopa.pn.cucumber.utils.RequestEndpoint.SET_PATCH_REQUEST_METADATA_MESSAGEID_ENDPOINT;

@Slf4j
public class ExternalChannelUtils extends RequestTemplate {
    private static final String SEPARATORE = "~";
    public static final String DOMAIN = "@pagopa.it";
    private static final String BASE_REQUEST_ID = "PnEcMsCucumberTest";
    private static final int TARGET_STRING_LENGTH = 30;
    public static final String X_PAGOPA_EXTCH_CX_ID = "x-pagopa-extch-cx-id";
    public static final String REQUEST_IDX = "requestIdx";
    public static final String X_API_KEY = "x-api-key";
    public static final String X_PAGOPA_EXTCH_SERVICE_ID = "x-pagopa-extch-service-id";
    public static final String FILE_KEY = "fileKey";


    protected static RequestSpecification stdReq() {
        return RestAssured.given()
                .header("Accept", "application/json")
                .header("Content-type", "application/json")
                .header("x-amz-trace-id", java.util.UUID.randomUUID().toString());
    }

    //SMS
    public static Response sendSmsCourtesySimpleMessage(String clientId, String requestId, String receiver, String messageText) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        DigitalCourtesySmsRequest digitalCourtesySmsRequest = createSmsRequest(requestId, receiver, messageText);
        oReq.body(digitalCourtesySmsRequest);
        Response response = CommonUtils.myPut(oReq, RequestEndpoint.SMS_ENDPOINT, CommonUtils.PN_EC);
        log.debug("SMS request {} sent for client {}: httpStatus={}", requestId, clientId, response.getStatusCode());
        return response;
    }

    // EMAIL
    public static Response sendEmailCourtesySimpleMessage(String clientId, String requestId, List<PnAttachment> attachmentList, String receiver) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        DigitalCourtesyMailRequest digitalCourtesyMailRequest = createMailRequest(requestId, receiver);
        List<String> attachmentsUri = attachmentList.stream().map(PnAttachment::getUri).toList();
        digitalCourtesyMailRequest.setAttachmentUrls(attachmentsUri);
        oReq.body(digitalCourtesyMailRequest);

        return CommonUtils.myPut(oReq, RequestEndpoint.EMAIL_ENDPOINT, CommonUtils.PN_EC);
    }

    //PEC
    public static Response sendDigitalNotification(String clientId, String requestId, List<PnAttachment> attachments, String receiver, String channel, String messageText) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        DigitalNotificationRequest digitalNotificationRequest = createDigitalNotificationRequest(requestId, receiver, channel, messageText);

        List<String> attachmentsUri = attachments.stream().map(PnAttachment::getUri).toList();
        digitalNotificationRequest.setAttachmentUrls(attachmentsUri);

        oReq.body(digitalNotificationRequest);
        return CommonUtils.myPut(oReq, RequestEndpoint.PEC_ENDPOINT, CommonUtils.PN_EC);
    }

    //CARTACEO
    public static Response sendPaperMessage(String clientId, String requestId, List<PnAttachment> attachments) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        PaperEngageRequest paperEngageRequest = createPaperEngageRequest(requestId);
        List<PaperEngageRequestAttachmentsInner> paperEngageRequestAttachmentsList = attachments.stream().map(attachment -> {
            PaperEngageRequestAttachmentsInner paperEngageRequestAttachments = new PaperEngageRequestAttachmentsInner();
            paperEngageRequestAttachments.setDocumentType(attachment.getDocumentType());
            paperEngageRequestAttachments.setUri(attachment.getUri());
            paperEngageRequestAttachments.setSha256(attachment.getSha256());
            paperEngageRequestAttachments.setOrder(BigDecimal.ZERO);
            return paperEngageRequestAttachments;
        }).toList();
        paperEngageRequest.setAttachments(paperEngageRequestAttachmentsList);
        oReq.body(paperEngageRequest);
        return CommonUtils.myPut(oReq, RequestEndpoint.CARTACEO_ENDPOINT, CommonUtils.PN_EC);
    }

    public static Response sendPaperMessageWithDifferentAddress(String clientId, String requestId, List<PnAttachment> attachments, String receiverAddress) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        PaperEngageRequest paperEngageRequest = createPaperEngageRequest(requestId, receiverAddress);
        List<PaperEngageRequestAttachmentsInner> paperEngageRequestAttachmentsList = attachments.stream().map(attachment -> {
            PaperEngageRequestAttachmentsInner paperEngageRequestAttachments = new PaperEngageRequestAttachmentsInner();
            paperEngageRequestAttachments.setDocumentType(attachment.getDocumentType());
            paperEngageRequestAttachments.setUri(attachment.getUri());
            paperEngageRequestAttachments.setSha256(attachment.getSha256());
            paperEngageRequestAttachments.setOrder(BigDecimal.ZERO);
            return paperEngageRequestAttachments;
        }).toList();
        paperEngageRequest.setAttachments(paperEngageRequestAttachmentsList);
        oReq.body(paperEngageRequest);
        return CommonUtils.myPut(oReq, RequestEndpoint.CARTACEO_ENDPOINT, CommonUtils.PN_EC);
    }

    public static Response sendPaperMessageWithDocumentTransformationTypeAndRasterizationFlag(String clientId, String requestId, List<PnAttachment> attachments, String transformationDocumentType,boolean applyRasterizationFlag) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        PaperEngageRequest paperEngageRequest = createPaperEngageRequest(requestId);
        paperEngageRequest.setTransformationDocumentType(transformationDocumentType);
        paperEngageRequest.applyRasterization(applyRasterizationFlag);
        List<PaperEngageRequestAttachmentsInner> paperEngageRequestAttachmentsList = attachments.stream().map(attachment -> {
            PaperEngageRequestAttachmentsInner paperEngageRequestAttachments = new PaperEngageRequestAttachmentsInner();
            paperEngageRequestAttachments.setDocumentType(attachment.getDocumentType());
            paperEngageRequestAttachments.setUri(attachment.getUri());
            paperEngageRequestAttachments.setSha256(attachment.getSha256());
            paperEngageRequestAttachments.setOrder(BigDecimal.ZERO);
            return paperEngageRequestAttachments;
        }).toList();
        paperEngageRequest.setAttachments(paperEngageRequestAttachmentsList);
        oReq.body(paperEngageRequest);
        return CommonUtils.myPut(oReq, RequestEndpoint.CARTACEO_ENDPOINT, CommonUtils.PN_EC);
    }

    public static Response sendPaperMessageWithDocumentTransformationType(String clientId, String requestId, List<PnAttachment> attachments, String transformationDocumentType, String paId) {
        log.debug("Sending paper message {} with transformationDocumentType {}", requestId, transformationDocumentType);
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        PaperEngageRequest paperEngageRequest = createPaperEngageRequest(requestId);
        paperEngageRequest.setRequestPaId(paId);
        if (!transformationDocumentType.isBlank()){
            paperEngageRequest.setTransformationDocumentType(transformationDocumentType);
        }
        List<PaperEngageRequestAttachmentsInner> paperEngageRequestAttachmentsList = attachments.stream().map(attachment -> {
            PaperEngageRequestAttachmentsInner paperEngageRequestAttachments = new PaperEngageRequestAttachmentsInner();
            paperEngageRequestAttachments.setDocumentType(attachment.getDocumentType());
            paperEngageRequestAttachments.setUri(attachment.getUri());
            paperEngageRequestAttachments.setSha256(attachment.getSha256());
            paperEngageRequestAttachments.setOrder(BigDecimal.ZERO);
            return paperEngageRequestAttachments;
        }).toList();
        paperEngageRequest.setAttachments(paperEngageRequestAttachmentsList);
        oReq.body(paperEngageRequest);
        return CommonUtils.myPut(oReq, RequestEndpoint.CARTACEO_ENDPOINT, CommonUtils.PN_EC);
    }

    public static Response sendPaperMessageRasterFlag(String clientId, String requestId, String requestPaId, String applyRasterization, List<PnAttachment> attachments) {
        log.debug("Sending paper message {} with applyRasterization {}", requestId, applyRasterization);
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        PaperEngageRequest paperEngageRequest = createPaperEngageRequest(requestId);
        List<PaperEngageRequestAttachmentsInner> paperEngageRequestAttachmentsList = attachments.stream().map(attachment -> {
            PaperEngageRequestAttachmentsInner paperEngageRequestAttachments = new PaperEngageRequestAttachmentsInner();
            paperEngageRequestAttachments.setDocumentType(attachment.getDocumentType());
            paperEngageRequestAttachments.setUri(attachment.getUri());
            paperEngageRequestAttachments.setSha256(attachment.getSha256());
            paperEngageRequestAttachments.setOrder(BigDecimal.ZERO);
            return paperEngageRequestAttachments;
        }).toList();
        paperEngageRequest.setAttachments(paperEngageRequestAttachmentsList);
        paperEngageRequest.setRequestPaId(requestPaId);
        Boolean applyRasterizationFlag = null;
        if (applyRasterization != null && !applyRasterization.isEmpty()) {
            applyRasterizationFlag = Boolean.valueOf(applyRasterization);
        }
        paperEngageRequest.setApplyRasterization(applyRasterizationFlag);

        oReq.body(paperEngageRequest);
        return CommonUtils.myPut(oReq, RequestEndpoint.CARTACEO_ENDPOINT, CommonUtils.PN_EC);
    }

    //API Consolidatore
    public static Response sendRequestConsolidatore(String clientId, String apiKey, List<ConsolidatoreIngressPaperProgressStatusEvent> events) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_SERVICE_ID, clientId)
                .header(X_API_KEY, apiKey);
        oReq.body(events);
        return CommonUtils.myPut(oReq, RequestEndpoint.CONSOLIDATORE_ENDPOINT, CommonUtils.PN_EC);
    }

    //CLIENT
    public static Response getClientConfigurations(String clientId) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId);
        ClientConfigurationDto clientConfigurationDto = createClientConfigurationRequest();
        oReq.body(clientConfigurationDto);
        return CommonUtils.myGet(oReq, RequestEndpoint.GET_CONFIGURATIONS_ENDPOINT, CommonUtils.PN_EC);
    }

    public static Response getClient(String clientId) {
        RequestSpecification oReq = stdReq()
                .pathParam(X_PAGOPA_EXTCH_CX_ID, clientId);
        return CommonUtils.myGet(oReq, RequestEndpoint.GET_CLIENT_ENDPOINT, CommonUtils.PN_EC);
    }

    //GET REQUEST
    public static Response getRequest(String clientId, String requestId) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.GET_REQUEST_ENDPOINT, CommonUtils.PN_EC);
    }

    public static Response getRequestByMessageId(String messageId) {
        RequestSpecification oReq = stdReq()
                .pathParam("messageId", messageId);
        return CommonUtils.myGet(oReq, RequestEndpoint.GET_REQUEST_MESSAGE_ID_ENDPOINT, CommonUtils.PN_EC);
    }

    // GET request metadata by messageId
    public static Response getRequestMetadataByMessageId(String messageId) {
        RequestSpecification oReq = stdReq()
                .pathParam("messageId", messageId);
        return CommonUtils.myGet(oReq, GET_REQUEST_METADATA_BY_MESSAGEID_ENDPOINT, CommonUtils.PN_EC);
    }

    // PATCH set request metadata messageId
    public static Response setRequestMetadataMessageId(String clientId, String requestIdx, MessageIdRequestMetadataDto messageIdRequestMetadataDto) {

        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestIdx)
                .body(messageIdRequestMetadataDto);

        return CommonUtils.myPatch(oReq, SET_PATCH_REQUEST_METADATA_MESSAGEID_ENDPOINT, CommonUtils.PN_EC);
    }

    //GET PEC
    public static Response getPecByRequestId(String clientId, String requestId) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.PEC_ENDPOINT, CommonUtils.PN_EC);
    }

    //GET EMAIL
    public static Response getEmailByRequestId(String clientId, String requestId) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.EMAIL_ENDPOINT, CommonUtils.PN_EC);
    }

    //GET SMS
    public static Response getSmsByRequestId(String clientId, String requestId) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.SMS_ENDPOINT, CommonUtils.PN_EC);
    }

    //GET PAPER
    public static Response getPaperByRequestId(String clientId, String requestId) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.CARTACEO_ENDPOINT, CommonUtils.PN_EC);
    }

    //GET ATTACHMENTS
    public static Response getAttachmentsByFileKey(String fileKey, String clientId, String apiKey) {

        RequestSpecification oReq = stdReq()
                .pathParam(FILE_KEY, fileKey)
                .header(X_PAGOPA_EXTCH_SERVICE_ID, clientId)
                .header(X_API_KEY, apiKey);
        return CommonUtils.myGet(oReq, RequestEndpoint.GET_ATTACHMENT, CommonUtils.PN_EC);
    }

    public static String generateRandomRequestId() {
        String randomAlphanumeric = RandomStringUtils.randomAlphanumeric(TARGET_STRING_LENGTH);
        return String.format("%s-%s", BASE_REQUEST_ID, randomAlphanumeric);
    }

    public static String encodeMessageId(String clientId, String requestId) {
        try {
            return String.format("%s%s%s%s",
                    Base64Utils.encodeToString(clientId.getBytes()),
                    SEPARATORE,
                    Base64Utils.encodeToString(requestId.getBytes()),
                    DOMAIN);
        } catch (Exception e) {
            throw new MessageIdException.EncodeMessageIdException();
        }

    }
    public static String generateRandomMessageId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String concatRequestId(String clientId, String requestId) {
        return (String.format("%s%s%s", clientId, SEPARATORE, requestId));
    }

}

