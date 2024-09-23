package it.pagopa.pn.cucumber.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import it.pagopa.pn.cucumber.dto.pojo.Checksum;
import it.pagopa.pn.safestorage.generated.openapi.server.v1.dto.FileCreationRequest;
import it.pagopa.pn.safestorage.generated.openapi.server.v1.dto.UpdateFileMetadataRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static it.pagopa.pn.cucumber.utils.LogUtils.*;
import static it.pagopa.pn.cucumber.utils.RequestEndpoint.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Slf4j
public class SafeStorageUtils {

	public static final String METADATA_ONLY = "metadataOnly";
	public static final String FILE_KEY = "fileKey";
	public static final String CHECKSUM_VALUE = "checksumValue";
	private static final ObjectMapper objectMapper = new ObjectMapper();

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

	public static Response getPresignedURLUpload(String sCxId, String sAPIKey, FileCreationRequest fileCreationRequest, String sSHA256, String sMD5, boolean boHeader, Checksum eCS, boolean traceId) {
		log.debug(INVOKING_SAFE_STORAGE, GET_PRESIGNED_URL_DOWNLOAD, Stream.of(sCxId, sAPIKey, fileCreationRequest, sSHA256, sMD5, (boHeader ? "header" : "body"), eCS.name()).toList());
		RequestSpecification oReq = stdReq()
				.header(X_PAGOPA_SAFE_STORAGE_CX_ID, sCxId)
				.header(X_API_KEY, sAPIKey);
		if (traceId) {
			oReq.header("x-amzn-trace-id", java.util.UUID.randomUUID().toString());
		}
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
		oReq.body(fileCreationRequest);
		return CommonUtils.myPost(oReq, SAFESTORAGE_FILES_UPLOAD_ENDPOINT);
	}

	public static Response getPresignedURLDownload(String sCxId, String sAPIKey, String sFileKey, boolean metadataOnly) {
		log.debug(INVOKING_SAFE_STORAGE, GET_PRESIGNED_URL_DOWNLOAD, Stream.of(sCxId, sAPIKey, sFileKey).toList());
		RequestSpecification oReq = stdReq()
				.pathParam(FILE_KEY, sFileKey)
				.param(METADATA_ONLY, metadataOnly)
				.header(X_PAGOPA_SAFE_STORAGE_CX_ID, sCxId)
				.header(X_API_KEY, sAPIKey);
		return CommonUtils.myGet(oReq, SAFESTORAGE_FILES_DOWNLOAD_ENDPOINT);
	}

	public static Response getObjectMetadata(String sCxId, String sAPIKey, String sFileKey) {
		log.debug(INVOKING_SAFE_STORAGE, GET_OBJECT_METADATA, Stream.of(sCxId, sAPIKey, sFileKey).toList());
		RequestSpecification oReq = stdReq()
				.header(X_PAGOPA_SAFE_STORAGE_CX_ID, sCxId)
				.header(X_API_KEY, sAPIKey)
				.pathParam(FILE_KEY, sFileKey)
				.param(METADATA_ONLY, true);
		return CommonUtils.myGet(oReq, SAFESTORAGE_FILES_DOWNLOAD_ENDPOINT);
	}

	public static Response getDocument(String sFileKey) {
		RequestSpecification oReq = stdReq()
				.pathParam(FILE_KEY, sFileKey)
				.param(METADATA_ONLY, true);
		return CommonUtils.myGet(oReq, SAFESTORAGE_INTERNAL_DOCUMENTS_GET_ENDPOINT);
	}

	public static Response updateObjectMetadata (String sCxId, String sAPIKey, String sFileKey, UpdateFileMetadataRequest requestBody) {
		String body = "";

		try {
			body = objectMapper.writeValueAsString(requestBody);
		} catch (JsonProcessingException jpe) {
			// decidere come gestire eccezione
		}

		RequestSpecification oReq = stdReq()
				.header(X_PAGOPA_SAFE_STORAGE_CX_ID, sCxId)
				.header(X_API_KEY, sAPIKey)
				.pathParam(FILE_KEY, sFileKey)
				.body(body);
		return CommonUtils.myPost(oReq, SAFESTORAGE_UPDATE_METADATA_ENDPOINT);
	}

    public static Response getDocumentsConfigs(String sCxId, String sAPIKey) {
        RequestSpecification oReq = stdReq()
                .header(X_PAGOPA_SAFE_STORAGE_CX_ID, sCxId)
                .header(X_API_KEY, sAPIKey);
        return CommonUtils.myGet(oReq, SAFESTORAGE_DOCUMENT_TYPES_GET_ENDPOINT);
    }

    public static Response getCurrentClientConfig(String sCxId, String sAPIKey) {
        RequestSpecification oReq = stdReq()
                .header(X_API_KEY, sAPIKey)
                .pathParam("clientId", sCxId);
        return CommonUtils.myGet(oReq, SAFESTORAGE_CONFIGURATION_CLIENT_GET_ENDPOINT);
    }
}
