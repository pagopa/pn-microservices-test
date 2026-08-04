package it.pagopa.pn.cucumber.poller;

import it.pagopa.pn.cucumber.dto.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.cucumber.dto.MessageBodyDto;
import jakarta.jms.*;
import lombok.CustomLog;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static it.pagopa.pn.cucumber.utils.SqsUtils.isSsMessage;
import static it.pagopa.pn.cucumber.utils.SqsUtils.parseMessageBody;

@CustomLog
public class PnSsQueuePoller extends QueuePoller {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Map<String, List<String>>> tagsMap = new ConcurrentHashMap<>();

    public PnSsQueuePoller() {
        super(System.getProperty("pn.ss.gestore.disponibilita.queue.name"));
    }

    @Override
    public void onMessage(jakarta.jms.Message message) {
        try {
            MessageBodyDto messageBodyDto = parseMessageBody(((TextMessage) message).getText());
            String detailType = messageBodyDto.getDetailType();
            log.debug("Retrieved message from queue: " + messageBodyDto);
            NotificationMessage notificationMessage = objectMapper.readValue(messageBodyDto.getDetail(), NotificationMessage.class);
            if (isSsMessage(messageBodyDto)) {
                if (!this.messageMap.containsKey(notificationMessage.getKey()))
                    this.messageMap.put(notificationMessage.getKey(), new HashSet<>(List.of(detailType)));
                else {
                    Set<String> documentStatusList = this.messageMap.get(notificationMessage.getKey());
                    documentStatusList.add(detailType);
                    this.messageMap.put(notificationMessage.getKey(), documentStatusList);
                }
                if (notificationMessage.getTags() != null) {
                    this.tagsMap.put(notificationMessage.getKey(), notificationMessage.getTags());
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
                check = result.contains(detailType);
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

    public Map<String, List<String>> getTags(String fileKey) {
        return tagsMap.get(fileKey);
    }

}
