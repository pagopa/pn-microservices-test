package it.pagopa.pn.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.configuration.EnvironmentConfiguration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.Optional;

@Slf4j
public class SqsUtils {


    private static final SqsClient sqsClient = SqsClient.create();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String EVENT_BUS_SOURCE_AVAILABLE_DOCUMENT = "SafeStorageOutcomeEvent";
    public static final String GESTORE_DISPONIBILITA_EVENT_NAME = "GESTORE DISPONIBILITA";

    public static boolean checkIfDocumentIsAvailable(String fileKey, String queueName) {
        String queueUrl = sqsClient.getQueueUrl(builder -> builder.queueName(queueName)).queueUrl();
        ReceiveMessageResponse response = sqsClient.receiveMessage(builder -> builder.queueUrl(queueUrl));
        Optional<Message> optionalMessage;
        long timeOut = EnvironmentConfiguration.getInstance().getTiming();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeOut;

        while (System.currentTimeMillis() < endTime){
            if (response.hasMessages()) {
                optionalMessage = response.messages().stream()
                        .filter(message -> {
                            String messageBody = message.body();
                            PutEventsRequestEntry putEventsRequestEntry = convertStringToObject(messageBody, PutEventsRequestEntry.class);
                            NotificationMessage notificationMessage = convertStringToObject(putEventsRequestEntry.detail(), NotificationMessage.class);
                            return putEventsRequestEntry.detailType().equals(EVENT_BUS_SOURCE_AVAILABLE_DOCUMENT)
                                    && putEventsRequestEntry.source().equals(GESTORE_DISPONIBILITA_EVENT_NAME)
                                    && notificationMessage.getKey().equals(fileKey);
                        })
                        .findFirst();
                if (optionalMessage.isPresent()) {
                    Message message = optionalMessage.get();
                    log.debug("message {}", message);
                    sqsClient.deleteMessage(builder -> builder.queueUrl(queueUrl).receiptHandle(message.receiptHandle()));
                    return true;
                }
            }
        }

        return false;
    }



    @SneakyThrows
    private static <T> T convertStringToObject(String payload, Class<T> classToConvert) {
        return objectMapper.readValue(payload, classToConvert);
    }

}
