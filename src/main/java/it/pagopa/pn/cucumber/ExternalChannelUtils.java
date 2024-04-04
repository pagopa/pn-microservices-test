package it.pagopa.pn.cucumber;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import it.pagopa.pn.ec.rest.v1.api.DigitalCourtesyMailRequest;
import it.pagopa.pn.ec.rest.v1.api.DigitalCourtesySmsRequest;
import it.pagopa.pn.ec.rest.v1.api.DigitalNotificationRequest;
import it.pagopa.pn.ec.rest.v1.dto.DigitalLegalMessagesApi;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

@Slf4j
public class ExternalChannelUtils {

    private static final String SEND_SMS_ENDPOINT =
            "/external-channels/v1/digital-deliveries/courtesy-simple-message-requests/{requestIdx}" ;

    private static final String SEND_EMAIL_ENDPOINT =
            "/external-channels/v1/digital-deliveries/courtesy-simple-message-requests/{requestIdx}";

    private static final String SEND_PEC_ENDPOINT =
            "/external-channels/v1/digital-deliveries/legal-full-message-requests/{requestIdx}";

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
        RequestSpecification oReq = stdReq()
                .header("x-pagopa-extch-cx-id", clientId)
                .pathParam("requestIdx", requestId);
        DigitalCourtesySmsRequest digitalCourtesySmsRequest = createSmsRequest(requestId);

        oReq.body(digitalCourtesySmsRequest);
         Response response = CommonUtils.myPut(oReq,SEND_SMS_ENDPOINT);

        return response;
    }

    public static DigitalCourtesySmsRequest createSmsRequest(String requestId){
        String defaultStringInit = "stringDefault";

        DigitalCourtesySmsRequest digitalCourtesySmsRequestFactory= new DigitalCourtesySmsRequest();
        digitalCourtesySmsRequestFactory.setRequestId(requestId);
        digitalCourtesySmsRequestFactory.eventType(defaultStringInit);
        digitalCourtesySmsRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        digitalCourtesySmsRequestFactory.setQos(DigitalCourtesySmsRequest.QosEnum.INTERACTIVE);
        digitalCourtesySmsRequestFactory.setReceiverDigitalAddress(Config.getInstance().getSmsReceiverDigitalAddress());
        digitalCourtesySmsRequestFactory.setMessageText(defaultStringInit);
        digitalCourtesySmsRequestFactory.channel(DigitalCourtesySmsRequest.ChannelEnum.SMS);
        return digitalCourtesySmsRequestFactory;
    }

    public static String generateRandomRequestId() {
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        log.debug("RequestID: " + generatedString);
        return generatedString;
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

    public static DigitalCourtesyMailRequest createMailRequest(String requestId) {
        String defaultStringInit = "stringDefault";

        DigitalCourtesyMailRequest digitalCourtesyMailRequestFactory= new DigitalCourtesyMailRequest();
        digitalCourtesyMailRequestFactory.setRequestId(requestId);
        digitalCourtesyMailRequestFactory.eventType(defaultStringInit);
        digitalCourtesyMailRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        digitalCourtesyMailRequestFactory.setQos(DigitalCourtesyMailRequest.QosEnum.INTERACTIVE);
        digitalCourtesyMailRequestFactory.setReceiverDigitalAddress("+");
        digitalCourtesyMailRequestFactory.setMessageText(defaultStringInit);
        digitalCourtesyMailRequestFactory.channel(DigitalCourtesyMailRequest.ChannelEnum.EMAIL);
        return digitalCourtesyMailRequestFactory;
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
    public static DigitalNotificationRequest createDigitalNotificationRequest(String requestId){
        String defaultStringInit = "stringDefault";

        DigitalNotificationRequest digitalNotificationRequestFactory = new DigitalNotificationRequest();
        digitalNotificationRequestFactory.setRequestId(requestId);
        digitalNotificationRequestFactory.eventType(defaultStringInit);
        digitalNotificationRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        digitalNotificationRequestFactory.setQos(DigitalNotificationRequest.QosEnum.INTERACTIVE);
        digitalNotificationRequestFactory.setReceiverDigitalAddress("+");
        digitalNotificationRequestFactory.setMessageText(defaultStringInit);
        digitalNotificationRequestFactory.channel(DigitalNotificationRequest.ChannelEnum.PEC);
        return digitalNotificationRequestFactory;
    }

}
