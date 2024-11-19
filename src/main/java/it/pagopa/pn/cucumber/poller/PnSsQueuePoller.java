package it.pagopa.pn.cucumber.poller;

import it.pagopa.pn.cucumber.dto.NotificationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.cucumber.dto.MessageBodyDto;
import jakarta.jms.*;
import lombok.CustomLog;
import lombok.SneakyThrows;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static it.pagopa.pn.cucumber.utils.SqsUtils.isSsMessage;
import static it.pagopa.pn.cucumber.utils.SqsUtils.parseMessageBody;

@CustomLog
public class PnSsQueuePoller extends QueuePoller {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<String> availableStatuses = List.of("SAVED", "PRELOADED");

    public PnSsQueuePoller() {
        super(System.getProperty("pn.ss.gestore.disponibilita.queue.name"));
    }

    @Override
    public void onMessage(jakarta.jms.Message message) {
        try {
            MessageBodyDto messageBodyDto = parseMessageBody(((TextMessage) message).getText());
            String detailType = messageBodyDto.getDetailType();
            log.trace("Retrieved message from queue: " + messageBodyDto);
            NotificationMessage notificationMessage = objectMapper.readValue(messageBodyDto.getDetail(), NotificationMessage.class);
            String documentStatus = notificationMessage.getDocumentStatus();
            if (isSsMessage(messageBodyDto)) {
                if (!this.messageMap.containsKey(notificationMessage.getKey()))
                    this.messageMap.put(notificationMessage.getKey(), new HashSet<>(List.of(concatStatusWithDetailType(documentStatus, detailType))));
                else {
                    Set<String> documentStatusList = this.messageMap.get(notificationMessage.getKey());
                    documentStatusList.add(concatStatusWithDetailType(documentStatus, detailType));
                    this.messageMap.put(notificationMessage.getKey(), documentStatusList);
                }
            }
        } catch (Exception e) {
            log.error("Error while receiving message from SS queue", e);
        }
    }

    public boolean checkMessageAvailability(String fileKey, String detailType) {
        boolean check = false;
        long pollingInterval = Long.parseLong(System.getProperty("pn.ss.sqs.lookup.interval.millis"));
        Instant timeLimit = Instant.now().plusMillis(Long.parseLong(System.getProperty("pn.ss.sqs.lookup.timeout.millis")));
        while (Instant.now().isBefore(timeLimit)) {
            var result = this.messageMap.get(fileKey);
            if (result != null) {
                check = availableStatuses.stream().anyMatch(availableStatus -> result.contains(concatStatusWithDetailType(availableStatus, detailType)));
                if (check) break;
            }
            try {
                Thread.sleep(pollingInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return check;
    }

    private String concatStatusWithDetailType(String status, String detailType) {
        return status + "~" + detailType;
    }

}
