package it.pagopa.pn.cucumber.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static it.pagopa.pn.cucumber.utils.CommonUtils.PN_IO;
import static it.pagopa.pn.cucumber.utils.CommonUtils.PN_IO_EXTERNAL;
import static it.pagopa.pn.cucumber.utils.CommonUtils.getBaseURL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;

public class IoMessageUtils {

    private IoMessageUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static final String HEADER_CX_ID = "x-pagopa-iocon-cx-id";
    public static final String HEADER_CX_TAXID = "x-pagopa-pn-cx-id";

    private static final String IO_MESSAGE_PATH = "/io-connector/message";
    private static final String IO_PROFILE_PATH = "/io-connector/profile";
    private static final String IO_MESSAGES_PATH = "/io-connector/messages/";
    private static final String IO_SENDMESSAGE_PATH = "/io-connector/sendmessage";
    private static final String IO_USER_STATUS_PATH = "/io-connector/user-status";

    public static String generateRequestId() {
        return "IO-REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        //return "TEST_REQ-20260604_1";
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

    public static Response getIoMessageWithoutTaxId(String requestId) {
        return RestAssured.given()
                .baseUri(getBaseURL(PN_IO))
                .when()
                .get(IO_MESSAGES_PATH + requestId)
                .then()
                .extract()
                .response();
    }

    public static Response getIoAttachment(String requestId, String fileKey, String cxTaxId) {
        return RestAssured.given()
                .baseUri(getBaseURL(PN_IO_EXTERNAL))
                .header(HEADER_CX_TAXID, cxTaxId)
                .redirects().follow(false)
                .when()
                .get(IO_MESSAGES_PATH + requestId + "/" + fileKey)
                .then()
                .extract()
                .response();
    }

    public static Response getIoAttachmentWithoutTaxId(String requestId, String fileKey) {
        return RestAssured.given()
                .baseUri(getBaseURL(PN_IO_EXTERNAL))
                .redirects().follow(false)
                .when()
                .get(IO_MESSAGES_PATH + requestId + "/" + fileKey)
                .then()
                .extract()
                .response();
    }

    public static String generateIun() {
        return randomLetters(4) + "-" + randomLetters(4) + "-" + randomLetters(4)
                + "-" + YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                + "-" + randomLetters(1) + "-" + ThreadLocalRandom.current().nextInt(10);
    }

    private static String randomLetters(int length) {
        StringBuilder letters = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            letters.append((char) ('A' + ThreadLocalRandom.current().nextInt(26)));
        }
        return letters.toString();
    }

    public static Response sendLegalIoMessage(Map<String, Object> body) {
        return RestAssured.given()
                .baseUri(getBaseURL(PN_IO))
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(IO_SENDMESSAGE_PATH)
                .then()
                .extract()
                .response();
    }

    public static Response getIoUserStatus(Map<String, Object> body) {
        return RestAssured.given()
                .baseUri(getBaseURL(PN_IO))
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(IO_USER_STATUS_PATH)
                .then()
                .extract()
                .response();
    }

    public static Map<String, Object> buildLegalSendMessageRequest(String recipientTaxId, String senderDenomination) {
        Map<String, Object> body = new HashMap<>();
        body.put("requestId", generateRequestId());
        body.put("iun", generateIun());
        body.put("recipientTaxID", recipientTaxId);
        body.put("recipientInternalID", "PF-" + UUID.randomUUID());
        body.put("recipientIndex", 0);
        body.put("senderDenomination", senderDenomination);
        body.put("subject", "Notifica di test BDD");
        return body;
    }

    public static Map<String, Object> buildUserStatusRequest(String taxId) {
        Map<String, Object> body = new HashMap<>();
        body.put("taxId", taxId);
        return body;
    }


    public static String hashTaxId(String taxId) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(taxId.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 non disponibile", e);
        }
    }
}
