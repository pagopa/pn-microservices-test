package it.pagopa.pn.cucumber.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static it.pagopa.pn.cucumber.utils.CommonUtils.PN_IO;
import static it.pagopa.pn.cucumber.utils.CommonUtils.getBaseURL;

public class IoMessageUtils {

    private IoMessageUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static final String HEADER_CX_ID = "x-pagopa-iocon-cx-id";
    public static final String HEADER_CX_TAXID = "x-pagopa-cx-taxid";

    private static final String IO_MESSAGE_PATH = "/io/message";
    private static final String IO_PROFILE_PATH = "/io/profile";
    private static final String IO_MESSAGES_PATH = "/messages/";

    public static String generateRequestId() {
        return "IO-REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static Response sendIoMessage(Map<String, Object> body, String cxId) {
        return RestAssured.given()
                .baseUri(getBaseURL(PN_IO))
                .header(HEADER_CX_ID, cxId)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(IO_MESSAGE_PATH)
                .then()
                .extract()
                .response();
    }

    public static Response sendIoMessageWithoutCxId(Map<String, Object> body) {
        return RestAssured.given()
                .baseUri(getBaseURL(PN_IO))
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(IO_MESSAGE_PATH)
                .then()
                .extract()
                .response();
    }

    public static Response sendIoProfile(Map<String, Object> body, String cxId) {
        return RestAssured.given()
                .baseUri(getBaseURL(PN_IO))
                .header(HEADER_CX_ID, cxId)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(IO_PROFILE_PATH)
                .then()
                .extract()
                .response();
    }

    public static Map<String, Object> buildValidRequest(String iun, String recipientTaxId,
                                                        String senderServiceId,
                                                        String subject, String markdown) {
        Map<String, Object> body = new HashMap<>();
        body.put("iun", iun);
        body.put("recipientTaxId", recipientTaxId);
        body.put("senderServiceId", senderServiceId);
        body.put("subject", subject);
        body.put("markdown", markdown);
        return body;
    }

    public static Map<String, Object> buildValidRequestWithoutIun(String recipientTaxId,
                                                                   String senderServiceId,
                                                                   String subject,
                                                                   String markdown) {
        Map<String, Object> body = new HashMap<>();
        body.put("recipientTaxId", recipientTaxId);
        body.put("senderServiceId", senderServiceId);
        body.put("subject", subject);
        body.put("markdown", markdown);
        return body;
    }

    public static Map<String, Object> buildProfileRequest(String recipientTaxId,
                                                          String senderServiceId) {
        Map<String, Object> body = new HashMap<>();
        body.put("recipientTaxId", recipientTaxId);
        body.put("senderServiceId", senderServiceId);
        return body;
    }

    public static Response sendIoProfileWithoutCxId(Map<String, Object> body) {
        return RestAssured.given()
                .baseUri(getBaseURL(PN_IO))
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(IO_PROFILE_PATH)
                .then()
                .extract()
                .response();
    }

    public static Response getIoMessage(String requestId, String cxTaxId) {
        return RestAssured.given()
                .baseUri(getBaseURL(PN_IO))
                .header(HEADER_CX_TAXID, cxTaxId)
                .when()
                .get(IO_MESSAGES_PATH + requestId)
                .then()
                .extract()
                .response();
    }
}
