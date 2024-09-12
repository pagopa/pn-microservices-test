package it.pagopa.pn.cucumber.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import it.pagopa.pn.cucumber.dto.pojo.Checksum;
import it.pagopa.pn.safestorage.generated.openapi.server.v1.dto.UpdateFileMetadataRequest;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Slf4j
public class SafeStorageUtils {

    private SafeStorageUtils() {
        throw new IllegalStateException("SafeStorageUtils is a utility class");
    }

    private static final String X_PAGOPA_SAFE_STORAGE_CX_ID = "x-pagopa-safestorage-cx-id";
    private static final String X_API_KEY = "x-api-key";
    private static final String X_CHECKSUM_VALUE = "x-checksum-value";

    protected static RequestSpecification stdReq() {
        return RestAssured.given()
                .header("Accept", APPLICATION_JSON_VALUE)
                .header("Content-type", APPLICATION_JSON_VALUE)
                .header("x-amzn-trace-id", java.util.UUID.randomUUID().toString());
    }

    protected static RequestSpecification stdReqKo() {
        return RestAssured.given()
                .header("Accept", APPLICATION_JSON_VALUE)
                .header("Content-type", APPLICATION_JSON_VALUE);
    }

    public static Response getPresignedURLUpload(String sCxId, String sAPIKey, String sContentType, String sDocType, String sSHA256, String sMD5, String sStatus, boolean boHeader, Checksum eCS) {
        log.debug("getPresignedURLUpload(\"{}\",\"{}\",\"{}\", \"{}\", \"{}\", \"{}\", \"{}\", {}, {})", sCxId, sAPIKey, sContentType, sDocType, sSHA256, sMD5, sStatus, (boHeader ? "header" : "body"), eCS.name());
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_SAFE_STORAGE_CX_ID, sCxId)
                .header(X_API_KEY, sAPIKey);
        if (boHeader) {
            switch (eCS) {
                case MD5:
                    oReq.header(X_CHECKSUM_VALUE, sMD5);
                    break;
                case SHA256:
                    oReq.header(X_CHECKSUM_VALUE, sSHA256);
                    break;
                default:
                    break;
            }
        }
        String sBody = "{ \"contentType\": \"" + sContentType + "\", \"documentType\": \"" + sDocType + "\", \"status\": \"" + sStatus + "\"";
        if (!boHeader) {
            switch (eCS) {
                case MD5:
                    sBody += ", \"checksumValue\": \"" + sMD5 + "\"";
                    break;
                case SHA256:
                    sBody += ", \"checksumValue\": \"" + sSHA256 + "\"";
                    break;
                default:
                    break;
            }
        }

        sBody += "}";
        oReq.body(sBody);
        return CommonUtils.myPost(oReq, "/safe-storage/v1/files");
    }

    public static Response getPresignedURLUpload(String sCxId, String sAPIKey, String sContentType, String sDocType, String sSHA256, String sMD5, String sStatus, boolean boHeader, Checksum eCS, String tag) {
        log.debug("getPresignedURLUpload(\"{}\",\"{}\",\"{}\", \"{}\", \"{}\", \"{}\", \"{}\", {}, {}, {})", sCxId, sAPIKey, sContentType, sDocType, sSHA256, sMD5, sStatus, (boHeader ? "header" : "body"), eCS.name(), tag);
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_SAFE_STORAGE_CX_ID, sCxId)
                .header(X_API_KEY, sAPIKey);
        if (boHeader) {
            switch (eCS) {
                case MD5:
                    oReq.header(X_CHECKSUM_VALUE, sMD5);
                    break;
                case SHA256:
                    oReq.header(X_CHECKSUM_VALUE, sSHA256);
                    break;
                default:
                    break;
            }
        }
        String sBody = "{ \"contentType\": \"" + sContentType + "\", \"documentType\": \"" + sDocType + "\", \"status\": \"" + sStatus + "\", \"tags\": \"" + tag + "\", \"  ";
        if (!boHeader) {
            switch (eCS) {
                case MD5:
                    sBody += ", \"checksumValue\": \"" + sMD5 + "\"";
                    break;
                case SHA256:
                    sBody += ", \"checksumValue\": \"" + sSHA256 + "\"";
                    break;
                default:
                    break;
            }
        }

        sBody += "}";
        oReq.body(sBody);
        return CommonUtils.myPost(oReq, "/safe-storage/v1/files");
    }

    public static Response getPresignedURLUploadKo(String sCxId, String sAPIKey, String sContentType, String sDocType, String sSHA256, String sMD5, String sStatus, boolean boHeader, Checksum eCS) {
        log.debug("getPresignedURLUpload(\"{}\",\"{}\",\"{}\", \"{}\", \"{}\", \"{}\", \"{}\", {}, {})", sCxId, sAPIKey, sContentType, sDocType, sSHA256, sMD5, sStatus, (boHeader ? "header" : "body"), eCS.name());
        RequestSpecification oReq = stdReqKo()
                .header(X_PAGOPA_SAFE_STORAGE_CX_ID, sCxId)
                .header(X_API_KEY, sAPIKey);
        if (boHeader) {
            switch (eCS) {
                case MD5:
                    oReq.header(X_CHECKSUM_VALUE, sMD5);
                    break;
                case SHA256:
                    oReq.header(X_CHECKSUM_VALUE, sSHA256);
                    break;
                default:
                    break;
            }
        }
        String sBody = "{ \"contentType\": \"" + sContentType + "\", \"documentType\": \"" + sDocType + "\", \"status\": \"" + sStatus + "\"";
        if (!boHeader) {
            switch (eCS) {
                case MD5:
                    sBody += ", \"checksumValue\": \"" + sMD5 + "\"";
                    break;
                case SHA256:
                    sBody += ", \"checksumValue\": \"" + sSHA256 + "\"";
                    break;
                default:
                    break;
            }
        }

        sBody += "}";
        oReq.body(sBody);

        return CommonUtils.myPost(oReq, "/safe-storage/v1/files");
    }


    public static Response getPresignedURLDownload(String sCxId, String sAPIKey, String sFileKey, boolean metadataOnly) {
        log.debug("getPresignedURLDownload(\"{}\",\"{}\",\"{}\")", sCxId, sAPIKey, sFileKey);
        RequestSpecification oReq = stdReq()
                .param("metadataOnly", metadataOnly)
                .header(X_PAGOPA_SAFE_STORAGE_CX_ID, sCxId)
                .header(X_API_KEY, sAPIKey);

        return CommonUtils.myGet(oReq, "/safe-storage/v1/files/" + sFileKey);
    }

    public static Response getObjectMetadata(String sCxId, String sAPIKey, String sFileKey) {
        log.debug("getObjectMetadata(\"{}\",\"{}\",\"{}\")", sCxId, sAPIKey, sFileKey);
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_SAFE_STORAGE_CX_ID, sCxId)
                .header(X_API_KEY, sAPIKey)
                .pathParam("fileKey", sFileKey)
                .param("metadataOnly", true);

        return CommonUtils.myGet(oReq, "/safe-storage/v1/files/{fileKey}");
    }

    public static Response getDocument(String sFileKey) {
        RequestSpecification oReq = stdReq()
                .pathParam("fileKey", sFileKey)
                .param("metadataOnly", true);

        return CommonUtils.myGet(oReq, "/safestorage/internal/v1/documents/{fileKey}");
    }

    public static Response updateObjectMetadata(String sCxId, String sAPIKey, String sFileKey, UpdateFileMetadataRequest requestBody) {

        ObjectMapper objMapper = new ObjectMapper();
        String body = "";

        try {
            body = objMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException jpe) {
            // decidere come gestire eccezione
        }

        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_SAFE_STORAGE_CX_ID, sCxId)
                .header(X_API_KEY, sAPIKey)
                .pathParam("fileKey", sFileKey)
                .body(body);

        return CommonUtils.myPost(oReq, "/safe-storage/v1/files/{fileKey}");
    }

    public static Response getDocumentsConfigs(String sCxId, String sAPIKey) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_SAFE_STORAGE_CX_ID, sCxId)
                .header(X_API_KEY, sAPIKey);

        return CommonUtils.myGet(oReq, "/safe-storage/v1/configurations/documents-types");
    }

    public static Response getCurrentClientConfig(String sCxId, String sAPIKey) {
        RequestSpecification oReq = stdReq()
                .header(X_API_KEY, sAPIKey)
                .pathParam("clientId", sCxId);

        return CommonUtils.myGet(oReq, "/safe-storage/v1/configurations/clients/{clientId}");
    }
}
