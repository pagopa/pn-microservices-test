package it.pagopa.pn.cucumber.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.cucumber.utils.CommonUtils.PN_IO;
import static it.pagopa.pn.cucumber.utils.CommonUtils.PN_MOCKSERVER;
import static it.pagopa.pn.cucumber.utils.CommonUtils.PN_USER_ATTRIBUTES;
import static it.pagopa.pn.cucumber.utils.CommonUtils.getBaseURL;

public class IoActivationUtils {

    private IoActivationUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static final String HEADER_UID = "x-pagopa-pn-uid";
    public static final String HEADER_CX_TYPE = "x-pagopa-pn-cx-type";
    public static final String HEADER_CX_ID = "x-pagopa-pn-cx-id";

    public static final String FIELD_FISCAL_CODE = "fiscal_code";
    public static final String FIELD_STATUS = "status";

    private static final String IO_ACTIVATIONS_PATH = "/io-connector/activations";
    private static final String USER_CONSENTS_PATH = "/user-consents/v1/consents/";
    private static final String MOCKSERVER_EXPECTATION_PATH = "/mockserver/expectation";
    private static final String MOCKSERVER_VERIFY_PATH = "/mockserver/verify";
    private static final String USER_CONSENTS_EXPECTATION_ID = "pn-microservices-test-io-activation-user-consents";
    private static final String MOCKSERVER_CLEAR_PATH = "/mockserver/clear";
    private static final String IO_ACTIVATIONS_MOCK_PATH = "/io(/api/v1)?/activations/?";
    private static final String IO_UPSERT_EXPECTATION_ID = "pn-microservices-test-io-activation-io-upsert";
    private static final String IO_READ_EXPECTATION_ID = "pn-microservices-test-io-activation-io-read";
    private static final int IO_EXPECTATION_PRIORITY = 10;
    private static final String IO_SERVICE_ID = "PN-MICROSERVICES-TEST";

    public static final String IO_METHOD_UPSERT = "PUT";
    public static final String IO_METHOD_READ = "POST";

    public static Map<String, Object> buildActivationPayload(String fiscalCode, String status) {
        Map<String, Object> body = new HashMap<>();
        body.put(FIELD_FISCAL_CODE, fiscalCode);
        body.put(FIELD_STATUS, status);
        return body;
    }

    public static Map<String, Object> buildFiscalCodePayload(String fiscalCode) {
        Map<String, Object> body = new HashMap<>();
        body.put(FIELD_FISCAL_CODE, fiscalCode);
        return body;
    }

    public static Map<String, String> buildAuthFleetHeaders(String uid, String cxType, String cxId) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(HEADER_UID, uid);
        headers.put(HEADER_CX_TYPE, cxType);
        headers.put(HEADER_CX_ID, cxId);
        return headers;
    }

    public static Response upsertIoActivation(Map<String, Object> body, Map<String, String> headers) {
        return RestAssured.given()
                .baseUri(getBaseURL(PN_IO))
                .headers(headers)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put(IO_ACTIVATIONS_PATH)
                .then()
                .extract()
                .response();
    }

    public static Response getIoActivation(Map<String, Object> body) {
        return RestAssured.given()
                .baseUri(getBaseURL(PN_IO))
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(IO_ACTIVATIONS_PATH)
                .then()
                .extract()
                .response();
    }

    public static Response getUserConsentByType(Map<String, String> headers, String consentType, String version) {
        RequestSpecification request = RestAssured.given()
                .baseUri(getBaseURL(PN_USER_ATTRIBUTES))
                .headers(headers);
        if (version != null) {
            request.queryParam("version", version);
        }
        return request
                .when()
                .get(USER_CONSENTS_PATH + consentType)
                .then()
                .extract()
                .response();
    }

    public static boolean isMockServerEnabled() {
        String port = System.getProperty("pn.mockserver.port");
        return port != null && !port.isBlank();
    }

    public static Response registerUserConsentsExpectation() {
        Map<String, Object> httpRequest = new HashMap<>();
        httpRequest.put("method", "PUT");
        httpRequest.put("path", USER_CONSENTS_PATH + ".*");
        Map<String, Object> expectation = new HashMap<>();
        expectation.put("id", USER_CONSENTS_EXPECTATION_ID);
        expectation.put("httpRequest", httpRequest);
        expectation.put("httpResponse", Map.of("statusCode", 200));
        expectation.put("times", Map.of("unlimited", true));
        return RestAssured.given()
                .baseUri(getBaseURL(PN_MOCKSERVER))
                .contentType(ContentType.JSON)
                .body(expectation)
                .when()
                .put(MOCKSERVER_EXPECTATION_PATH)
                .then()
                .extract()
                .response();
    }

    public static Response verifyUserConsentAccepted(String uid, String consentType, String version,
                                                     Map<String, Object> times) {
        Map<String, Object> httpRequest = new HashMap<>();
        httpRequest.put("method", "PUT");
        httpRequest.put("path", USER_CONSENTS_PATH + consentType);
        httpRequest.put("queryStringParameters", Map.of("version", List.of(version)));
        httpRequest.put("headers", Map.of(HEADER_UID, List.of(uid)));
        httpRequest.put("body", Map.of("type", "JSON", "json", Map.of("action", "ACCEPT", "channel", "IO")));
        Map<String, Object> verification = new HashMap<>();
        verification.put("httpRequest", httpRequest);
        verification.put("times", times);
        return RestAssured.given()
                .baseUri(getBaseURL(PN_MOCKSERVER))
                .contentType(ContentType.JSON)
                .body(verification)
                .when()
                .put(MOCKSERVER_VERIFY_PATH)
                .then()
                .extract()
                .response();
    }

    public static Response registerIoActivationResponse(String method, String fiscalCode, String status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service_id", IO_SERVICE_ID);
        body.put(FIELD_FISCAL_CODE, fiscalCode);
        body.put(FIELD_STATUS, status);
        body.put("version", 1);
        return registerIoActivationExpectation(method, fiscalCode, 200, "application/json", body);
    }

    public static Response registerIoActivationError(String method, String fiscalCode, int statusCode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", statusCode);
        body.put("title", "Mocked IO error");
        return registerIoActivationExpectation(method, fiscalCode, statusCode, "application/problem+json", body);
    }

    private static Response registerIoActivationExpectation(String method, String fiscalCode, int statusCode,
                                                            String contentType, Map<String, Object> body) {
        Map<String, Object> httpResponse = new HashMap<>();
        httpResponse.put("statusCode", statusCode);
        httpResponse.put("headers", Map.of("Content-Type", List.of(contentType)));
        httpResponse.put("body", Map.of("type", "JSON", "json", body));
        Map<String, Object> expectation = new HashMap<>();
        expectation.put("id", IO_METHOD_UPSERT.equals(method) ? IO_UPSERT_EXPECTATION_ID : IO_READ_EXPECTATION_ID);
        expectation.put("priority", IO_EXPECTATION_PRIORITY);
        expectation.put("httpRequest", ioActivationRequestMatcher(method, fiscalCode, null));
        expectation.put("httpResponse", httpResponse);
        expectation.put("times", Map.of("unlimited", true));
        return RestAssured.given()
                .baseUri(getBaseURL(PN_MOCKSERVER))
                .contentType(ContentType.JSON)
                .body(expectation)
                .when()
                .put(MOCKSERVER_EXPECTATION_PATH)
                .then()
                .extract()
                .response();
    }

    public static Response verifyIoActivationRequest(String method, String fiscalCode, String status,
                                                     Map<String, Object> times) {
        Map<String, Object> verification = new HashMap<>();
        verification.put("httpRequest", ioActivationRequestMatcher(method, fiscalCode, status));
        verification.put("times", times);
        return RestAssured.given()
                .baseUri(getBaseURL(PN_MOCKSERVER))
                .contentType(ContentType.JSON)
                .body(verification)
                .when()
                .put(MOCKSERVER_VERIFY_PATH)
                .then()
                .extract()
                .response();
    }

    public static void clearIoActivationMocks(String fiscalCode) {
        for (String id : List.of(IO_UPSERT_EXPECTATION_ID, IO_READ_EXPECTATION_ID)) {
            Response cleared = clearMockServer("EXPECTATIONS", Map.of("id", id));
            boolean notRegistered = cleared.getStatusCode() == 400
                    && cleared.getBody().asString().contains("No expectation found with id");
            if (cleared.getStatusCode() != 200 && !notRegistered) {
                throw new IllegalStateException("Pulizia mock IO " + id + " fallita: HTTP " + cleared.getStatusCode()
                        + " " + cleared.getBody().asString());
            }
        }
        for (String method : List.of(IO_METHOD_UPSERT, IO_METHOD_READ)) {
            Response cleared = clearMockServer("LOG", ioActivationRequestMatcher(method, fiscalCode, null));
            if (cleared.getStatusCode() != 200) {
                throw new IllegalStateException("Pulizia log MockServer per " + method + " fallita: HTTP "
                        + cleared.getStatusCode() + " " + cleared.getBody().asString());
            }
        }
    }

    private static Response clearMockServer(String type, Map<String, Object> body) {
        return RestAssured.given()
                .baseUri(getBaseURL(PN_MOCKSERVER))
                .contentType(ContentType.JSON)
                .queryParam("type", type)
                .body(body)
                .when()
                .put(MOCKSERVER_CLEAR_PATH)
                .then()
                .extract()
                .response();
    }

    private static Map<String, Object> ioActivationRequestMatcher(String method, String fiscalCode, String status) {
        Map<String, Object> json = new HashMap<>();
        json.put(FIELD_FISCAL_CODE, fiscalCode);
        if (status != null) {
            json.put(FIELD_STATUS, status);
        }
        Map<String, Object> httpRequest = new HashMap<>();
        httpRequest.put("method", method);
        httpRequest.put("path", IO_ACTIVATIONS_MOCK_PATH);
        httpRequest.put("body", Map.of("type", "JSON", "json", json, "matchType", "ONLY_MATCHING_FIELDS"));
        return httpRequest;
    }
}
