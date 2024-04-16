package it.pagopa.pn.cucumber.dto;

import it.pagopa.pn.cucumber.dto.pojo.ConsAuditLogError;
import it.pagopa.pn.ec.rest.v1.api.ConsolidatoreIngressPaperProgressStatusEvent;
import it.pagopa.pn.safestorage.generated.openapi.server.v1.dto.OperationResultCodeResponse;

import java.util.List;

public class RicezioneEsitiDto {
    ConsolidatoreIngressPaperProgressStatusEvent paperProgressStatusEvent;
    OperationResultCodeResponse operationResultCodeResponse;
    List<ConsAuditLogError> consAuditLogErrorList;

    public RicezioneEsitiDto paperProgressStatusEvent(ConsolidatoreIngressPaperProgressStatusEvent paperProgressStatusEvent) {
        this.paperProgressStatusEvent = paperProgressStatusEvent;
        return this;
    }

    public RicezioneEsitiDto operationResultCodeResponse(OperationResultCodeResponse operationResultCodeResponse) {
        this.operationResultCodeResponse = operationResultCodeResponse;
        return this;
    }

    public RicezioneEsitiDto consAuditLogErrorList(List<ConsAuditLogError> consAuditLogErrorList) {
        this.consAuditLogErrorList = consAuditLogErrorList;
        return this;
    }
}
