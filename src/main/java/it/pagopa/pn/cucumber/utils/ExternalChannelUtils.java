package it.pagopa.pn.cucumber.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import it.pagopa.pn.cucumber.RequestTemplate;
import it.pagopa.pn.cucumber.dto.ClientConfigurationInternalDto;
import it.pagopa.pn.cucumber.dto.pojo.PnAttachment;
import it.pagopa.pn.ec.rest.v1.api.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class ExternalChannelUtils extends RequestTemplate {

    private static final String SEND_SMS_ENDPOINT =
            "/external-channels/v1/digital-deliveries/courtesy-simple-message-requests/{requestIdx}" ;

    private static final String SEND_EMAIL_ENDPOINT =
            "/external-channels/v1/digital-deliveries/courtesy-full-message-requests/{requestIdx}";

    private static final String SEND_PEC_ENDPOINT =
            "/external-channels/v1/digital-deliveries/legal-full-message-requests/{requestIdx}";

    private static final String SEND_CARTACEO_ENDPOINT =
            "/external-channels/v1/paper-deliveries-engagements/{requestIdx}";
    private static final String SEND_CONSOLIDATORE_ENDPOINT =
            "/consolidatore-ingress/v1/push-progress-events";

    private static final String GET_CONFIGURATIONS_ENDPOINT =
            "/external-channels/v1/configurations";

    private static final String GET_CLIENT_ENDPOINT =
            "/external-channel/gestoreRepository/clients/{x-pagopa-extch-cx-id}";

    private static final String GET_PEC_ENDPOINT =
            "/external-channels/v1/digital-deliveries/legal-full-message-requests/{requestIdx}";


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
         Response response = CommonUtils.myPut(oReq,SEND_SMS_ENDPOINT);
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

        return CommonUtils.myPut(oReq,SEND_EMAIL_ENDPOINT);
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
        return CommonUtils.myPut(oReq,SEND_PEC_ENDPOINT);
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
        return CommonUtils.myPut(oReq,SEND_CARTACEO_ENDPOINT);
    }

    //API Consolidatore
    public static Response sendRequestConsolidatore(String clientId, String apiKey, List<ConsolidatoreIngressPaperProgressStatusEvent> events) {
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-service-id", clientId)
                .header("x-api-key", apiKey);
        oReq.body(events);
        return CommonUtils.myPut(oReq, SEND_CONSOLIDATORE_ENDPOINT);
    }

    //CLIENT
    public static Response getConfigurations(String clientId) {
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId);
        // .pathParam("requestIdx", generateRandomRequestId());
        ClientConfigurationDto clientConfigurationDto = createClientConfigurationRequest();
        oReq.body(clientConfigurationDto);
        return CommonUtils.myGet(oReq, GET_CONFIGURATIONS_ENDPOINT);
    }

    public static Response getClient(String clientId){
        RequestSpecification oReq = stdReq()
                .pathParam("x-pagopa-extch-cx-id", clientId);
      //  ClientConfigurationInternalDto clientConfigurationInternalDto = createClientConfigurationInternalRequest();
        //oReq.body(clientConfigurationInternalDto);
        return CommonUtils.myGet(oReq, GET_CLIENT_ENDPOINT);
    }

    //GET PEC
    public static Response getPecByRequestId(String clientId, String requestId){
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        return CommonUtils.myGet(oReq, GET_PEC_ENDPOINT);
    }

    public static String generateRandomRequestId() {
        int targetStringLength = 30;
        String requestId = "PnEcMsCucumberTest";
        String generatedString = requestId.concat("-").concat(RandomStringUtils.randomAlphanumeric(targetStringLength));

        log.info("requestId {} ", generatedString);
        return generatedString;
    }

}
