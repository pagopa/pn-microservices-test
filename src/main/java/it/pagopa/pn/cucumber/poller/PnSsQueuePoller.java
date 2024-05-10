package it.pagopa.pn.cucumber.poller;

import it.pagopa.pn.cucumber.dto.NotificationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.cucumber.dto.MessageBodyDto;
import jakarta.jms.*;
import lombok.CustomLog;
import lombok.SneakyThrows;

import java.time.Instant;
import java.util.Set;

import static it.pagopa.pn.cucumber.utils.SqsUtils.isSsMessage;
import static it.pagopa.pn.cucumber.utils.SqsUtils.parseMessageBody;

@CustomLog
public class PnSsQueuePoller extends QueuePoller {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PnSsQueuePoller() {
        super(System.getProperty("pn.ss.gestore.disponibilita.queue.name"));
    }

    @Override
    @SneakyThrows({JsonProcessingException.class, JMSException.class})
    public void onMessage(jakarta.jms.Message message) {
        MessageBodyDto messageBodyDto = parseMessageBody(((TextMessage) message).getText());
        NotificationMessage notificationMessage = objectMapper.readValue(messageBodyDto.getDetail(), NotificationMessage.class);
        if (isSsMessage(messageBodyDto)) {
            if (!this.messageMap.containsKey(notificationMessage.getKey()))
                this.messageMap.put(notificationMessage.getKey(), Set.of(notificationMessage.getDocumentStatus()));
            else {
                Set<String> documentStatusList = this.messageMap.get(notificationMessage.getKey());
                documentStatusList.add(notificationMessage.getDocumentStatus());
                this.messageMap.put(notificationMessage.getKey(), documentStatusList);
            }
        }
    }

    public boolean checkMessageAvailability(String fileKey) {
        boolean check = false;
        long pollingInterval = Long.parseLong(System.getProperty("pn.ss.sqs.lookup.interval.millis"));
        Instant timeLimit = Instant.now().plusMillis(Long.parseLong(System.getProperty("pn.ss.sqs.lookup.timeout.millis")));
        while (Instant.now().isBefore(timeLimit)) {
            var result = this.messageMap.get(fileKey);
            if (result != null && (result.contains("SAVED") || result.contains("PRELOADED"))) {
                check = true;
                break;
            }
            try {
                Thread.sleep(pollingInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return check;
    }

}
