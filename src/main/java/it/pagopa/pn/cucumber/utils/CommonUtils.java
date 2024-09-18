package it.pagopa.pn.cucumber.utils;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.response.Response;
import io.restassured.specification.QueryableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.SpecificationQuerier;
import it.pagopa.pn.configuration.TestVariablesConfiguration;
import it.pagopa.pn.cucumber.dto.pojo.Checksum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Null;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;


@Slf4j
public class CommonUtils {

	private CommonUtils() {
		throw new IllegalStateException("CommonUtils is a utility class");
	}

	private static String baseURL = null;

	protected static String getBaseURL() {
		if( baseURL == null ) {
			baseURL = System.getProperty("baseURL");
			if( baseURL == null ) {
				baseURL="";
			}
		}
		return baseURL;
	}
	
	public static int checkDump(Response oResp, boolean boDumpBody) {
		int iRc = oResp.getStatusCode();

		if (boDumpBody && log.isDebugEnabled()) {
			oResp.then().log().all();
		}
		return iRc;
	}

	@SneakyThrows(UnsupportedEncodingException.class)
	public static Response uploadFile(String sURL, File oFile, String sSHA256, String sMD5, String sContentType, String sSecret, Checksum eCS) {

		log.debug("uploadFile(\"{}\", \"{}\", \"{}\", \"{}\", \"{}\", "+eCS.name()+")", sURL, sSHA256, sMD5, sContentType, sSecret);
		EncoderConfig encoderConfig = new EncoderConfig();
		RequestSpecification oReq = RestAssured.given()
			.config(RestAssured.config()
                    .encoderConfig(encoderConfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
			.header("content-type", sContentType);
		switch (eCS) {
			case MD5:
				oReq.header("Content-MD5", sMD5);
				break;
			case SHA256:
				oReq.header("x-amz-checksum-sha256", sSHA256);
				break;
			default:
				break;
		}
		if( sSecret != null ) {

			oReq.header("x-amz-meta-secret", sSecret);
		}
		oReq.body(oFile);

		if (log.isDebugEnabled() ) {
			oReq.log().all();
		}
		String sMyURL = URLDecoder.decode(sURL, "utf-8");
		Response oResp = oReq
			.put(sMyURL);
		//log.debug("In upload file oResp --> " + oResp.getBody().asString());

		return oResp;
	}

	protected static Response myGet(RequestSpecification oReqSpec, String sURI) {
		oReqSpec.given().baseUri(getBaseURL()).basePath(sURI);
		QueryableRequestSpecification queryRequest = SpecificationQuerier.query(oReqSpec);
		log.debug("GET {}", queryRequest.getURI());
        return oReqSpec.get();
	}
	
	protected static Response myPost(RequestSpecification oReqSpec, String sURI) {

		oReqSpec.given().baseUri(getBaseURL()).basePath(sURI);
		QueryableRequestSpecification queryRequest = SpecificationQuerier.query(oReqSpec);
		log.debug("POST {}", queryRequest.getURI());
		log.debug(queryRequest.getBody().toString());
		return oReqSpec.post();
	}

	protected static Response myPut(RequestSpecification oReqSpec, String sURI) {
		oReqSpec.given().baseUri(getBaseURL()).basePath(sURI);
		QueryableRequestSpecification queryRequest = SpecificationQuerier.query(oReqSpec);
		log.debug("PUT {}", queryRequest.getURI());
		return oReqSpec.put();
	}

	@SneakyThrows({NoSuchAlgorithmException.class, IOException.class})
	public static String getSHA256(File file) {
		FileInputStream oFIS = new FileInputStream(file);
		byte[] baFile = oFIS.readAllBytes();
		oFIS.close();
		MessageDigest md = MessageDigest.getInstance("SHA256");
		md.update(baFile);
		byte[] digest = md.digest();
		return Base64.getEncoder().encodeToString(digest);
	}

	@SneakyThrows({NoSuchAlgorithmException.class, IOException.class})
	public static String getMD5(File file) {
		FileInputStream oFIS = new FileInputStream(file);
		byte[] baFile = oFIS.readAllBytes();
		oFIS.close();
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(baFile);
		byte[] digest = md.digest();
		return Base64.getEncoder().encodeToString(digest);
	}

	public static String getValueOrDefault(Map<String, String> map, String key, String defaultValue) {
		try {
			String value = map.get(key);
			if (value == null) {
				return defaultValue;
			}
			return value;
		} catch (NullPointerException e) {
			return defaultValue;
		}
	}

}
