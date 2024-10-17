package it.pagopa.pn.cucumber.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import it.pagopa.pn.configuration.Config;
import it.pagopa.pn.configuration.TestVariablesConfiguration;
import it.pagopa.pn.cucumber.dto.pojo.Checksum;
import it.pagopa.pn.cucumber.utils.CommonUtils;
import it.pagopa.pn.cucumber.utils.SafeStorageUtils;
import it.pagopa.pn.cucumber.poller.PnSsQueuePoller;
import it.pagopa.pn.safestorage.generated.openapi.server.v1.dto.*;
import jakarta.jms.JMSException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.slf4j.MDC;

import java.io.*;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.configuration.TestVariablesConfiguration.getValueIfTagged;
import static it.pagopa.pn.cucumber.utils.LogUtils.MDC_CORR_ID_KEY;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class SsStepDefinitions {
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
    private int statusCode = 0;
    File oFile = null;
    private String sPNClientUp = null;
    private String sPNClient_AKUp = null;
    private String status = null;
    private String retentionUntil = "";
    private Date retentionDate = null;
    private static String nomeCoda;
    private static final PnSsQueuePoller queuePoller;
    UpdateFileMetadataRequest requestBody = new UpdateFileMetadataRequest();
    private boolean metadataOnly;
    private FileDownloadResponse fileDownloadResponse;

    static {
        try {
            MDC.clear();
            Config.getInstance().loadProperties();
            queuePoller = new PnSsQueuePoller();
            queuePoller.startPolling();
        } catch (JMSException e) {
            throw new RuntimeException("Error initializing queue poller", e);
        }
    }

    @Given("the SafeStorage client {string} authenticated by {string}")
    public void clientAuthentication(String sPNClient, String sPNClient_AK) {
        this.sPNClient = getValueIfTagged(sPNClient);
        this.sPNClient_AK = getValueIfTagged(sPNClient_AK);
    }

    @Given("{string} authenticated by {string} try to upload a document of type {string} with content type {string} using {string}")
    public void a_file_to_upload(String sPNClient, String sPNClient_AK, String sDocumentType, String sMimeType, String sFileName) throws NoSuchAlgorithmException, IOException {

        sPNClient = getValueIfTagged(sPNClient);
        sPNClient_AK = getValueIfTagged(sPNClient_AK);
        sDocumentType = getValueIfTagged(sDocumentType);
        sMimeType = getValueIfTagged(sMimeType);
        sFileName = getValueIfTagged(sFileName);

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
    public void no_file_to_update(String sPNClientUp, String sPNClient_AKUp, String status, String retentionUntil, String fileKey) {

        sPNClientUp = getValueIfTagged(sPNClientUp);
        sPNClient_AKUp = getValueIfTagged(sPNClient_AKUp);
        status = getValueIfTagged(status);
        retentionUntil = getValueIfTagged(retentionUntil);
        fileKey = getValueIfTagged(fileKey);

        this.status = status;
        this.retentionUntil = retentionUntil;
        this.sPNClientUp = sPNClientUp;
        this.sPNClient_AKUp = sPNClient_AKUp;
        if (fileKey != null && !fileKey.isEmpty()) {
            this.sKey = fileKey;
            MDC.put(MDC_CORR_ID_KEY, fileKey);
        } else {
            this.sKey = "";
        }
        Response oResp;

        if (retentionUntil != null && !retentionUntil.isEmpty()) {
            requestBody.setRetentionUntil(Date.from(Instant.parse(retentionUntil)));
        }
        requestBody.setStatus(status);

        oResp = SafeStorageUtils.updateObjectMetadata(sPNClientUp, sPNClient_AKUp, fileKey, requestBody);
        iRC = oResp.getStatusCode();
    }

    @When("{string} authenticated by {string} try to update the document using {string} and {string}")
    public void a_file_to_update(String sPNClientUp, String sPNClient_AKUp, String status, String retentionUntil) {

        sPNClientUp = getValueIfTagged(sPNClientUp);
        sPNClient_AKUp = getValueIfTagged(sPNClient_AKUp);
        status = getValueIfTagged(status);
        retentionUntil = getValueIfTagged(retentionUntil);


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

        oResp = SafeStorageUtils.updateObjectMetadata(sPNClientUp, sPNClient_AKUp, sKey, requestBody);
        iRC = oResp.getStatusCode();
    }

    @When("request a presigned url to upload the file")
    public void getUploadPresignedURL() throws JsonProcessingException {
        Response oResp;
        FileCreationRequest fileCreationRequest = new FileCreationRequest().contentType(sMimeType).documentType(sDocumentType).status("SAVED");
        oResp = SafeStorageUtils.getPresignedURLUpload(sPNClient, sPNClient_AK, fileCreationRequest, sSHA256, sMD5, boHeader, Checksum.SHA256, true);
        iRC = oResp.getStatusCode();
        if (iRC == 200) {
            sURL = oResp.then().extract().path("uploadUrl");
            sKey = oResp.then().extract().path("key");
            sSecret = oResp.then().extract().path("secret");
        }
    }

    @When("request a presigned url to upload the file with {string}")
    public void getUploadPresignedURLWithTagAndValue(String tag) {
        tag = getValueIfTagged(tag);
        Response oResp;
        var tags = Map.of(tag, List.of("test-value" + randomAlphanumeric(5)));
        FileCreationRequest fileCreationRequest = new FileCreationRequest().contentType(sMimeType).documentType(sDocumentType).status("SAVED").tags(tags);
        oResp = SafeStorageUtils.getPresignedURLUpload(sPNClient, sPNClient_AK, fileCreationRequest, sSHA256, sMD5, boHeader, Checksum.SHA256, true);
        iRC = oResp.getStatusCode();
        Assertions.assertEquals(200, iRC);
        if (iRC == 200) {
            sURL = oResp.then().extract().path("uploadUrl");
            sKey = oResp.then().extract().path("key");
            sSecret = oResp.then().extract().path("secret");
        }
    }

    @When("request a presigned url to upload the file without traceId")
    public void getUploadPresignedURLWithoutTraceId() {
        Response oResp;
        FileCreationRequest fileCreationRequest = new FileCreationRequest().contentType(sMimeType).documentType(sDocumentType).status("SAVED");
        oResp = SafeStorageUtils.getPresignedURLUpload(sPNClient, sPNClient_AK, fileCreationRequest, sSHA256, sMD5, boHeader, Checksum.SHA256, false);
        iRC = oResp.getStatusCode();
        if (iRC == 200) {
            sURL = oResp.then().extract().path("uploadUrl");
            sKey = oResp.then().extract().path("key");
            sSecret = oResp.then().extract().path("secret");
        }
    }


    @When("upload that file")
    public void uploadFile() {
        Assertions.assertNotNull(sURL);
        iRC = CommonUtils.uploadFile(sURL, oFile, sSHA256, sMD5, sMimeType, sSecret, Checksum.SHA256).getStatusCode();
    }

    @When("it's available")
    public void it_s_available() throws JsonProcessingException, InterruptedException {
        Response oResp;
        iRC = 0;
        //Set a time limit for the availability check.
        Instant timeLimit = Instant.now().plusSeconds(Long.parseLong(System.getProperty("pn.ss.document.availability.timeout.millis")));
        boolean hasBeenFound = false;
        //Check if the document is available every x seconds.
        //Time limit represent a timeout for the check.
        while (Instant.now().isBefore(timeLimit)) {
            oResp = SafeStorageUtils.getDocument(sKey);
            iRC = oResp.getStatusCode();
            if (iRC == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                log.debug(oResp.getBody().asString());
                DocumentResponse oFDR = objectMapper.readValue(oResp.getBody().asString(), DocumentResponse.class);
                Document document = oFDR.getDocument();
                assert document != null;
                assert document.getDocumentState() != null;
                //If the document is available, exit the loop.
                if (document.getDocumentState().equalsIgnoreCase("available")) {
                    hasBeenFound = true;
                    break;
                }
            }
            Thread.sleep(Long.parseLong(System.getProperty("pn.ss.document.availability.interval.millis")));
        }
        //If the document is not available after the timeout, the test will fail.
        Assertions.assertTrue(hasBeenFound);
    }

    @Then("i found in S3")
    public void i_found_in_s3() {
        Assertions.assertEquals(200, SafeStorageUtils.getPresignedURLDownload(sPNClient, sPNClient_AK, sKey, false).getStatusCode());// Ok
        statusCode = 200;
    }


    @And("i check availability message {string}")
    public void i_check_availability_messages(String sRC) {
        int sCode;
        boolean check = queuePoller.checkMessageAvailability(sKey);
        if (!check) {
            sCode = 404;
            log.info("Message not found for key {}", sKey);
        } else {
            sCode = 200;
            log.debug("Message found for key {}", sKey);
        }
        Assertions.assertEquals(Integer.parseInt(sRC), sCode);
    }

    @Then("i get an error {string}")
    public void i_get_an_error(String sRC) {
        Assertions.assertEquals(Integer.parseInt(sRC), iRC);

    }


    @Then("i check that the document got updated")
    public void metadata_changed() throws JsonProcessingException, InterruptedException {
        //Check if the previous updateMetadata request has been successful.
        Assertions.assertEquals(200, iRC);
        //Check if the document in DynamoDB has been updated.
        Response oResp;
        statusCode = 0;
        while (statusCode != 200) {
            oResp = SafeStorageUtils.getObjectMetadata(sPNClientUp, sPNClient_AKUp, sKey);
            statusCode = oResp.getStatusCode();
            if (statusCode == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                FileDownloadResponse oFDR = objectMapper.readValue(oResp.getBody().asString(), FileDownloadResponse.class);

                boolean condition = false;

                if (retentionUntil != null && !retentionUntil.isEmpty()) {
                    retentionDate = Date.from(Instant.parse(retentionUntil));
                    if (oFDR.getRetentionUntil().toInstant().truncatedTo(ChronoUnit.SECONDS).equals(retentionDate.toInstant().truncatedTo(ChronoUnit.SECONDS))) {
                        condition = true;
                    }
                }

                if (oFDR.getDocumentStatus().equalsIgnoreCase(status)) {
                    condition = true;
                }
                assertTrue(condition);

            }
            Thread.sleep(3000);
        }
    }


    @Given("{string} authenticated by {string} try to get a file with key {string} and metadataOnly as {string}")
    public void getPresignedUrlByFileKey(String sPNClient, String sPNClient_AK, String fileKey, String metadataOnly) {
        this.sPNClient = getValueIfTagged(sPNClient);
        this.sPNClient_AK = getValueIfTagged(sPNClient_AK);
        this.sKey = fileKey;
        MDC.put(MDC_CORR_ID_KEY, fileKey);
        this.metadataOnly = Boolean.parseBoolean(metadataOnly);

    }

    @When("request a presigned url to download the file")
    public void requestAPresignedUrlToDownloadTheFile() {
        Response response = SafeStorageUtils.getPresignedURLDownload(sPNClient, sPNClient_AK, sKey, metadataOnly);
        this.statusCode = response.getStatusCode();
        if (statusCode == 200) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                this.fileDownloadResponse = objectMapper.readValue(response.getBody().asString(), FileDownloadResponse.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Then("i get that presigned url")
    public void iGetThatPresignedUrl() {
        log.debug("fileDownloadResponse {}", fileDownloadResponse);
        Assertions.assertEquals(200, statusCode);
        Assertions.assertNotNull(fileDownloadResponse);
        Assertions.assertNotNull(fileDownloadResponse.getDownload());
    }

    @Then("i get file metadata")
    public void iGetFileMetadata() {
        log.debug("fileDownloadResponse {}", fileDownloadResponse);
        Assertions.assertEquals(200, statusCode);
        Assertions.assertNotNull(fileDownloadResponse);
        Assertions.assertNull(fileDownloadResponse.getDownload());
    }

    @Given("a document with fileKey {string}")
    public void aFileKey(String fileKey) {
        this.sKey = fileKey;
        MDC.put(MDC_CORR_ID_KEY, fileKey);
    }

    @When("I get documents configs")
    public void iGetDocumentsConfigs() {
        Response response = SafeStorageUtils.getDocumentsConfigs(sPNClient, sPNClient_AK);
        this.statusCode = response.getStatusCode();
        if (statusCode == 200) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                DocumentTypesConfigurations documentTypesConfigurations = objectMapper.readValue(response.getBody().asString(), DocumentTypesConfigurations.class);
                log.debug("DocumentTypesConfigurations {}", documentTypesConfigurations);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @When("I get current client config")
    public void iGetCurrentClientConfig() {
        Response response = SafeStorageUtils.getCurrentClientConfig(sPNClient, sPNClient_AK);
        this.statusCode = response.getStatusCode();
        if (statusCode == 200) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                UserConfiguration userConfiguration = objectMapper.readValue(response.getBody().asString(), UserConfiguration.class);
                log.debug("UserConfiguration: {}", userConfiguration);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Then("I get {string} statusCode")
    public void iGetStatusCode(String statusCode) {
        Assertions.assertEquals(Integer.parseInt(statusCode), this.statusCode);
    }


    @AfterAll
    public static void doFinally() throws JMSException {
        queuePoller.close();
    }

}
