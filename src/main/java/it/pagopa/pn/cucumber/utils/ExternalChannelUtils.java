package it.pagopa.pn.cucumber.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import it.pagopa.pn.cucumber.RequestTemplate;
import it.pagopa.pn.cucumber.utils.CommonUtils;
import it.pagopa.pn.ec.rest.v1.api.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;

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
            "/consolidatore-ingress/v1/push-progress-events/{requestIdx}";


    protected static RequestSpecification stdReq() {
        return RestAssured.given()
                .header("Accept", "application/json")
                .header("Content-type", "application/json")
                .header("x-amzn-trace-id", java.util.UUID.randomUUID().toString());
    }

    /*
    un metodo che definisce una chiamata API per ogni channel
     */
    //SMS
    public static Response sendSmsCourtesySimpleMessage(String clientId, String requestId) {
        log.info("requestId {}", requestId);
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        DigitalCourtesySmsRequest digitalCourtesySmsRequest = createSmsRequest(requestId);
    log.info(digitalCourtesySmsRequest.getRequestId());
        oReq.body(digitalCourtesySmsRequest);
         Response response = CommonUtils.myPut(oReq,SEND_SMS_ENDPOINT);
         log.info(oReq.get().asString());

        return response;
    }



    // EMAIL

    public static Response sendEmailCourtesySimpleMessage(String clientId, String requestId) {
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        DigitalCourtesyMailRequest digitalCourtesyMailRequest = createMailRequest(requestId);
        oReq.body(digitalCourtesyMailRequest);
        Response response = CommonUtils.myPut(oReq,SEND_EMAIL_ENDPOINT);

        return response;
    }


    //PEC
    public static Response sendDigitalNotification(String clientId, String requestId) {
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        DigitalNotificationRequest digitalNotificationRequest = createDigitalNotificationRequest(requestId);

        oReq.body(digitalNotificationRequest);
        Response response = CommonUtils.myPut(oReq,SEND_PEC_ENDPOINT);
        return response;
    }

    //CARTACEO
    public static Response sendPaperMessage(String clientId, String requestId) {
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        PaperEngageRequest paperEngageRequest = createPaperEngageRequest(requestId);

        oReq.body(paperEngageRequest);
        Response response = CommonUtils.myPut(oReq,SEND_CARTACEO_ENDPOINT);
        return response;
    }

    //API Consolidatore
    public static Response sendRequestConsolidatore(String clientId, String requestId) {
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        ConsolidatoreIngressPaperProgressStatusEvent ingressPaperProgressStatusEvent ;
      //  oReq.body(ingressPaperProgressStatusEvent);
        Response response = CommonUtils.myPut(oReq, SEND_CONSOLIDATORE_ENDPOINT);
        return response;
    }

    public static String generateRandomRequestId() {
        int targetStringLength = 40;
        String requestId = "requestId";
        String generatedString = requestId.concat("-").concat(RandomStringUtils.randomAlphanumeric(targetStringLength));

        log.info("requestId {} ", generatedString);
        return generatedString;
    }





}
