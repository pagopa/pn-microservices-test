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
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class ExternalChannelUtils extends RequestTemplate {
    private static final String SEPARATORE = "~";
    public static final String DOMAIN = "@pagopa.it";

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
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        DigitalCourtesySmsRequest digitalCourtesySmsRequest = createSmsRequest(requestId, receiver);
    log.info(digitalCourtesySmsRequest.getRequestId());
        oReq.body(digitalCourtesySmsRequest);
         Response response = CommonUtils.myPut(oReq, RequestEndpoint.SMS_ENDPOINT);
         log.info(oReq.get().asString());

        return response;
    }

    // EMAIL
    public static Response sendEmailCourtesySimpleMessage(String clientId, String requestId, String receiver) {
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        DigitalCourtesyMailRequest digitalCourtesyMailRequest = createMailRequest(requestId, receiver);
        oReq.body(digitalCourtesyMailRequest);

        return CommonUtils.myPut(oReq,RequestEndpoint.EMAIL_ENDPOINT);
    }


    //PEC
    public static Response sendDigitalNotification(String clientId, String requestId, List<PnAttachment> attachments, String receiver) {
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        DigitalNotificationRequest digitalNotificationRequest = createDigitalNotificationRequest(requestId, receiver);
        List<String> attachmentsUri = attachments.stream().map(PnAttachment::getUri).toList();
        digitalNotificationRequest.setAttachmentUrls(attachmentsUri);

        oReq.body(digitalNotificationRequest);
        return CommonUtils.myPut(oReq,RequestEndpoint.PEC_ENDPOINT);
    }

    //CARTACEO
    public static Response sendPaperMessage(String clientId, String requestId, List<PnAttachment> attachments) {
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
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
                .header("x-pagopa-extch-service-id", clientId)
                .header("x-api-key", apiKey);
        oReq.body(events);
        return CommonUtils.myPut(oReq, RequestEndpoint.CONSOLIDATORE_ENDPOINT);
    }

    //CLIENT
    public static Response getClientConfigurations(String clientId) {
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId);
        ClientConfigurationDto clientConfigurationDto = createClientConfigurationRequest();
        oReq.body(clientConfigurationDto);
        return CommonUtils.myGet(oReq, RequestEndpoint.GET_CONFIGURATIONS_ENDPOINT);
    }

    public static Response getClient(String clientId){
        RequestSpecification oReq = stdReq()
                .pathParam("x-pagopa-extch-cx-id", clientId);
        return CommonUtils.myGet(oReq, RequestEndpoint.GET_CLIENT_ENDPOINT);
    }

    //GET REQUEST
    public static Response getRequest(String clientId, String requestId) {
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
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
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.PEC_ENDPOINT);
    }

    //GET EMAIL
    public static Response getEmailByRequestId(String clientId, String requestId){
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.EMAIL_ENDPOINT);
    }

    //GET SMS
    public static Response getSmsByRequestId(String clientId, String requestId){
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.SMS_ENDPOINT);
    }

    //GET PAPER
    public static Response getPaperByRequestId(String clientId, String requestId){
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        return CommonUtils.myGet(oReq, RequestEndpoint.CARTACEO_ENDPOINT);
    }

    //GET ATTACHMENTS
    public static Response getAttachmentsByFileKey( String fileKey, String clientId, String apiKey){

        RequestSpecification oReq = stdReq()
                .pathParam("fileKey", fileKey)
                .header("x-pagopa-extch-service-id", clientId)
                .header("x-api-key", apiKey);
        return CommonUtils.myGet(oReq, RequestEndpoint.GET_ATTACHMENT);
    }

    public static String generateRandomRequestId() {
        int targetStringLength = 30;
        String requestId = "PnEcMsCucumberTest";
        String generatedString = requestId.concat("-").concat(RandomStringUtils.randomAlphanumeric(targetStringLength));

        log.info("requestId {} ", generatedString);
        return generatedString;
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

    public static <E, D> ResponseEntity<D> convertObjectRequest(E entity, Class<D> dto) {
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            String responseBody = ((Response) entity).getBody().asString();
            D dtoObject = objectMapper.readValue(responseBody, dto);
            return ResponseEntity.ok(dtoObject);
        } catch (Exception e) {
            log.error("Errore durante la deserializzazione", e);
            throw new RuntimeException("Impossibile deserializzare la response", e);
        }
    }

}

