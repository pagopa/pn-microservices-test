package it.pagopa.pn.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.time.Instant;
import java.util.Optional;

@Slf4j
public class SqsUtils {


    private static final SqsClient sqsClient = SqsClient.create();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String EVENT_BUS_SOURCE_AVAILABLE_DOCUMENT = "SafeStorageOutcomeEvent";
    public static final String GESTORE_DISPONIBILITA_EVENT_NAME = "GESTORE DISPONIBILITA";


    public static boolean checkIfDocumentIsAvailable(String fileKey, String queueName) {
        String queueUrl = sqsClient.getQueueUrl(builder -> builder.queueName(queueName)).queueUrl();
        long pollingInterval = Long.parseLong(System.getProperty("pn.ss.sqs.lookup.interval.millis"));
        Instant timeLimit = Instant.now().plusSeconds(Long.parseLong(System.getProperty("pn.ss.sqs.lookup.timeout.millis")));
        while (Instant.now().isBefore(timeLimit)) {
            ReceiveMessageResponse response = sqsClient.receiveMessage(builder -> builder.queueUrl(queueUrl).waitTimeSeconds(2).maxNumberOfMessages(10));

            for (Message message : response.messages()) {
                String messageBody = message.body();
                PutEventsRequestEntry putEventsRequestEntry = convertStringToObject(messageBody, PutEventsRequestEntry.class);
                NotificationMessage notificationMessage = putEventsRequestEntry.detail;

                if (putEventsRequestEntry.detailType().equals(EVENT_BUS_SOURCE_AVAILABLE_DOCUMENT)
                        && putEventsRequestEntry.source().equals(GESTORE_DISPONIBILITA_EVENT_NAME)
                        && notificationMessage.getKey().equals(fileKey)) {
                    log.debug("Message found: " + message);
                    sqsClient.deleteMessage(builder -> builder.queueUrl(queueUrl).receiptHandle(message.receiptHandle()));
                    return true;
                }
            }
            try {
                Thread.sleep(pollingInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    @SneakyThrows
    private static <T> T convertStringToObject(String payload, Class<T> classToConvert) {
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(payload, classToConvert);
    }

}
