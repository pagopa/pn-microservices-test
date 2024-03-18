package it.pagopa.pn.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.Optional;

public class SqsUtils {

    private static final SqsClient sqsClient = SqsClient.create();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String EVENT_BUS_SOURCE_AVAILABLE_DOCUMENT = "SafeStorageOutcomeEvent";
    public static final String GESTORE_DISPONIBILITA_EVENT_NAME = "GESTORE DISPONIBILITA";

    public static boolean checkIfDocumentIsAvailable(String fileKey, String queueName) {
        String queueUrl = sqsClient.getQueueUrl(builder -> builder.queueName(queueName)).queueUrl();
        ReceiveMessageResponse response = sqsClient.receiveMessage(builder -> builder.queueUrl(queueUrl));
        Optional<Message> optionalMessage;
        while (response.hasMessages()) {
            optionalMessage = response.messages().stream()
                    .filter(message -> {
                        String messageBody = message.body();
                        PutEventsRequestEntry putEventsRequestEntry = convertStringToObject(messageBody, PutEventsRequestEntry.class);
                        NotificationMessage notificationMessage = convertStringToObject(putEventsRequestEntry.detail(), NotificationMessage.class);
                        return putEventsRequestEntry.detailType().equals(EVENT_BUS_SOURCE_AVAILABLE_DOCUMENT) && putEventsRequestEntry.source().equals(GESTORE_DISPONIBILITA_EVENT_NAME) && notificationMessage.getKey().equals(fileKey);
                    })
                    .findFirst();
            if (optionalMessage.isPresent()) {
                Message message = optionalMessage.get();
                sqsClient.deleteMessage(builder -> builder.queueUrl(queueUrl).receiptHandle(message.receiptHandle()));
                return true;
            }
        }
        return false;
    }

    @SneakyThrows
    private static <T> T convertStringToObject(String payload, Class<T> classToConvert) {
        return objectMapper.readValue(payload, classToConvert);
    }

}
