package it.pagopa.pn.cucumber.poller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.cucumber.dto.MessageBodyDto;
import it.pagopa.pn.ec.rest.v1.api.CourtesyMessageProgressEvent;
import it.pagopa.pn.ec.rest.v1.api.LegalMessageSentDetails;
import it.pagopa.pn.ec.rest.v1.api.PaperProgressStatusEvent;
import it.pagopa.pn.ec.rest.v1.api.SingleStatusUpdate;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.CustomLog;

import java.time.Instant;
import java.util.*;

import static it.pagopa.pn.cucumber.utils.SqsUtils.isEcMessage;
import static it.pagopa.pn.cucumber.utils.SqsUtils.parseMessageBody;

@CustomLog
public class PnEcQueuePoller extends QueuePoller {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PnEcQueuePoller() {
        super(System.getProperty("pn.ec.notifiche.esterne.queue.name"));
    }

    @Override
    public void onMessage(Message message) {
        try {
            MessageBodyDto messageBodyDto = parseMessageBody(((TextMessage) message).getText());
            SingleStatusUpdate singleStatusUpdate = objectMapper.readValue(messageBodyDto.getDetail(), SingleStatusUpdate.class);
            log.debug("SingleStatusUpdate : {}", singleStatusUpdate);

            String requestId = "";
            String status = "";
            if (isEcMessage(messageBodyDto)) {

                if (singleStatusUpdate.getDigitalCourtesy() != null) {
                    CourtesyMessageProgressEvent digitalCourtesy = singleStatusUpdate.getDigitalCourtesy();
                    requestId = digitalCourtesy.getRequestId();
                    status = digitalCourtesy.getEventCode().getValue();
                } else if (singleStatusUpdate.getDigitalLegal() != null) {
                    LegalMessageSentDetails digitalLegal = singleStatusUpdate.getDigitalLegal();
                    requestId = digitalLegal.getRequestId();
                    status = digitalLegal.getEventCode().getValue();
                } else if (singleStatusUpdate.getAnalogMail() != null) {
                    PaperProgressStatusEvent analogMail = singleStatusUpdate.getAnalogMail();
                    requestId = analogMail.getRequestId();
                    status = analogMail.getStatusCode();
                }

                if (!this.messageMap.containsKey(requestId))
                    this.messageMap.put(requestId, new HashSet<>(List.of(status)));
                else {
                    Set<String> documentStatusList = this.messageMap.get(requestId);
                    documentStatusList.add(status);
                    this.messageMap.put(requestId, documentStatusList);
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkMessageAvailability(String requestId, List<String> statusesToCheck) {
        boolean check = false;
        long pollingInterval = Long.parseLong(System.getProperty("pn.ss.sqs.lookup.interval.millis"));
        Instant timeLimit = Instant.now().plusMillis(Long.parseLong(System.getProperty("pn.ss.sqs.lookup.timeout.millis")));
        Set<String> statusesFound = null;
        while (Instant.now().isBefore(timeLimit)) {
            statusesFound = this.messageMap.get(requestId);
            if (statusesFound != null && statusesFound.containsAll(statusesToCheck)) {
                check = true;
                break;
            }
            try {
                Thread.sleep(pollingInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.debug("Statuses found : {}", statusesFound);
        return check;
    }

}
