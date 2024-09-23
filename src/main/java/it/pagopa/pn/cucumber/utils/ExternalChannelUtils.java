package it.pagopa.pn.cucumber.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import it.pagopa.pn.cucumber.RequestTemplate;
import it.pagopa.pn.cucumber.dto.ClientConfigurationInternalDto;
import it.pagopa.pn.cucumber.dto.pojo.PnAttachment;
import it.pagopa.pn.ec.rest.v1.api.*;
import it.pagopa.pn.exception.MessageIdException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;

import java.math.BigDecimal;
import java.util.List;

import static it.pagopa.pn.cucumber.utils.LogUtils.MDC_CORR_ID_KEY;

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

    /*
    un metodo che definisce una chiamata API per ogni channel
     */
    //SMS
    public static Response sendSmsCourtesySimpleMessage(String clientId, String requestId, String receiver) {
        log.info("requestId {}", requestId);
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        DigitalCourtesySmsRequest digitalCourtesySmsRequest = createSmsRequest(requestId, receiver);
    log.info(digitalCourtesySmsRequest.getRequestId());
        oReq.body(digitalCourtesySmsRequest);
         Response response = CommonUtils.myPut(oReq, RequestEndpoint.SMS_ENDPOINT);
         log.info(oReq.get().asString());

        return response;
    }
    public static Response sendSmsCourtesySimpleMessageErr(String clientId, String requestId, String receiver) {
        log.info("requestId {}", requestId);
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        DigitalCourtesySmsRequest digitalCourtesySmsRequest = createSmsRequestErr(requestId, receiver);
    log.info(digitalCourtesySmsRequest.getRequestId());
        oReq.body(digitalCourtesySmsRequest);
         Response response = CommonUtils.myPut(oReq, RequestEndpoint.SMS_ENDPOINT);
         log.info(oReq.get().asString());

        return response;
    }

    // EMAIL
    public static Response sendEmailCourtesySimpleMessage(String clientId, String requestId, String receiver) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        DigitalCourtesyMailRequest digitalCourtesyMailRequest = createMailRequest(requestId, receiver);
        oReq.body(digitalCourtesyMailRequest);

        return CommonUtils.myPut(oReq,RequestEndpoint.EMAIL_ENDPOINT);
    }


    //PEC
    public static Response sendDigitalNotification(String clientId, String requestId, List<PnAttachment> attachments, String receiver) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        DigitalNotificationRequest digitalNotificationRequest = createDigitalNotificationRequest(requestId, receiver);

        List<String> attachmentsUri = attachments.stream().map(PnAttachment::getUri).toList();
        digitalNotificationRequest.setAttachmentUrls(attachmentsUri);

        oReq.body(digitalNotificationRequest);
        return CommonUtils.myPut(oReq,RequestEndpoint.PEC_ENDPOINT);
    }
    public static Response sendDigitalNotificationErr(String clientId, String requestId, List<PnAttachment> attachments, String receiver) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        DigitalNotificationRequest digitalNotificationRequest = createDigitalNotificationRequestErr(requestId, receiver);
        List<String> attachmentsUri = attachments.stream().map(PnAttachment::getUri).toList();
        digitalNotificationRequest.setAttachmentUrls(attachmentsUri);

        oReq.body(digitalNotificationRequest);
        return CommonUtils.myPut(oReq,RequestEndpoint.PEC_ENDPOINT);
    }

    //CARTACEO
    public static Response sendPaperMessage(String clientId, String requestId, List<PnAttachment> attachments) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        PaperEngageRequest paperEngageRequest = createPaperEngageRequest(requestId);
        List<PaperEngageRequestAttachments> paperEngageRequestAttachmentsList = attachments.stream().map(attachment -> {
            PaperEngageRequestAttachments paperEngageRequestAttachments = new PaperEngageRequestAttachments();
            paperEngageRequestAttachments.setDocumentType(attachment.getDocumentType());
            paperEngageRequestAttachments.setUri(attachment.getUri());
            paperEngageRequestAttachments.setSha256(attachment.getSha256());
            paperEngageRequestAttachments.setOrder(BigDecimal.ZERO);
            return paperEngageRequestAttachments;
        }).toList();
        paperEngageRequest.setAttachments(paperEngageRequestAttachmentsList);
        oReq.body(paperEngageRequest);
        return CommonUtils.myPut(oReq, RequestEndpoint.CARTACEO_ENDPOINT);
    }

    //API Consolidatore
    public static Response sendRequestConsolidatore(String clientId, String apiKey, List<ConsolidatoreIngressPaperProgressStatusEvent> events) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_SERVICE_ID, clientId)
                .header(X_API_KEY, apiKey);
        oReq.body(events);
        return CommonUtils.myPut(oReq, RequestEndpoint.CONSOLIDATORE_ENDPOINT);
    }

    //CLIENT
    public static Response getClientConfigurations(String clientId) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId);
        ClientConfigurationDto clientConfigurationDto = createClientConfigurationRequest();
        oReq.body(clientConfigurationDto);
        return CommonUtils.myGet(oReq, RequestEndpoint.GET_CONFIGURATIONS_ENDPOINT);
    }

    public static Response getClient(String clientId){
        RequestSpecification oReq = stdReq()
                .pathParam(X_PAGOPA_EXTCH_CX_ID, clientId);
        return CommonUtils.myGet(oReq, RequestEndpoint.GET_CLIENT_ENDPOINT);
    }

    //GET REQUEST
    public static Response getRequest(String clientId, String requestId) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.GET_REQUEST_ENDPOINT);
    }

    public static Response getRequestByMessageId(String messageId) {
        RequestSpecification oReq = stdReq()
                .pathParam("messageId", messageId);
        return CommonUtils.myGet(oReq, RequestEndpoint.GET_REQUEST_MESSAGE_ID_ENDPOINT);
    }


    //GET PEC
    public static Response getPecByRequestId(String clientId, String requestId){
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.PEC_ENDPOINT);
    }

    //GET EMAIL
    public static Response getEmailByRequestId(String clientId, String requestId){
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.EMAIL_ENDPOINT);
    }

    //GET SMS
    public static Response getSmsByRequestId(String clientId, String requestId){
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.SMS_ENDPOINT);
    }

    //GET PAPER
    public static Response getPaperByRequestId(String clientId, String requestId){
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_EXTCH_CX_ID, clientId)
                .pathParam(REQUEST_IDX, requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.CARTACEO_ENDPOINT);
    }

    //GET ATTACHMENTS
    public static Response getAttachmentsByFileKey( String fileKey, String clientId, String apiKey){

        RequestSpecification oReq = stdReq()
                .pathParam(FILE_KEY, fileKey)
                .header(X_PAGOPA_EXTCH_SERVICE_ID, clientId)
                .header(X_API_KEY, apiKey);
        return CommonUtils.myGet(oReq, RequestEndpoint.GET_ATTACHMENT);
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

}

