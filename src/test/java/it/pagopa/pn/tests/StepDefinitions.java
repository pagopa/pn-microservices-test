package it.pagopa.pn.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import it.pagopa.pn.configuration.TestVariablesConfiguration;
import it.pagopa.pn.cucumber.*;
import it.pagopa.pn.cucumber.Checksum;
import it.pagopa.pn.cucumber.CommonUtils;
import it.pagopa.pn.cucumber.SafeStorageUtils;
import it.pagopa.pn.safestorage.generated.openapi.server.v1.dto.FileDownloadResponse;
import it.pagopa.pn.safestorage.generated.openapi.server.v1.dto.UpdateFileMetadataRequest;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

import static it.pagopa.pn.cucumber.SqsUtils.checkIfDocumentIsAvailable;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CustomLog
public class StepDefinitions {
    private String sPNClient = null;
    private String sPNClient_AK = null;
    private String sDocumentType = null;
    private String sSHA256 = null;
    private String sMD5 = null;
    private static boolean boHeader = true;
    private String sURL = null;
    private String sKey = null;
    private String sSecret = null;
    private String sMimeType = null;
    private int iRC = 0;
    File oFile = null;
    private String sPNClientUp = null;
    private String sPNClient_AKUp = null;
    private String status = null;
    private String retentionUntil = "";
    private Date retentionDate = null;
    private static String nomeCoda;
    UpdateFileMetadataRequest requestBody = new UpdateFileMetadataRequest();


    @BeforeAll
    public static void loadPropertiesForQueue() {
        nomeCoda = Config.getInstance().getNomeCoda();
    }

    @Given("{string} authenticated by {string} try to upload a document of type {string} with content type {string} using {string}")
    public void a_file_to_upload(String sPNClient, String sPNClient_AK, String sDocumentType, String sMimeType, String sFileName) throws NoSuchAlgorithmException, FileNotFoundException, IOException {

        sPNClient = parseIfTagged(sPNClient);
        sPNClient_AK = parseIfTagged(sPNClient_AK);
        sDocumentType = parseIfTagged(sDocumentType);
        sMimeType = parseIfTagged(sMimeType);
        sFileName = parseIfTagged(sFileName);


        Path pathFile = Paths.get(sFileName).toAbsolutePath();

        this.sPNClient = sPNClient;
        this.sPNClient_AK = sPNClient_AK;
        this.sDocumentType = sDocumentType;
        this.sMimeType = sMimeType;

        oFile = new File(sFileName);
        FileInputStream oFIS = new FileInputStream(oFile);
        byte[] baFile = oFIS.readAllBytes();
        oFIS.close();
        MessageDigest md = MessageDigest.getInstance("SHA256");
        md.update(baFile);
        byte[] digest = md.digest();
        sSHA256 = Base64.getEncoder().encodeToString(digest);

        md = MessageDigest.getInstance("MD5");
        md.update(baFile);
        digest = md.digest();
        sMD5 = Base64.getEncoder().encodeToString(digest);
    }

	@Given("{string} authenticated by {string} try to update the document using {string} and {string} but has invalid or null {string}")
	public void no_file_to_update (String sPNClientUp, String sPNClient_AKUp, String status, String retentionUntil, String fileKey) {

        sPNClientUp = parseIfTagged(sPNClientUp);
        sPNClient_AKUp = parseIfTagged(sPNClient_AKUp);
        status = parseIfTagged(status);
        retentionUntil = parseIfTagged(retentionUntil);
        fileKey = parseIfTagged(fileKey);

        this.status = status;
        this.retentionUntil = retentionUntil;
        this.sPNClientUp = sPNClientUp;
        this.sPNClient_AKUp = sPNClient_AKUp;
        if (fileKey != null && !fileKey.isEmpty()) {
            this.sKey = fileKey;
        } else {
            this.sKey = "";
        }

        log.debug("Client utilizzato: " + sPNClientUp);

        Response oResp;

        if (retentionUntil != null && !retentionUntil.isEmpty()) {
            requestBody.setRetentionUntil(Date.from(Instant.parse(retentionUntil)));
        }
        requestBody.setStatus(status);

        CommonUtils.checkDump(oResp = SafeStorageUtils.updateObjectMetadata(sPNClientUp, sPNClient_AKUp, fileKey, requestBody), true);
        iRC = oResp.getStatusCode();
        log.debug("file key: " + fileKey);
        log.debug("new status: " + status);
        log.debug("new retentionUntil: " + retentionUntil);
    }

    @When("{string} authenticated by {string} try to update the document just uploaded using {string} and {string}")
    public void a_file_to_update(String sPNClientUp, String sPNClient_AKUp, String status, String retentionUntil) {

        sPNClientUp = parseIfTagged(sPNClientUp);
        sPNClient_AKUp = parseIfTagged(sPNClient_AKUp);
        status = parseIfTagged(status);
        retentionUntil = parseIfTagged(retentionUntil);

        this.status = status;
        this.retentionUntil = retentionUntil;
        this.sPNClientUp = sPNClientUp;
        this.sPNClient_AKUp = sPNClient_AKUp;

        log.debug("client: " + sPNClientUp);

        Response oResp;

        if (retentionUntil != null && !retentionUntil.isEmpty()) {
            requestBody.setRetentionUntil(Date.from(Instant.parse(retentionUntil)));
        }
        requestBody.setStatus(status);

        CommonUtils.checkDump(oResp = SafeStorageUtils.updateObjectMetadata(sPNClientUp, sPNClient_AKUp, sKey, requestBody), true);
        iRC = oResp.getStatusCode();
        log.debug("new status: " + status);
        log.debug("new retentionUntil: " + retentionUntil);

    }

    @When("request a presigned url to upload the file")
    public void getUploadPresignedURL() {
        Response oResp;

        CommonUtils.checkDump(oResp = SafeStorageUtils.getPresignedURLUpload(sPNClient, sPNClient_AK, sMimeType, sDocumentType, sSHA256, sMD5, "SAVED", boHeader, Checksum.SHA256), true);

        iRC = oResp.getStatusCode();
        log.debug("oResp body: " + oResp.getBody().asString());
        log.debug("oResp uploadUrl: " + oResp.then().extract().path("uploadUrl"));
        log.info("file key: " + oResp.then().extract().path("key"));
        log.debug("oResp secret: " + oResp.then().extract().path("secret"));
        log.debug("iRC: " + iRC);
        if (iRC == 200) {
            sURL = oResp.then().extract().path("uploadUrl");
            sKey = oResp.then().extract().path("key");
            sSecret = oResp.then().extract().path("secret");
        }
    }

    @When("request a presigned url to upload the file without traceId")
    public void getUploadPresignedURLWithoutTraceId() {
        Response oResp;

        CommonUtils.checkDump(oResp = SafeStorageUtils.getPresignedURLUploadKo(sPNClient, sPNClient_AK, sMimeType, sDocumentType, sSHA256, sMD5, "SAVED", boHeader, Checksum.SHA256), true);
        log.debug("CLIENT: " + sPNClient);

        iRC = oResp.getStatusCode();
        log.debug("oResp body: " + oResp.getBody().asString());
        log.debug("oResp uploadUrl: " + oResp.then().extract().path("uploadUrl"));
        log.info("file key: " + oResp.then().extract().path("key"));
        log.debug("oResp secret: " + oResp.then().extract().path("secret"));
        log.debug("iRC: " + iRC);
        if (iRC == 200) {
            sURL = oResp.then().extract().path("uploadUrl");
            sKey = oResp.then().extract().path("key");
            sSecret = oResp.then().extract().path("secret");
        }
    }


    @When("upload that file")
    public void uploadFile() throws MalformedURLException, UnsupportedEncodingException {
        log.debug("sURL: " + sURL);
        Assertions.assertNotNull(sURL);
        iRC = CommonUtils.uploadFile(sURL, oFile, sSHA256, sMD5, sMimeType, sSecret, Checksum.SHA256).getStatusCode();
    }

    @When("it's available")
    public void it_s_available() throws JsonProcessingException, InterruptedException {
        Response oResp;
        iRC = 0;
        while (iRC != 200) {
            oResp = SafeStorageUtils.getObjectMetadata(sPNClient, sPNClient_AK, sKey);
            iRC = oResp.getStatusCode();
            if (iRC == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                log.debug(oResp.getBody().asString());
                FileDownloadResponse oFDR = objectMapper.readValue(oResp.getBody().asString(), FileDownloadResponse.class);
                if (oFDR.getDocumentStatus().equalsIgnoreCase("SAVED") || oFDR.getDocumentStatus().equalsIgnoreCase("PRELOADED")) {
                    break;
                }
            }
            Thread.sleep(3000);
        }
    }

    @Then("i found in S3")
    public void i_found_in_s3() {
        Assertions.assertEquals(200, CommonUtils.checkDump(SafeStorageUtils.getPresignedURLDownload(sPNClient, sPNClient_AK, sKey), true)); // Ok
    }

    @And("no availability messages")
    public void no_availability_messages() {
        Assertions.assertFalse(checkIfDocumentIsAvailable(sKey, nomeCoda));
    }

    @And("i check availability message")
    public void i_check_availability_messages() {
        Assertions.assertTrue(checkIfDocumentIsAvailable(sKey, nomeCoda));
    }

    @Then("i get an error {string}")
    public void i_get_an_error(String sRC) {
        Assertions.assertEquals(Integer.parseInt(sRC), iRC);

    }

    @Then("i check that the document got updated")
    public void metadata_changed() throws JsonMappingException, JsonProcessingException, InterruptedException {

        Response oResp;
        iRC = 0;
        while (iRC != 200) {
            oResp = SafeStorageUtils.getObjectMetadata(sPNClientUp, sPNClient_AKUp, sKey);
            iRC = oResp.getStatusCode();
            if (iRC == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                log.debug("response body: " + oResp.getBody().asString());
                FileDownloadResponse oFDR = objectMapper.readValue(oResp.getBody().asString(), FileDownloadResponse.class);
                log.debug("file download response: " + oFDR);

                boolean condition = false;

                if (retentionUntil != null && !retentionUntil.isEmpty()) {
                    retentionDate = Date.from(Instant.parse(retentionUntil));
                    if (oFDR.getRetentionUntil().toInstant().truncatedTo(ChronoUnit.SECONDS).equals(retentionDate.toInstant().truncatedTo(ChronoUnit.SECONDS))) {
                        condition = true;
                    }
                }

                log.debug("retentionDate: " + retentionDate);
                log.debug("status: " + status);

                if (oFDR.getDocumentStatus().equalsIgnoreCase(status)) {
                    condition = true;
                }
                assertTrue(condition);

            }
            Thread.sleep(3000);
        }
    }


    @After
    public void doFinally() throws IOException {
    }

    private String parseIfTagged(String value) {
        return TestVariablesConfiguration.getInstance().getValueIfTagged(value);
    }


}
