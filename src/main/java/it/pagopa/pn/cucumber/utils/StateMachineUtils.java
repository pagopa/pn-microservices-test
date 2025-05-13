package it.pagopa.pn.cucumber.utils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

import static it.pagopa.pn.cucumber.utils.RequestEndpoint.STATEMACHINE_VALIDATE_EXTERNALSTATUS;
import static it.pagopa.pn.cucumber.utils.RequestEndpoint.STATEMACHINE_VALIDATE_STATUS;

@Slf4j
public class StateMachineUtils {


    protected static RequestSpecification stdReq() {
        return RestAssured.given()
                .header("Accept", "application/json")
                .header("Content-type", "application/json")
                .header("x-amz-trace-id", java.util.UUID.randomUUID().toString());
    }

public static Response validateStatus (String process, String status, String clientId, String nextStatus){

    RequestSpecification oReq = stdReq()
            .pathParam("process", process)
            .pathParam("status",status)
            .queryParam("clientId", clientId)
            .queryParam("nextStatus", nextStatus);

    return CommonUtils.myGet(oReq, STATEMACHINE_VALIDATE_STATUS,CommonUtils.PN_SM);
}

    public static Response validateExternalStatus (String process, String status, String clientId){

        RequestSpecification oReq = stdReq()
                .pathParam("process", process)
                .pathParam("status",status)
                .queryParam("clientId", clientId);
        return CommonUtils.myGet(oReq, STATEMACHINE_VALIDATE_EXTERNALSTATUS,CommonUtils.PN_SM);
    }
}
