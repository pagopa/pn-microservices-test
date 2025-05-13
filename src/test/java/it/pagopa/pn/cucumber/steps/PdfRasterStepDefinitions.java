package it.pagopa.pn.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import it.pagopa.pn.cucumber.utils.PdfRasterUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;

import java.io.File;


@Slf4j
public class PdfRasterStepDefinitions {

    private Response response;
    private String fileName;

    @Given("a valid PDF document named {string}")
    public void givenValidPdfDocumentNamed(String fileName) {
        this.fileName = fileName;
    }

    @When("I send a multipart POST request to {string}")
    public void whenISendMultipartPostRequestToEndpoint(String endpoint) {
        File pdf = new File("src/test/resources/" + fileName);
        response = PdfRasterUtils.getCurrentClientConfig(endpoint, pdf, "file", "application/pdf");
    }

    @Then("I should receive an HTTP response with status {int}")
    public void thenIShouldReceiveHttpResponseWithStatus(int statusCode) {
        Assertions.assertEquals(statusCode, response.getStatusCode());
    }

    @Then("the response should contain a valid rasterized PDF document")
    public void thenResponseShouldContainValidRasterizedPdfDocument() {
        byte[] pdfContent = response.asByteArray();
        Assertions.assertTrue(pdfContent.length > 4, "PDF content is empty or invalid");
        Assertions.assertEquals("%PDF", new String(pdfContent, 0, 4), "Invalid PDF header");
    }

    @Given("an invalid PDF document named {string}")
    public void givenInvalidPdfDocumentNamed(String fileName) {
        this.fileName = fileName;
    }
}
