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
    public void tryToGetValidateStatus(String status, String process, String nextStatus) {
        this.status = getValueIfTagged(status);
        this.process = getValueIfTagged(process);
        this.nextStatus = getValueIfTagged(nextStatus);

        log.info("Validating process: {}, status: {}, nextStatus: {}", this.process, this.status, this.nextStatus);


        try {
            this.response = StateMachineUtils.validateStatus(process, status, clientId, nextStatus);


                this.isAllowed = response.then().extract().path("allowed");

                log.info("SM response -> {}", response.asString());
        } catch (Exception e) {
            log.error("Errore durante la chiamata a validateStatus o nel parsing della risposta: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante la chiamata a validateStatus: " + e.getMessage(), e);
        }
    }


    @Then("i get response validate {string}")
    public void iGetResponseValidate(String sRc) {

        Assertions.assertEquals(Boolean.parseBoolean(sRc), isAllowed);

    }


    @Then("i get {string} and {string}")
    public void iGetResponseValidateExternalStatus(String exR, String lR) {

        Assertions.assertEquals(exR, externalStatus);

        //può Tornare un alore a null in logicStatus
        if (null == lR || lR.equals( "null")) Assertions.assertNull(logicStatus);
        else Assertions.assertEquals(lR, logicStatus);
    }




    @When("try to validate a {string} of a {string}")
    public void tryToGetValidateExternalstatus(String status, String process) {

        this.status = getValueIfTagged(status);
        this.process = getValueIfTagged(process);

        log.info("Validating process: {}, status: {}", this.process, this.status);


        try {
            this.response = StateMachineUtils.validateExternalStatus(process, status, clientId);

            externalStatus= response.then().extract().path("externalStatus");
            logicStatus= response.then().extract().path("logicStatus");
            log.info("Response {}", response.asString());

        } catch (Exception e) {
            log.error("Errore durante la chiamata a validateStatus o nel parsing della risposta: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante la chiamata a validateStatus: " + e.getMessage(), e);

        }
        }

}
