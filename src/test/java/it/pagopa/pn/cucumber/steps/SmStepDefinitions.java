package it.pagopa.pn.cucumber.steps;


import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import it.pagopa.pn.cucumber.utils.StateMachineUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;

import static it.pagopa.pn.configuration.TestVariablesConfiguration.getValueIfTagged;

@Slf4j
public class SmStepDefinitions {
    private String clientId = null;
    private String status = null;
    private String process = null;
    private String nextStatus = null;
    private Boolean isAllowed = false;
    private String externalStatus= null;
    private String logicStatus= null;
    private Response response ;


    @Given("a {string}")
    public void aClientIdSendRequest(String clientId) {
        this.clientId= getValueIfTagged(clientId);

    }

    @When("try to validate {string} of a {string} with {string}")
    public void tryToValidateStatus(String status, String process, String nextStatus) {
        this.status = getValueIfTagged(status);
        this.process = getValueIfTagged(process);
        this.nextStatus = getValueIfTagged(nextStatus);

        log.info("Validating transition of process {} from status {} to {}", this.process, this.status, this.nextStatus);


        try {
            this.response = StateMachineUtils.validateStatus(process, status, clientId, nextStatus);


                this.isAllowed = response.then().extract().path("allowed");

                log.debug("validateStatus response: {}", response.asString());
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la chiamata a validateStatus: " + e.getMessage(), e);
        }
    }

    @Then("i get response if nextStatus is {string}")
    public void getResponseValidate(String isAllowedExpected) {

        Assertions.assertEquals(Boolean.parseBoolean(isAllowedExpected), isAllowed);

    }


    @When("submit a {string} of a {string}")
    public void sendStatusProcess(String status, String process) {

        this.status = getValueIfTagged(status);
        this.process = getValueIfTagged(process);

        log.info("Decoding logical status {} of process {}", this.status, this.process);


        try {
            this.response = StateMachineUtils.validateExternalStatus(process, status, clientId);

            externalStatus= response.then().extract().path("externalStatus");
            logicStatus= response.then().extract().path("logicStatus");
            log.debug("validateExternalStatus response: {}", response.asString());

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la chiamata a validateExternalStatus: " + e.getMessage(), e);

        }
        }


    @Then("i get {string} and {string}")
    public void getExternalStatus(String exR, String lR) {

        Assertions.assertEquals(exR, externalStatus);

        //può Tornare un valore a null in logicStatus
        if (null == lR || lR.equals( "null")) Assertions.assertNull(logicStatus);
        else Assertions.assertEquals(lR, logicStatus);
    }

}
