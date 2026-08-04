package it.pagopa.pn.cucumber.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import it.pagopa.pn.configuration.Config;
import it.pagopa.pn.cucumber.dto.pojo.Checksum;
import it.pagopa.pn.cucumber.utils.CommonUtils;
import it.pagopa.pn.cucumber.utils.S3Utils;
import it.pagopa.pn.cucumber.utils.SafeStorageUtils;
import it.pagopa.pn.cucumber.poller.PnSsQueuePoller;
import it.pagopa.pn.safestorage.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.S3Service;
import it.pagopa.pn.service.impl.S3ServiceImpl;
import it.pagopa.pn.service.impl.SqsServiceImpl;
import jakarta.jms.JMSException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.slf4j.MDC;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotification;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static it.pagopa.pn.configuration.TestVariablesConfiguration.getValueIfTagged;
import static it.pagopa.pn.cucumber.utils.LogUtils.MDC_CORR_ID_KEY;
import static it.pagopa.pn.cucumber.utils.S3Utils.OBJECT_RESTORE_COMPLETED;
import static it.pagopa.pn.cucumber.utils.SqsUtils.*;
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
    private static PnSsQueuePoller queuePoller;
    private final SqsServiceImpl sqsService = new SqsServiceImpl();
    private final S3Service s3Service = new S3ServiceImpl();
    UpdateFileMetadataRequest requestBody = new UpdateFileMetadataRequest();
    private boolean metadataOnly;
    private FileDownloadResponse fileDownloadResponse;
    public static final String TRANSFORMATION_TAG_PREFIX = "Transformation-";
    private final Map<String, String> fileKeysByAlias = new HashMap<>();
    private final Map<String, Map<String, String>> tagValuesByAlias = new HashMap<>();
    private Response searchResponse;
    private AdditionalFileTagsSearchResponse searchResult;
    private Response massiveResponse;
    private AdditionalFileTagsMassiveUpdateResponse massiveResult;
    private Response getTagsResponse;
    private AdditionalFileTagsGetResponse getTagsResult;


    @BeforeAll
    public static void init() {
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

        log.info("FILENAME:{} ", sFileName);
        System.out.println("FILENAME: "+ sFileName);
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
            MDC.put(MDC_CORR_ID_KEY, sKey);
            sSecret = oResp.then().extract().path("secret");
        }
        System.out.println("KEY: "+ sKey);
    }

    @When("request a presigned url to upload the file with {string}")
    public void getUploadPresignedURLWithTagAndValue(String tag) {
        tag = getValueIfTagged(tag);
        requestPresignedUrlWithTags(Map.of(tag, List.of("test-value" + randomAlphanumeric(5))));
    }

    private void requestPresignedUrlWithTags(Map<String, List<String>> tags) {
        FileCreationRequest fileCreationRequest = new FileCreationRequest().contentType(sMimeType).documentType(sDocumentType).status("SAVED").tags(tags);
        Response oResp = SafeStorageUtils.getPresignedURLUpload(sPNClient, sPNClient_AK, fileCreationRequest, sSHA256, sMD5, boHeader, Checksum.SHA256, true);
        iRC = oResp.getStatusCode();
        Assertions.assertEquals(200, iRC);
        if (iRC == 200) {
            sURL = oResp.then().extract().path("uploadUrl");
            sKey = oResp.then().extract().path("key");
            sSecret = oResp.then().extract().path("secret");
        }
    }

    private void uploadAndIndexDocument(String sPNClient, String sPNClient_AK, String sDocumentType, Map<String, String> tagBaseValues, String alias) throws NoSuchAlgorithmException, IOException, JsonProcessingException, InterruptedException {
        sPNClient = getValueIfTagged(sPNClient);
        sPNClient_AK = getValueIfTagged(sPNClient_AK);
        sDocumentType = getValueIfTagged(sDocumentType);

        a_file_to_upload(sPNClient, sPNClient_AK, sDocumentType, "application/pdf", "src/main/resources/test.pdf");

        Map<String, List<String>> tags = new HashMap<>();
        Map<String, String> resolvedValues = new HashMap<>();
        for (Map.Entry<String, String> entry : tagBaseValues.entrySet()) {
            String uniqueValue = entry.getValue() + randomAlphanumeric(8);
            tags.put(entry.getKey(), List.of(uniqueValue));
            resolvedValues.put(entry.getKey(), uniqueValue);
        }

        requestPresignedUrlWithTags(tags);
        uploadFile();
        it_s_available_ss();

        fileKeysByAlias.put(alias, sKey);
        tagValuesByAlias.put(alias, resolvedValues);
    }

    @Given("{string} authenticated by {string} upload and index a document of type {string} with tag {string} value {string} as {string}")
    public void uploadAndIndexDocumentWithTagValueAsAlias(String sPNClient, String sPNClient_AK, String sDocumentType, String tag, String value, String alias) throws NoSuchAlgorithmException, IOException, JsonProcessingException, InterruptedException {
        tag = getValueIfTagged(tag);
        value = getValueIfTagged(value);
        uploadAndIndexDocument(sPNClient, sPNClient_AK, sDocumentType, Map.of(tag, value), alias);
    }

    @Given("{string} authenticated by {string} upload and index a document of type {string} with tags as {string}")
    public void uploadAndIndexDocumentWithTagsAsAlias(String sPNClient, String sPNClient_AK, String sDocumentType, String alias, DataTable dataTable) throws NoSuchAlgorithmException, IOException, JsonProcessingException, InterruptedException {
        Map<String, String> tagBaseValues = new HashMap<>();
        for (Map<String, String> row : dataTable.asMaps()) {
            tagBaseValues.put(getValueIfTagged(row.get("tag")), getValueIfTagged(row.get("value")));
        }
        uploadAndIndexDocument(sPNClient, sPNClient_AK, sDocumentType, tagBaseValues, alias);
    }

    private void executeSearch(Map<String, String> tagParams, String logic, Boolean withTags) {
        searchResponse = SafeStorageUtils.searchFileTags(sPNClient, sPNClient_AK, tagParams, logic, withTags);
        iRC = searchResponse.getStatusCode();
        searchResult = null;
        if (iRC == 200) {
            try {
                searchResult = new ObjectMapper().readValue(searchResponse.getBody().asString(), AdditionalFileTagsSearchResponse.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @When("search files using tag {string} value from {string}")
    public void searchFilesUsingTagValueFrom(String tag, String alias) {
        tag = getValueIfTagged(tag);
        Map<String, String> tagParams = new HashMap<>();
        tagParams.put(tag, tagValuesByAlias.get(alias).get(tag));
        executeSearch(tagParams, null, false);
    }

    @When("search files using tag {string} value from {string} including tags in response")
    public void searchFilesUsingTagValueFromIncludingTagsInResponse(String tag, String alias) {
        tag = getValueIfTagged(tag);
        Map<String, String> tagParams = new HashMap<>();
        tagParams.put(tag, tagValuesByAlias.get(alias).get(tag));
        executeSearch(tagParams, null, true);
    }

    @When("search files using tags {string} from {string}")
    public void searchFilesUsingTagsFrom(String tagsCsv, String alias) {
        Map<String, String> tagParams = new HashMap<>();
        for (String tagToken : tagsCsv.split(",")) {
            String tag = getValueIfTagged(tagToken.trim());
            tagParams.put(tag, tagValuesByAlias.get(alias).get(tag));
        }
        executeSearch(tagParams, null, false);
    }

    @When("search files using tag {string} value from {string} and non matching tag {string}")
    public void searchFilesUsingTagValueFromAndNonMatchingTag(String tag, String alias, String nonMatchingTag) {
        tag = getValueIfTagged(tag);
        nonMatchingTag = getValueIfTagged(nonMatchingTag);
        Map<String, String> tagParams = new HashMap<>();
        tagParams.put(tag, tagValuesByAlias.get(alias).get(tag));
        tagParams.put(nonMatchingTag, "non-matching-value-" + randomAlphanumeric(8));
        executeSearch(tagParams, null, false);
    }

    @When("search files using tag {string} value from {string} and non matching tag {string} with logic {string}")
    public void searchFilesUsingTagValueFromAndNonMatchingTagWithLogic(String tag, String alias, String nonMatchingTag, String logic) {
        tag = getValueIfTagged(tag);
        nonMatchingTag = getValueIfTagged(nonMatchingTag);
        logic = getValueIfTagged(logic);
        Map<String, String> tagParams = new HashMap<>();
        tagParams.put(tag, tagValuesByAlias.get(alias).get(tag));
        tagParams.put(nonMatchingTag, "non-matching-value-" + randomAlphanumeric(8));
        executeSearch(tagParams, logic, false);
    }

    @When("search files with {int} tag params")
    public void searchFilesWithTagParams(int numTags) {
        Map<String, String> tagParams = new HashMap<>();
        for (int i = 0; i < numTags; i++) {
            tagParams.put("searchLimitTag" + i, "search-limit-value" + randomAlphanumeric(5));
        }
        executeSearch(tagParams, null, false);
    }

    @When("search files with no tag params")
    public void searchFilesWithNoTagParams() {
        executeSearch(new HashMap<>(), null, false);
    }

    @Then("the search response contains fileKey alias {string}")
    public void theSearchResponseContainsFileKeyAlias(String alias) {
        Assertions.assertNotNull(searchResult);
        Assertions.assertNotNull(searchResult.getFileKeys());
        String expectedFileKey = fileKeysByAlias.get(alias);
        Assertions.assertTrue(searchResult.getFileKeys().stream().anyMatch(entry -> entry.getFileKey().equals(expectedFileKey)));
    }

    @Then("the search response is empty")
    public void theSearchResponseIsEmpty() {
        Assertions.assertNotNull(searchResult);
        Assertions.assertTrue(searchResult.getFileKeys() == null || searchResult.getFileKeys().isEmpty());
    }

    @Then("the search response fileKey alias {string} has tags without local prefix")
    public void theSearchResponseFileKeyAliasHasTagsWithoutLocalPrefix(String alias) {
        Assertions.assertNotNull(searchResult);
        Assertions.assertNotNull(searchResult.getFileKeys());
        String expectedFileKey = fileKeysByAlias.get(alias);
        var matchingEntry = searchResult.getFileKeys().stream().filter(entry -> entry.getFileKey().equals(expectedFileKey)).findFirst();
        Assertions.assertTrue(matchingEntry.isPresent());
        Map<String, List<String>> tags = matchingEntry.get().getTags();
        Assertions.assertNotNull(tags);
        Assertions.assertFalse(tags.isEmpty());
        Assertions.assertTrue(tags.keySet().stream().noneMatch(k -> k.contains("~")));
    }

    @Given("{string} authenticated by {string} upload and index a document of type {string} as {string}")
    public void uploadAndIndexDocumentWithoutTagsAsAlias(String sPNClient, String sPNClient_AK, String sDocumentType, String alias) throws NoSuchAlgorithmException, IOException, JsonProcessingException, InterruptedException {
        uploadAndIndexDocument(sPNClient, sPNClient_AK, sDocumentType, new HashMap<>(), alias);
    }

    @When("set tag {string} value {string} on document alias {string}")
    public void setTagValueOnDocumentAlias(String tag, String value, String alias) {
        tag = getValueIfTagged(tag);
        String uniqueValue = getValueIfTagged(value) + randomAlphanumeric(8);
        AdditionalFileTagsUpdateRequest request = new AdditionalFileTagsUpdateRequest().SET(Map.of(tag, List.of(uniqueValue)));
        Response response = SafeStorageUtils.updateFileTags(sPNClient, sPNClient_AK, fileKeysByAlias.get(alias), request);
        iRC = response.getStatusCode();
        tagValuesByAlias.computeIfAbsent(alias, k -> new HashMap<>()).put(tag, uniqueValue);
    }

    @When("delete tag {string} on document alias {string}")
    public void deleteTagOnDocumentAlias(String tag, String alias) {
        tag = getValueIfTagged(tag);
        String value = tagValuesByAlias.get(alias).get(tag);
        AdditionalFileTagsUpdateRequest request = new AdditionalFileTagsUpdateRequest().DELETE(Map.of(tag, List.of(value)));
        Response response = SafeStorageUtils.updateFileTags(sPNClient, sPNClient_AK, fileKeysByAlias.get(alias), request);
        iRC = response.getStatusCode();
    }

    @When("set tag {string} and delete tag {string} on document alias {string}")
    public void setTagAndDeleteTagOnDocumentAlias(String setTag, String deleteTag, String alias) {
        setTag = getValueIfTagged(setTag);
        deleteTag = getValueIfTagged(deleteTag);
        String setValue = "update-mix-set-value-" + randomAlphanumeric(8);
        String deleteValue = tagValuesByAlias.get(alias).get(deleteTag);
        AdditionalFileTagsUpdateRequest request = new AdditionalFileTagsUpdateRequest()
                .SET(Map.of(setTag, List.of(setValue)))
                .DELETE(Map.of(deleteTag, List.of(deleteValue)));
        Response response = SafeStorageUtils.updateFileTags(sPNClient, sPNClient_AK, fileKeysByAlias.get(alias), request);
        iRC = response.getStatusCode();
        tagValuesByAlias.computeIfAbsent(alias, k -> new HashMap<>()).put(setTag, setValue);
    }

    @When("set and delete the same tag {string} on document alias {string} expecting failure")
    public void setAndDeleteTheSameTagOnDocumentAliasExpectingFailure(String tag, String alias) {
        tag = getValueIfTagged(tag);
        AdditionalFileTagsUpdateRequest request = new AdditionalFileTagsUpdateRequest()
                .SET(Map.of(tag, List.of("conflict-set-value-" + randomAlphanumeric(5))))
                .DELETE(Map.of(tag, List.of("conflict-delete-value-" + randomAlphanumeric(5))));
        Response response = SafeStorageUtils.updateFileTags(sPNClient, sPNClient_AK, fileKeysByAlias.get(alias), request);
        iRC = response.getStatusCode();
    }

    @When("set tag {string} value {string} on document alias {string} expecting failure")
    public void setTagValueOnDocumentAliasExpectingFailure(String tag, String value, String alias) {
        tag = getValueIfTagged(tag);
        value = getValueIfTagged(value);
        AdditionalFileTagsUpdateRequest request = new AdditionalFileTagsUpdateRequest().SET(Map.of(tag, List.of(value + randomAlphanumeric(5))));
        Response response = SafeStorageUtils.updateFileTags(sPNClient, sPNClient_AK, fileKeysByAlias.get(alias), request);
        iRC = response.getStatusCode();
    }

    @When("set tag {string} with {int} values on document alias {string} expecting failure")
    public void setTagWithValuesOnDocumentAliasExpectingFailure(String tag, int numValues, String alias) {
        tag = getValueIfTagged(tag);
        List<String> values = new ArrayList<>();
        for (int i = 0; i < numValues; i++) {
            values.add("v" + i + "-" + randomAlphanumeric(3));
        }
        AdditionalFileTagsUpdateRequest request = new AdditionalFileTagsUpdateRequest().SET(Map.of(tag, values));
        Response response = SafeStorageUtils.updateFileTags(sPNClient, sPNClient_AK, fileKeysByAlias.get(alias), request);
        iRC = response.getStatusCode();
    }

    @When("update with {int} operations on document alias {string} expecting failure")
    public void updateWithOperationsOnDocumentAliasExpectingFailure(int numOperations, String alias) {
        Map<String, List<String>> set = new HashMap<>();
        for (int i = 0; i < numOperations; i++) {
            set.put("updateLimitTag" + i, List.of("update-limit-value-" + randomAlphanumeric(5)));
        }
        AdditionalFileTagsUpdateRequest request = new AdditionalFileTagsUpdateRequest().SET(set);
        Response response = SafeStorageUtils.updateFileTags(sPNClient, sPNClient_AK, fileKeysByAlias.get(alias), request);
        iRC = response.getStatusCode();
    }

    @When("update tag {string} on a non existing fileKey expecting failure")
    public void updateTagOnANonExistingFileKeyExpectingFailure(String tag) {
        tag = getValueIfTagged(tag);
        String nonExistingFileKey = "non-existing-" + randomAlphanumeric(16);
        AdditionalFileTagsUpdateRequest request = new AdditionalFileTagsUpdateRequest().SET(Map.of(tag, List.of("update-value-" + randomAlphanumeric(5))));
        Response response = SafeStorageUtils.updateFileTags(sPNClient, sPNClient_AK, nonExistingFileKey, request);
        iRC = response.getStatusCode();
    }

    private void executeMassiveUpdate(List<Tags> tags) {
        AdditionalFileTagsMassiveUpdateRequest request = new AdditionalFileTagsMassiveUpdateRequest().tags(tags);
        massiveResponse = SafeStorageUtils.massiveUpdateFileTags(sPNClient, sPNClient_AK, request);
        iRC = massiveResponse.getStatusCode();
        massiveResult = null;
        if (iRC == 200) {
            try {
                massiveResult = new ObjectMapper().readValue(massiveResponse.getBody().asString(), AdditionalFileTagsMassiveUpdateResponse.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @When("massive set tag {string} value {string} on documents {string}")
    public void massiveSetTagValueOnDocuments(String tag, String value, String aliasesCsv) {
        tag = getValueIfTagged(tag);
        String uniqueValue = getValueIfTagged(value) + randomAlphanumeric(8);
        List<Tags> tags = new ArrayList<>();
        for (String aliasToken : aliasesCsv.split(",")) {
            String alias = aliasToken.trim();
            tags.add(new Tags().fileKey(fileKeysByAlias.get(alias)).SET(Map.of(tag, List.of(uniqueValue))));
            tagValuesByAlias.computeIfAbsent(alias, k -> new HashMap<>()).put(tag, uniqueValue);
        }
        executeMassiveUpdate(tags);
    }

    @When("massive delete tag {string} on documents {string}")
    public void massiveDeleteTagOnDocuments(String tag, String aliasesCsv) {
        tag = getValueIfTagged(tag);
        List<Tags> tags = new ArrayList<>();
        for (String aliasToken : aliasesCsv.split(",")) {
            String alias = aliasToken.trim();
            String value = tagValuesByAlias.get(alias).get(tag);
            tags.add(new Tags().fileKey(fileKeysByAlias.get(alias)).DELETE(Map.of(tag, List.of(value))));
        }
        executeMassiveUpdate(tags);
    }

    @When("massive request with duplicate file key alias {string} expecting failure")
    public void massiveRequestWithDuplicateFileKeyAliasExpectingFailure(String alias) {
        String tag = getValueIfTagged("@tag");
        String fileKey = fileKeysByAlias.get(alias);
        List<Tags> tags = new ArrayList<>();
        tags.add(new Tags().fileKey(fileKey).SET(Map.of(tag, List.of("massive-duplicate-value-" + randomAlphanumeric(5)))));
        tags.add(new Tags().fileKey(fileKey).SET(Map.of(tag, List.of("massive-duplicate-value-" + randomAlphanumeric(5)))));
        executeMassiveUpdate(tags);
    }

    @When("massive update with {int} file keys expecting failure")
    public void massiveUpdateWithFileKeysExpectingFailure(int numFileKeys) {
        String tag = getValueIfTagged("@tag");
        List<Tags> tags = new ArrayList<>();
        for (int i = 0; i < numFileKeys; i++) {
            String fileKey = "massive-limit-" + i + "-" + randomAlphanumeric(8);
            tags.add(new Tags().fileKey(fileKey).SET(Map.of(tag, List.of("massive-limit-value-" + randomAlphanumeric(5)))));
        }
        executeMassiveUpdate(tags);
    }

    @When("massive update tag {string} value {string} on document alias {string} and invalid tag {string} on document alias {string} expecting partial errors")
    public void massiveUpdateTagValueOnDocumentAliasAndInvalidTagOnDocumentAliasExpectingPartialErrors(String validTag, String value, String validAlias, String invalidTag, String invalidAlias) {
        validTag = getValueIfTagged(validTag);
        String uniqueValue = getValueIfTagged(value) + randomAlphanumeric(8);
        List<Tags> tags = new ArrayList<>();
        tags.add(new Tags().fileKey(fileKeysByAlias.get(validAlias)).SET(Map.of(validTag, List.of(uniqueValue))));
        tags.add(new Tags().fileKey(fileKeysByAlias.get(invalidAlias)).SET(Map.of(invalidTag, List.of("massive-partial-invalid-value-" + randomAlphanumeric(5)))));
        tagValuesByAlias.computeIfAbsent(validAlias, k -> new HashMap<>()).put(validTag, uniqueValue);
        executeMassiveUpdate(tags);
    }

    @Then("the massive response has {int} errors")
    public void theMassiveResponseHasErrors(int numErrors) {
        Assertions.assertNotNull(massiveResult);
        Assertions.assertNotNull(massiveResult.getErrors());
        Assertions.assertEquals(numErrors, massiveResult.getErrors().size());
    }

    @Then("the massive response has an error for fileKey alias {string}")
    public void theMassiveResponseHasAnErrorForFileKeyAlias(String alias) {
        Assertions.assertNotNull(massiveResult);
        Assertions.assertNotNull(massiveResult.getErrors());
        String expectedFileKey = fileKeysByAlias.get(alias);
        Assertions.assertTrue(massiveResult.getErrors().stream()
                .anyMatch(error -> error.getFileKey() != null && error.getFileKey().contains(expectedFileKey)));
    }

    @When("get the tags of document alias {string}")
    public void getTheTagsOfDocumentAlias(String alias) {
        getTagsResponse = SafeStorageUtils.getFileTags(sPNClient, sPNClient_AK, fileKeysByAlias.get(alias));
        iRC = getTagsResponse.getStatusCode();
        getTagsResult = null;
        if (iRC == 200) {
            try {
                getTagsResult = new ObjectMapper().readValue(getTagsResponse.getBody().asString(), AdditionalFileTagsGetResponse.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @When("get the tags of a non existing fileKey expecting failure")
    public void getTheTagsOfANonExistingFileKeyExpectingFailure() {
        String nonExistingFileKey = "non-existing-" + randomAlphanumeric(16);
        getTagsResponse = SafeStorageUtils.getFileTags(sPNClient, sPNClient_AK, nonExistingFileKey);
        iRC = getTagsResponse.getStatusCode();
        getTagsResult = null;
    }

    @Then("the document tags contain {string} with value from alias {string}")
    public void theDocumentTagsContainWithValueFromAlias(String tag, String alias) {
        tag = getValueIfTagged(tag);
        Assertions.assertNotNull(getTagsResult);
        Assertions.assertNotNull(getTagsResult.getTags());
        String expectedValue = tagValuesByAlias.get(alias).get(tag);
        Assertions.assertTrue(getTagsResult.getTags().containsKey(tag));
        Assertions.assertTrue(getTagsResult.getTags().get(tag).contains(expectedValue));
    }

    @Then("the document tags are empty")
    public void theDocumentTagsAreEmpty() {
        Assertions.assertNotNull(getTagsResult);
        Assertions.assertTrue(getTagsResult.getTags() == null || getTagsResult.getTags().isEmpty());
    }

    @Then("the document tags key {string} has no local prefix")
    public void theDocumentTagsKeyHasNoLocalPrefix(String tag) {
        tag = getValueIfTagged(tag);
        Assertions.assertNotNull(getTagsResult);
        Assertions.assertNotNull(getTagsResult.getTags());
        Assertions.assertTrue(getTagsResult.getTags().containsKey(tag));
        Assertions.assertTrue(getTagsResult.getTags().keySet().stream().noneMatch(k -> k.contains("~")));
    }

    @When("request a presigned url to upload the file with tag {string} expecting failure")
    public void getUploadPresignedURLWithTagExpectingFailure(String tag) {
        tag = getValueIfTagged(tag);
        Response oResp;
        var tags = Map.of(tag, List.of("test-value" + randomAlphanumeric(5)));
        FileCreationRequest fileCreationRequest = new FileCreationRequest().contentType(sMimeType).documentType(sDocumentType).status("SAVED").tags(tags);
        oResp = SafeStorageUtils.getPresignedURLUpload(sPNClient, sPNClient_AK, fileCreationRequest, sSHA256, sMD5, boHeader, Checksum.SHA256, true);
        iRC = oResp.getStatusCode();
    }

    @When("request a presigned url to upload the file with multi-value tag {string} expecting failure")
    public void getUploadPresignedURLWithMultiValueTagExpectingFailure(String tag) {
        tag = getValueIfTagged(tag);
        Response oResp;
        var tags = Map.of(tag, List.of("v1-" + randomAlphanumeric(3), "v2-" + randomAlphanumeric(3)));
        FileCreationRequest fileCreationRequest = new FileCreationRequest().contentType(sMimeType).documentType(sDocumentType).status("SAVED").tags(tags);
        oResp = SafeStorageUtils.getPresignedURLUpload(sPNClient, sPNClient_AK, fileCreationRequest, sSHA256, sMD5, boHeader, Checksum.SHA256, true);
        iRC = oResp.getStatusCode();
    }

    @When("request a presigned url to upload the file with {int} tags expecting failure")
    public void getUploadPresignedURLWithManyTagsExpectingFailure(int numTags) {
        Map<String, List<String>> tags = new HashMap<>();
        for (int i = 0; i < numTags; i++) {
            tags.put("limitTag" + i, List.of("test-value" + randomAlphanumeric(5)));
        }
        FileCreationRequest fileCreationRequest = new FileCreationRequest().contentType(sMimeType).documentType(sDocumentType).status("SAVED").tags(tags);
        Response oResp = SafeStorageUtils.getPresignedURLUpload(sPNClient, sPNClient_AK, fileCreationRequest, sSHA256, sMD5, boHeader, Checksum.SHA256, true);
        iRC = oResp.getStatusCode();
    }

    @When("request a presigned url to upload the file with tag {string} having {int} values expecting failure")
    public void getUploadPresignedURLWithManyValuesExpectingFailure(String tag, int numValues) {
        tag = getValueIfTagged(tag);
        List<String> values = new ArrayList<>();
        for (int i = 0; i < numValues; i++) {
            values.add("v" + i + "-" + randomAlphanumeric(3));
        }
        Map<String, List<String>> tags = Map.of(tag, values);
        FileCreationRequest fileCreationRequest = new FileCreationRequest().contentType(sMimeType).documentType(sDocumentType).status("SAVED").tags(tags);
        Response oResp = SafeStorageUtils.getPresignedURLUpload(sPNClient, sPNClient_AK, fileCreationRequest, sSHA256, sMD5, boHeader, Checksum.SHA256, true);
        iRC = oResp.getStatusCode();
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

    @When("it's available_ss")
    public void it_s_available_ss() throws JsonProcessingException, InterruptedException {
        Response oResp;
        iRC = 0;
        //Set a time limit for the availability check.
        Instant timeLimit = Instant.now().plusMillis(Long.parseLong(System.getProperty("pn.ss.document.availability.timeout.millis")));
        boolean hasBeenFound = false;
        //Check if the document is available every x seconds.
        //Time limit represent a timeout for the check.
        while (Instant.now().isBefore(timeLimit)) {
            oResp = SafeStorageUtils.getDocument(sKey);
            iRC = oResp.getStatusCode();
            if (iRC == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                log.trace(oResp.getBody().asString());
                DocumentResponse oFDR = objectMapper.readValue(oResp.getBody().asString(), DocumentResponse.class);
                DocumentResponseDocument document = oFDR.getDocument();
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

        System.out.println("S3 KEY: "+sKey);
        statusCode = 200;
    }


    @And("i check availability message {string}")
    public void i_check_availability_message(String sRC) {
        checkAvailabilityMessage(sRC, sKey, EVENT_BUS_SOURCE_AVAILABLE_DOCUMENT);
    }

    @And("i check unavailability message {string}")
    public void i_check_unavailability_message(String sRC) {
        checkAvailabilityMessage(sRC, sKey, EVENT_BUS_SOURCE_UNAVAILABILITY_EVENT);
    }

    @And("the availability message exposes tag {string} without local prefix")
    public void the_availability_message_exposes_tag_without_local_prefix(String tag) {
        String resolved = getValueIfTagged(tag);
        Map<String, List<String>> eventTags = queuePoller.getTags(sKey);
        Assertions.assertNotNull(eventTags, "No tags in availability event for key " + sKey);
        Assertions.assertTrue(eventTags.containsKey(resolved), "Expected unprefixed tag key '" + resolved + "' in event tags: " + eventTags);
        Assertions.assertTrue(eventTags.keySet().stream().noneMatch(k -> k.contains("~")), "Event tags must not expose local prefix: " + eventTags);
    }

    @And("i check glacier restore availability message {string}")
    public void i_check_glacier_restore_availability_message(String sRC) {
        checkAvailabilityMessage(sRC, sKey, EVENT_BUS_SOURCE_GLACIER_DOCUMENTS);
    }

    private void checkAvailabilityMessage(String statusCode, String fileKey, String detailType) {
        int sCode;
        boolean check = queuePoller.checkMessageAvailability(fileKey, detailType);
        System.out.println("CHECK: "+ check);
        if (!check) {
            sCode = 404;
            log.info("Message not found for key {}", sKey);
        } else {
            sCode = 200;
            log.debug("Message found for key {}", sKey);
        }
        Assertions.assertEquals(Integer.parseInt(statusCode), sCode);
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

    @When("I send restore event to main bucket events queue")
    public void sendRestoreEventToAvailabilityEventsQueue() {
        String bucketEventsQueue = System.getProperty("pn.ss.main.bucket.events.queue.name");
        String bucketName = System.getProperty("pn.ss.availability.bucket.name");
        if (bucketName == null || bucketName.isEmpty()) {
            bucketName = s3Service.getBucketName(System.getProperty("pn.ss.availability.bucket.prefix"));
        }
        S3EventNotification eventNotification = S3Utils.createS3EventNotification(sKey, OBJECT_RESTORE_COMPLETED, bucketName);
        sqsService.send(bucketEventsQueue, eventNotification.toJson());
    }

    @When("I change document state to {string}")
    public void iChangeDocumentState(String status) {
        Response response = SafeStorageUtils.patchDocument(sPNClient, sPNClient_AK, sKey, new DocumentChanges().documentState(status));
        Assertions.assertEquals(200, response.getStatusCode());
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
        if (queuePoller != null)
            queuePoller.close();
    }

    @Then("i found in S3 final bucket with a single version")
    public void iFoundInTheFinalSBucketWithASingleVersion() {
        Assertions.assertNotNull(sKey, "Document key must not be null");

        String finalBucket = System.getProperty("pn.ss.availability.bucket.name");
        if (finalBucket == null || finalBucket.isEmpty()) {
            System.out.println("FinalBucket property is empty");
            finalBucket = s3Service.getBucketName(
                    System.getProperty("pn.ss.availability.bucket.prefix")
            );
            System.out.println("FinalBucket property: "+finalBucket);
        }

        ListObjectVersionsResponse response = s3Service.listObjectVersions(sKey, finalBucket);

        long versionCount = response.versions().stream()
                .filter(v -> v.key().equals(sKey))
                .count();

        System.out.println("Final bucket version count for key " + sKey + ": " + versionCount);

        Assertions.assertEquals(1, versionCount,
                "Expected exactly 1 version in final bucket for key " + sKey + ", but found " + versionCount);

    }

    @And("the file is no present in the staging S3 bucket")
    public void theFileIsNoPresentInTheStagingSBucket() {
        Assertions.assertNotNull(sKey, "Document key must not be null");

        String stagingBucket = System.getProperty("pn.ss.availability.bucket.staging.name");
        if (stagingBucket == null || stagingBucket.isEmpty()) {
            System.out.println("StagingBucket property is empty");
            stagingBucket = s3Service.getBucketName(
                    System.getProperty("pn.ss.availability.staging.bucket.prefix")
            );
            System.out.println("stagingBucket property: "+stagingBucket);
        }

        ListObjectVersionsResponse response = s3Service.listObjectVersions(sKey, stagingBucket);

        long versionCount = response.versions().stream()
                .filter(v -> v.key().equals(sKey))
                .count();

        System.out.println("Staging bucket version count for key " + sKey + ": " + versionCount);

        Assertions.assertEquals(0, versionCount,
                "Expected 0 versions in staging bucket for key " + sKey + ", but found " + versionCount);

    }

    @And("the file has a tag ERROR in the S3 staging bucket and no version in final bucket")
    public void theFileHasErrorTagInTheS3Bucket() {
        Assertions.assertNotNull(sKey, "Document key must not be null");

        String finalBucket = System.getProperty("pn.ss.availability.bucket.name");
        if (finalBucket == null || finalBucket.isEmpty()) {
            System.out.println("FinalBucket property is empty");
            finalBucket = s3Service.getBucketName(
                    System.getProperty("pn.ss.availability.bucket.prefix")
            );
            System.out.println("FinalBucket property: "+finalBucket);
        }

        String stagingBucket = System.getProperty("pn.ss.availability.bucket.staging.name");
        if (stagingBucket == null || stagingBucket.isEmpty()) {
            System.out.println("StagingBucket property is empty");
            stagingBucket = s3Service.getBucketName(
                    System.getProperty("pn.ss.availability.staging.bucket.prefix")
            );
            System.out.println("stagingBucket property: "+stagingBucket);
        }

        GetObjectTaggingResponse taggingResponse = s3Service.getObjectTagging(sKey, stagingBucket);
        ListObjectVersionsResponse response = s3Service.listObjectVersions(sKey, finalBucket);

        long versionCount = response.versions().stream().filter(v -> v.key().equals(sKey)).count();

        System.out.println("Final bucket version count for key " + sKey + ": " + versionCount);

        Assertions.assertEquals(0, versionCount, "Expected 0 versions in final bucket for key " + sKey + ", but found " + versionCount);
        boolean hasErrorTag = taggingResponse.tagSet().stream().anyMatch(tag -> tag.value().equals("ERROR"));
        System.out.println("Object " + sKey + " has ERROR tag: " + hasErrorTag);
        Assertions.assertTrue(hasErrorTag, "Expected object " + sKey + " to have a tag with value ERROR, but it does not.");
    }

}
