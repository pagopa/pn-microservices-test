package it.pagopa.pn.cucumber.utils;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.QueryableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.SpecificationQuerier;
import it.pagopa.pn.cucumber.dto.pojo.Checksum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import static it.pagopa.pn.configuration.TestVariablesConfiguration.getValueIfTagged;

@Slf4j
public class CommonUtils {

	private CommonUtils() {
		throw new IllegalStateException("CommonUtils is a utility class");
	}

	private static final RestAssuredConfig REST_ASSURED_CONFIG = RestAssured.config().encoderConfig(new EncoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false));
	public static final String BASE_URL_PROPERTY = "baseURL";
	private static final String BASE_URL = System.getProperty(BASE_URL_PROPERTY) == null ? "http://localhost" : System.getProperty(BASE_URL_PROPERTY);
	private static final String PN_EC_PORT = System.getProperty("pn.ec.port") == null ? "" : System.getProperty("pn.ec.port");
	private static final String PN_SS_PORT = System.getProperty("pn.ss.port") == null ? "" : System.getProperty("pn.ss.port");
	private static final String PN_SM_PORT = System.getProperty("pn.sm.port") == null ? "" : System.getProperty("pn.sm.port");
	private static final String PN_PDFRASTER_PORT = System.getProperty("pn.pdfraster.port") == null ? "" : System.getProperty("pn.pdfraster.port");
	private static final String PN_IO_PORT = System.getProperty("pn.io.port") == null ? "" : System.getProperty("pn.io.port");
	private static final String PN_IO_EXTERNAL_PORT = System.getProperty("pn.io.external.port") == null ? "" : System.getProperty("pn.io.external.port");
	public static final String PN_EC = "pnEc";
	public static final String PN_SS = "pnSs";
	public static final String PN_SM = "pnSm";
	public static final String PN_PDFRASTER = "pnPdfRaster";
	public static final String PN_IO = "pnIo";
	public static final String PN_IO_EXTERNAL = "pnIoExternal";

	protected static String getPort(String service) {
		if (service.equals(PN_EC)) {
			return PN_EC_PORT;
		} else if (service.equals(PN_SS)) {
			return PN_SS_PORT;
		} else if (service.equals(PN_SM)) {
			return PN_SM_PORT;
		} else if (service.equals(PN_PDFRASTER)) {
			return PN_PDFRASTER_PORT;
		} else if (service.equals(PN_IO)) {
			return PN_IO_PORT;
		} else if (service.equals(PN_IO_EXTERNAL)) {
			return PN_IO_EXTERNAL_PORT;
		} else {
			return "";
		}
	}

	protected static String getBaseURL(String service) {
		String url = BASE_URL;
		String port = getPort(service);
		if (port != null && !port.isEmpty()) {
			url += ":" + port;
		}
		return url;
	}

	public static Response uploadFile(String sURL, File oFile, String sSHA256, String sMD5, String sContentType, String sSecret, Checksum eCS) {

		log.trace("uploadFile('{}', '{}', '{}', '{}', '{}')", sURL, sSHA256, sMD5, sContentType, sSecret);
		RequestSpecification oReq = RestAssured.given()
				.config(REST_ASSURED_CONFIG)
				.header("content-type", sContentType);

		if (eCS.equals(Checksum.SHA256)) oReq.header("x-amz-checksum-sha256", sSHA256);
		else if (eCS.equals(Checksum.MD5)) oReq.header("Content-MD5", sMD5);

		if (sSecret != null) {
			oReq.header("x-amz-meta-secret", sSecret);
		}
		oReq.body(oFile);
		String sMyURL = URLDecoder.decode(sURL, StandardCharsets.UTF_8);
		return oReq.put(sMyURL);
	}

	public static Response uploadFileByte(String sURL, byte[] fileBytes, String sSHA256, String sMD5, String sContentType, String sSecret, Checksum eCS) {

		log.trace("uploadFile(byte[])('{}', '{}', '{}', '{}', '{}')", sURL, sSHA256, sMD5, sContentType, sSecret);

		RequestSpecification oReq = RestAssured.given()
				.config(REST_ASSURED_CONFIG)
				.header("content-type", sContentType);

		if (eCS.equals(Checksum.SHA256)) {
			oReq.header("x-amz-checksum-sha256", sSHA256);
		} else if (eCS.equals(Checksum.MD5)) {
			oReq.header("Content-MD5", sMD5);
		}

		if (sSecret != null) {
			oReq.header("x-amz-meta-secret", sSecret);
		}

		oReq.body(fileBytes);

		String sMyURL = URLDecoder.decode(sURL, StandardCharsets.UTF_8);
		return oReq.put(sMyURL);
	}

	protected static Response myGet(RequestSpecification oReqSpec, String sURI, String service) {
		oReqSpec.given().baseUri(getBaseURL(service)).basePath(sURI);
		QueryableRequestSpecification queryRequest = SpecificationQuerier.query(oReqSpec);
		log.debug("GET {}", queryRequest.getURI());
		return oReqSpec.get();
	}

	protected static Response myPost(RequestSpecification oReqSpec, String sURI, String service) {
		oReqSpec.given().baseUri(getBaseURL(service)).basePath(sURI);
		QueryableRequestSpecification queryRequest = SpecificationQuerier.query(oReqSpec);
		log.debug("POST {}. Request body -> {}", queryRequest.getURI(), queryRequest.getBody().toString());
		return oReqSpec.post();
	}

	protected static Response myPut(RequestSpecification oReqSpec, String sURI, String service) {
		oReqSpec.given().baseUri(getBaseURL(service)).basePath(sURI);
		QueryableRequestSpecification queryRequest = SpecificationQuerier.query(oReqSpec);
		log.debug("PUT {}. Request body -> {}", queryRequest.getURI(), queryRequest.getBody().toString());
		return oReqSpec.put();
	}

	protected static Response myPatch(RequestSpecification oReqSpec, String sURI, String service) {
		oReqSpec.given().baseUri(getBaseURL(service)).basePath(sURI);
		QueryableRequestSpecification queryRequest = SpecificationQuerier.query(oReqSpec);
		log.debug("PATCH {}. Request body -> {}", queryRequest.getURI(), queryRequest.getBody().toString());
		return oReqSpec.patch();
	}

	@SneakyThrows({NoSuchAlgorithmException.class, IOException.class})
	public static String getSHA256(File file) {
		try (FileInputStream oFIS = new FileInputStream(file)) {
			byte[] baFile = oFIS.readAllBytes();
			MessageDigest md = MessageDigest.getInstance("SHA256");
			md.update(baFile);
			byte[] digest = md.digest();
			return Base64.getEncoder().encodeToString(digest);
		}
	}

	@SneakyThrows({NoSuchAlgorithmException.class, IOException.class})
	public static String getMD5(File file) {
		try (FileInputStream oFIS = new FileInputStream(file)) {
			byte[] baFile = oFIS.readAllBytes();
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(baFile);
			byte[] digest = md.digest();
			return Base64.getEncoder().encodeToString(digest);
		}
	}

	public static String getValueOrDefault(Map<String, String> map, String key, String defaultValue) {
		try {
			String value = map.get(key);
			if (value == null) {
				return defaultValue;
			}
			return getValueIfTagged(value);
		} catch (NullPointerException e) {
			return defaultValue;
		}
	}

	public static Response sendMultipartRequest(String endpoint, File file, String partName, String contentType, String service) {
		return RestAssured
				.given()
				.baseUri(getBaseURL(service))
				.multiPart(partName, file, contentType)
				.when()
				.post(endpoint)
				.then()
				.extract().response();
	}
}