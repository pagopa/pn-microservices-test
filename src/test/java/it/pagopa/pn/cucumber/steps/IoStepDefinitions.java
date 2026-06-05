package it.pagopa.pn.cucumber.steps;

import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import it.pagopa.pn.configuration.Config;
import it.pagopa.pn.cucumber.utils.IoMessageUtils;
import lombok.CustomLog;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static it.pagopa.pn.configuration.TestVariablesConfiguration.getValueIfTagged;
import static it.pagopa.pn.cucumber.utils.LogUtils.MDC_CORR_ID_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CustomLog
public class IoStepDefinitions {

    private Map<String, Object> requestBody;
    private Response response;
    private String sentRequestId;
    private String cxId;
    private String storedRequestId;
    private String storedRecipientTaxId;
    private String sentSubject;
    private String sentMarkdown;
    private String sentFirstFileKey;

    private final Set<String> intentionallyAbsentFields = new HashSet<>();

    @BeforeAll
    public static void init() {
        Config.getInstance().loadProperties();
    }

    @Before
    public void resetScenarioState() {
        this.requestBody = null;
        this.response = null;
        this.sentRequestId = null;
        this.storedRequestId = null;
        this.storedRecipientTaxId = null;
        this.sentSubject = null;
        this.sentMarkdown = null;
        this.sentFirstFileKey = null;
        this.intentionallyAbsentFields.clear();
        // cxId non resettato: viene reimpostato dal Background di ogni feature
    }

    @Given("il cxId è {string}")
    public void cxIdIs(String cxId) {
        this.cxId = getValueIfTagged(cxId);
        log.info("cxId={}", this.cxId);
    }

    @Given("un messaggio IO valido con iun {string}, recipientTaxId {string}, senderServiceId {string}, subject {string}, markdown {string}")
    public void aValidIoMessage(String iun, String recipientTaxId,
                                String senderServiceId, String subject, String markdown) {
        this.storedRecipientTaxId = getValueIfTagged(recipientTaxId);
        this.requestBody = IoMessageUtils.buildValidRequest(
                getValueIfTagged(iun),
                this.storedRecipientTaxId,
                getValueIfTagged(senderServiceId),
                subject,
                markdown
        );
        log.info("Prepared IO message: iun={} senderServiceId={}", getValueIfTagged(iun), getValueIfTagged(senderServiceId));
    }

    @Given("un messaggio IO valido senza iun con recipientTaxId {string}, senderServiceId {string}, subject {string}, markdown {string}")
    public void aValidIoMessageWithoutIun(String recipientTaxId,
                                          String senderServiceId, String subject, String markdown) {
        this.requestBody = IoMessageUtils.buildValidRequestWithoutIun(
                getValueIfTagged(recipientTaxId),
                getValueIfTagged(senderServiceId),
                subject,
                markdown
        );
        log.info("Prepared IO message without iun: senderServiceId={}", getValueIfTagged(senderServiceId));
    }

    @Given("un messaggio IO senza il campo {string}")
    public void anIoMessageMissingField(String field) {
        this.requestBody = IoMessageUtils.buildValidRequest(
                getValueIfTagged("@io.iun"),
                getValueIfTagged("@io.recipientTaxId"),
                getValueIfTagged("@io.senderServiceId"),
                "Avviso di pagamento",
                "Gentile cittadino, hai ricevuto un avviso."
        );
        this.requestBody.remove(field);
        this.intentionallyAbsentFields.add(field);
        log.info("Removed field '{}' from IO message request", field);
    }

    @And("la richiesta include paymentData con amount {int}, noticeCode {string} e creditorTaxId {string}")
    public void requestIncludesPaymentData(int amount, String noticeCode, String creditorTaxId) {
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", amount);
        paymentData.put("noticeCode", noticeCode);
        paymentData.put("creditorTaxId", creditorTaxId);
        this.requestBody.put("paymentData", paymentData);
        log.info("Added paymentData: amount={} noticeCode={}", amount, noticeCode);
    }

    @And("la richiesta ha sensitiveContent true")
    public void requestHasSensitiveContent() {
        this.requestBody.put("sensitiveContent", true);
        log.info("Set sensitiveContent=true");
    }

    @And("la richiesta include dueDate {string}")
    public void requestIncludesDueDate(String dueDate) {
        this.requestBody.put("dueDate", dueDate);
        log.info("Set dueDate={}", dueDate);
    }

    @And("la richiesta include paymentData con amount {int}, noticeCode {string}, creditorTaxId {string} e invalidAfterDueDate {string}")
    public void requestIncludesPaymentDataWithInvalidAfterDueDate(int amount, String noticeCode,
                                                                   String creditorTaxId,
                                                                   String invalidAfterDueDate) {
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", amount);
        paymentData.put("noticeCode", noticeCode);
        paymentData.put("creditorTaxId", creditorTaxId);
        paymentData.put("invalidAfterDueDate", Boolean.parseBoolean(invalidAfterDueDate));
        this.requestBody.put("paymentData", paymentData);
        log.info("Added paymentData with invalidAfterDueDate={}: amount={} noticeCode={}", invalidAfterDueDate, amount, noticeCode);
    }

    @And("la richiesta include pollingMaxHours {int}")
    public void requestIncludesPollingMaxHours(int hours) {
        this.requestBody.put("pollingMaxHours", hours);
        log.info("Set pollingMaxHours={}", hours);
    }

    @Given("una richiesta profilo IO valida con recipientTaxId {string}, senderServiceId {string}")
    public void aValidIoProfileRequest(String recipientTaxId, String senderServiceId) {
        this.requestBody = IoMessageUtils.buildProfileRequest(
                getValueIfTagged(recipientTaxId),
                getValueIfTagged(senderServiceId)
        );
        log.info("Prepared IO profile request: recipientTaxId={}", getValueIfTagged(recipientTaxId));
    }

    @Given("una richiesta profilo IO senza campi obbligatori")
    public void aProfileRequestMissingRequiredFields() {
        this.requestBody = new HashMap<>();
        log.info("Prepared empty IO profile request (missing required fields)");
    }

    @Given("un requestId {string}")
    public void aRequestId(String requestId) {
        this.storedRequestId = getValueIfTagged(requestId);
        log.info("storedRequestId={}", this.storedRequestId);
    }

    @Given("invio messaggio IO valido con recipientTaxId {string} e senderServiceId {string}")
    public void sentAValidIoMessage(String recipientTaxId, String senderServiceId) {
        this.storedRecipientTaxId = getValueIfTagged(recipientTaxId);
        this.sentRequestId = IoMessageUtils.generateRequestId();
        this.sentSubject = "Avviso di pagamento";
        this.sentMarkdown = "Gentile cittadino, hai ricevuto un avviso.";
        Map<String, Object> body = IoMessageUtils.buildValidRequest(
                getValueIfTagged("@io.iun"),
                this.storedRecipientTaxId,
                getValueIfTagged(senderServiceId),
                sentSubject,
                sentMarkdown
        );
        body.put("requestId", sentRequestId);
        MDC.put(MDC_CORR_ID_KEY, sentRequestId);
        IoMessageUtils.sendIoMessage(body, resolveCxId());
        log.info("Pre-setup: sent IO message requestId={} recipientTaxId={}", sentRequestId, storedRecipientTaxId);
    }

    @Given("invio messaggio IO valido con allegati, recipientTaxId {string} e senderServiceId {string}")
    public void sentAValidIoMessageWithAttachments(String recipientTaxId, String senderServiceId) {
        this.storedRecipientTaxId = getValueIfTagged(recipientTaxId);
        this.sentRequestId = IoMessageUtils.generateRequestId();
        this.sentSubject = "Avviso di pagamento con allegati";
        this.sentMarkdown = "Gentile cittadino, hai ricevuto un avviso con documenti allegati.";
        Map<String, Object> body = IoMessageUtils.buildValidRequest(
                getValueIfTagged("@io.iun"),
                this.storedRecipientTaxId,
                getValueIfTagged(senderServiceId),
                sentSubject,
                sentMarkdown
        );
        body.put("requestId", sentRequestId);
        this.sentFirstFileKey = "PN_AAR-10b382bdd1a74bfb863918949e8f54f9.pdf";
        body.put("attachments", java.util.List.of(
                java.util.Map.of("id", "att-001", "fileKey", sentFirstFileKey),
                java.util.Map.of("id", "att-002", "fileKey", "PN_NOTIFICATION_ATTACHMENTS-test-doc2.pdf")
        ));
        MDC.put(MDC_CORR_ID_KEY, sentRequestId);
        IoMessageUtils.sendIoMessage(body, resolveCxId());
        log.info("Pre-setup: sent IO message with attachments requestId={} recipientTaxId={}", sentRequestId, storedRecipientTaxId);
    }

    @When("invio il messaggio IO")
    public void sendIoMessage() {
        this.sentRequestId = IoMessageUtils.generateRequestId();
        if (!intentionallyAbsentFields.contains("requestId")) {
            requestBody.put("requestId", sentRequestId);
        }
        MDC.put(MDC_CORR_ID_KEY, sentRequestId);
        this.response = IoMessageUtils.sendIoMessage(requestBody, resolveCxId());
        log.info("IO message sent: requestId={} cxId={} httpStatus={}", sentRequestId, resolveCxId(), response.getStatusCode());
    }

    @When("invio il messaggio IO senza l'header obbligatorio")
    public void sendIoMessageWithoutHeader() {
        this.sentRequestId = IoMessageUtils.generateRequestId();
        if (!intentionallyAbsentFields.contains("requestId")) {
            requestBody.put("requestId", sentRequestId);
        }
        MDC.put(MDC_CORR_ID_KEY, sentRequestId);
        this.response = IoMessageUtils.sendIoMessageWithoutCxId(requestBody);
        log.info("IO message sent WITHOUT cx-id header: requestId={} httpStatus={}", sentRequestId, response.getStatusCode());
    }

    @When("invio la richiesta di profilo IO")
    public void sendIoProfile() {
        MDC.put(MDC_CORR_ID_KEY, resolveCxId());
        this.response = IoMessageUtils.sendIoProfile(requestBody, resolveCxId());
        log.info("IO profile request sent: cxId={} httpStatus={}", resolveCxId(), response.getStatusCode());
    }

    @When("invio la richiesta di profilo IO senza l'header obbligatorio")
    public void sendIoProfileWithoutHeader() {
        this.response = IoMessageUtils.sendIoProfileWithoutCxId(requestBody);
        log.info("IO profile request sent WITHOUT cx-id header: httpStatus={}", response.getStatusCode());
    }

    @When("recupero il messaggio IO per id")
    public void getIoMessageById() {
        String id = storedRequestId != null ? storedRequestId : sentRequestId;
        String taxId = storedRecipientTaxId != null ? storedRecipientTaxId : getValueIfTagged("@io.recipientTaxId");
        this.response = IoMessageUtils.getIoMessage(id, taxId);
        log.info("GET /messages/{} httpStatus={}", id, response.getStatusCode());
    }

    @When("recupero il messaggio IO per id con taxId errato")
    public void getIoMessageByIdWithWrongTaxId() {
        String id = storedRequestId != null ? storedRequestId : sentRequestId;
        this.response = IoMessageUtils.getIoMessage(id, "WRONG-TAX-ID-00000");
        log.info("GET /messages/{} with wrong taxId httpStatus={}", id, response.getStatusCode());
    }

    @When("recupero il messaggio IO senza l'header obbligatorio")
    public void getIoMessageWithoutHeader() {
        String id = storedRequestId != null ? storedRequestId : sentRequestId;
        this.response = IoMessageUtils.getIoMessageWithoutTaxId(id);
        log.info("GET /messages/{} WITHOUT taxId header httpStatus={}", id, response.getStatusCode());
    }

    @When("recupero l'allegato IO con la prima fileKey del messaggio")
    public void getIoAttachmentByFirstFileKey() {
        this.response = IoMessageUtils.getIoAttachment(sentRequestId, sentFirstFileKey, storedRecipientTaxId);
        log.info("GET /messages/{}/{} httpStatus={}", sentRequestId, sentFirstFileKey, response.getStatusCode());
    }

    @When("recupero l'allegato IO con la prima fileKey e taxId errato")
    public void getIoAttachmentWithWrongTaxId() {
        this.response = IoMessageUtils.getIoAttachment(sentRequestId, sentFirstFileKey, "WRONG-TAX-ID-00000");
        log.info("GET /messages/{}/{} with wrong taxId httpStatus={}", sentRequestId, sentFirstFileKey, response.getStatusCode());
    }

    @When("recupero l'allegato IO con fileKey {string} e taxId {string}")
    public void getIoAttachmentWithFileKeyAndTaxId(String fileKey, String taxId) {
        String id = storedRequestId != null ? storedRequestId : sentRequestId;
        this.response = IoMessageUtils.getIoAttachment(id, fileKey, getValueIfTagged(taxId));
        log.info("GET /messages/{}/{} httpStatus={}", id, fileKey, response.getStatusCode());
    }

    @When("recupero l'allegato IO senza l'header obbligatorio")
    public void getIoAttachmentWithoutHeader() {
        this.response = IoMessageUtils.getIoAttachmentWithoutTaxId(sentRequestId, sentFirstFileKey);
        log.info("GET /messages/{}/{} WITHOUT taxId header httpStatus={}", sentRequestId, sentFirstFileKey, response.getStatusCode());
    }

    @And("la risposta è un redirect con header Location valorizzato")
    public void responseIsRedirectWithLocation() {
        String location = response.getHeader("Location");
        assertNotNull(location, "L'header 'Location' è assente nella risposta redirect");
        assertFalse(location.isEmpty(), "L'header 'Location' è vuoto nella risposta redirect");
        log.info("Location redirect: {}", location);
    }

    @And("reinvio lo stesso messaggio IO con lo stesso requestId")
    public void resendIoMessageWithSameRequestId() {
        this.response = IoMessageUtils.sendIoMessage(requestBody, resolveCxId());
        log.info("IO message resent with same requestId={} httpStatus={}", sentRequestId, response.getStatusCode());
    }

    @And("reinvio lo stesso requestId con subject diverso {string}")
    public void resendWithDifferentSubject(String newSubject) {
        Map<String, Object> modifiedBody = new HashMap<>(requestBody);
        modifiedBody.put("subject", newSubject);
        this.response = IoMessageUtils.sendIoMessage(modifiedBody, resolveCxId());
        log.info("IO message resent with different subject, requestId={} httpStatus={}", sentRequestId, response.getStatusCode());
    }

    @And("reinvio lo stesso requestId con cxId diverso {string}")
    public void resendWithDifferentCxId(String differentCxId) {
        this.response = IoMessageUtils.sendIoMessage(requestBody, getValueIfTagged(differentCxId));
        log.info("IO message resent with different cxId={}, requestId={} httpStatus={}", getValueIfTagged(differentCxId), sentRequestId, response.getStatusCode());
    }

    @Then("la risposta HTTP ha status {int}")
    public void responseHttpStatusIs(int expectedStatus) {
        assertEquals(expectedStatus, response.getStatusCode(),
                "HTTP status atteso " + expectedStatus + " ma ricevuto " + response.getStatusCode()
                        + ". Body: " + response.getBody().asString());
    }

    @And("lo status del messaggio è {string}")
    public void messageStatusIs(String expectedStatus) {
        String actualStatus = response.jsonPath().getString("status");
        assertEquals(expectedStatus, actualStatus,
                "Status messaggio atteso '" + expectedStatus + "' ma ricevuto '" + actualStatus + "'");
    }

    @And("attendo che il messaggio abbia status {string}")
    public void waitForIoMessageStatus(String expectedStatus) throws InterruptedException {
        long timeoutMillis = Long.parseLong(System.getProperty("pn.io.message.status.timeout.millis"));
        long intervalMillis = Long.parseLong(System.getProperty("pn.io.message.status.interval.millis"));
        Instant timeLimit = Instant.now().plusMillis(timeoutMillis);
        boolean found = false;
        while (Instant.now().isBefore(timeLimit)) {
            Response getResponse = IoMessageUtils.getIoMessage(sentRequestId, storedRecipientTaxId);
            if (getResponse.getStatusCode() == 200) {
                String actualStatus = getResponse.jsonPath().getString("status");
                if (expectedStatus.equals(actualStatus)) {
                    found = true;
                    break;
                }
                log.info("Attendo status='{}', attuale='{}', requestId={}", expectedStatus, actualStatus, sentRequestId);
            }
            Thread.sleep(intervalMillis);
        }
        assertTrue(found, "Status '" + expectedStatus + "' non raggiunto entro il timeout per requestId=" + sentRequestId);
    }

    @And("lo status del profilo è {string}")
    public void profileStatusIs(String expectedStatus) {
        String actualStatus = response.jsonPath().getString("status");
        assertEquals(expectedStatus, actualStatus,
                "Status profilo atteso '" + expectedStatus + "' ma ricevuto '" + actualStatus + "'");
    }

    @And("il requestId nella risposta corrisponde a quello inviato")
    public void requestIdInResponseMatchesSent() {
        String responseRequestId = response.jsonPath().getString("requestId");
        assertEquals(sentRequestId, responseRequestId,
                "RequestId nella risposta '" + responseRequestId + "' non corrisponde a quello inviato '" + sentRequestId + "'");
    }

    @And("il cxId nella risposta corrisponde a quello inviato")
    public void cxIdInResponseMatchesSent() {
        String responseCxId = response.jsonPath().getString("xPagopaIoConCxId");
        assertEquals(resolveCxId(), responseCxId,
                "xPagopaIoConCxId nella risposta '" + responseCxId + "' non corrisponde a quello inviato '" + resolveCxId() + "'");
    }

    @And("la risposta contiene il campo preferredLanguages")
    public void responseContainsPreferredLanguages() {
        assertNotNull(response.jsonPath().get("preferredLanguages"),
                "Il campo 'preferredLanguages' è assente nella risposta");
    }

    @And("la risposta non contiene il campo preferredLanguages")
    public void responseDoesNotContainPreferredLanguages() {
        assertNull(response.jsonPath().get("preferredLanguages"),
                "Il campo 'preferredLanguages' dovrebbe essere assente nella risposta");
    }

    @Given("una richiesta profilo IO senza il campo {string}")
    public void aProfileRequestMissingField(String field) {
        this.requestBody = IoMessageUtils.buildProfileRequest(
                getValueIfTagged("@io.recipientTaxId"),
                getValueIfTagged("@io.senderServiceId")
        );
        this.requestBody.remove(field);
        log.info("Removed field '{}' from IO profile request", field);
    }

    @And("la richiesta ha subject di {int} caratteri")
    public void requestHasSubjectOfLength(int length) {
        this.requestBody.put("subject", "S".repeat(length));
        log.info("Set subject to {} characters", length);
    }

    @And("la richiesta include allegati")
    public void requestIncludesAttachments() {
        this.requestBody.put("attachments", java.util.List.of(
                java.util.Map.of("id", "att-001", "fileKey", "PN_AAR-10b382bdd1a74bfb863918949e8f54f9.pdf"),
                java.util.Map.of("id", "att-002", "fileKey", "PN_NOTIFICATION_ATTACHMENTS-test-doc2.pdf")
        ));
        log.info("Added 2 attachments to IO message request");
    }

    @And("la richiesta include un allegato senza fileKey")
    public void requestIncludesAttachmentWithoutFileKey() {
        java.util.Map<String, Object> attachment = new java.util.HashMap<>();
        attachment.put("id", "att-001");
        this.requestBody.put("attachments", java.util.List.of(attachment));
        log.info("Added attachment without fileKey to IO message request");
    }

    @And("la richiesta include un allegato senza id")
    public void requestIncludesAttachmentWithoutId() {
        java.util.Map<String, Object> attachment = new java.util.HashMap<>();
        attachment.put("fileKey", "PN_AAR-10b382bdd1a74bfb863918949e8f54f9.pdf");
        this.requestBody.put("attachments", java.util.List.of(attachment));
        log.info("Added attachment without id to IO message request");
    }

    @And("la richiesta include un allegato con fileKey senza estensione pdf")
    public void requestIncludesAttachmentWithNonPdfFileKey() {
        this.requestBody.put("attachments", java.util.List.of(
                java.util.Map.of("id", "att-001", "fileKey", "PN_NOTIFICATION_ATTACHMENTS-test-doc-no-extension")
        ));
        log.info("Added attachment with non-pdf fileKey to IO message request");
    }

    @And("la richiesta include una lista allegati vuota")
    public void requestIncludesEmptyAttachmentList() {
        this.requestBody.put("attachments", java.util.List.of());
        log.info("Added empty attachments list to IO message request");
    }

    @And("la risposta contiene i dettagli del messaggio")
    public void responseContainsMessageDetails() {
        assertNotNull(response.jsonPath().get("details"),
                "Il campo 'details' è assente nella risposta");
    }

    @And("la risposta contiene i dettagli del messaggio con subject e markdown corretti")
    public void responseContainsCorrectDetails() {
        assertNotNull(response.jsonPath().get("details"), "Il campo 'details' è assente nella risposta");
        if (sentSubject != null) {
            assertEquals(sentSubject, response.jsonPath().getString("details.subject"),
                    "Il campo 'details.subject' non corrisponde a quello inviato");
        }
        if (sentMarkdown != null) {
            assertEquals(sentMarkdown, response.jsonPath().getString("details.markdown"),
                    "Il campo 'details.markdown' non corrisponde a quello inviato");
        }
    }

    @And("la risposta contiene almeno un allegato con fileKey, category e contentType")
    public void responseContainsAtLeastOneAttachmentWithAllFields() {
        java.util.List<Map<String, Object>> attachments = response.jsonPath().getList("attachments");
        assertNotNull(attachments, "Il campo 'attachments' è assente nella risposta");
        assertFalse(attachments.isEmpty(), "La lista allegati è vuota nella risposta");
        Map<String, Object> first = attachments.get(0);
        assertNotNull(first.get("url"), "Il campo 'url' (fileKey) è assente nel primo allegato");
        assertEquals("DOCUMENT", first.get("category"),
                "Il campo 'category' del primo allegato non è 'DOCUMENT'");
        assertEquals("application/pdf", first.get("content_type"),
                "Il campo 'content_type' del primo allegato non è 'application/pdf'");
    }

    private String resolveCxId() {
        return cxId != null ? cxId : System.getProperty("clientId-delivery-push", "pn-delivery-push");
    }
}
