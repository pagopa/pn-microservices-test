package it.pagopa.pn.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.pn.ec.rest.v1.api.CourtesyMessageProgressEvent;
import it.pagopa.pn.ec.rest.v1.api.LegalMessageSentDetails;
import it.pagopa.pn.ec.rest.v1.api.PaperProgressStatusEvent;
import it.pagopa.pn.ec.rest.v1.api.SingleStatusUpdate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import org.json.JSONObject;

@Slf4j
public class SqsUtils {


    private static final SqsClient sqsClient = SqsClient.create();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String EVENT_BUS_SOURCE_AVAILABLE_DOCUMENT = "SafeStorageOutcomeEvent";
    public static final String EVENT_BUS_SOURCE_DIGITAL_MESSAGE = "ExternalChannelOutcomeEvent";
    public static final String GESTORE_DISPONIBILITA_EVENT_NAME = "GESTORE DISPONIBILITA";
    public static final String NOTIFICATION_TRACKER_EVENT_NAME = "NOTIFICATION TRACKER";


    public static boolean checkMessageInDebugQueue(String id, String queueName) {
        long pollingInterval = Config.getInstance().getDocumentAvailabilityTimeout();
        int maxPollingAttempts = 3;
        String queueUrl = sqsClient.getQueueUrl(builder -> builder.queueName(queueName)).queueUrl();
        boolean hasFoundMessage = false;
        for (int attempt = 0; attempt < maxPollingAttempts; attempt++) {
            log.debug("Polling messages from '{}', attempt number {}", queueName, attempt);
            boolean boolResp = true;
            while (boolResp) {
                ReceiveMessageResponse response = sqsClient.receiveMessage(builder -> builder.queueUrl(queueUrl).maxNumberOfMessages(10));
                boolResp = response.hasMessages();
                for (Message message : response.messages()) {
                    hasFoundMessage = checkMessage(message, id);
                    if (hasFoundMessage) {
                        log.debug("Message found! Deleting message '{}' from queue...", message.messageId());
                        sqsClient.deleteMessage(builder -> builder.queueUrl(queueUrl).receiptHandle(message.receiptHandle()));
                        break;
                    }
                }
            }
            try {
                log.debug("Waiting for next polling...");
                Thread.sleep(pollingInterval); // aspetto prima di effettuare il polling successivo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return hasFoundMessage;
    }

    @SneakyThrows
    private static <T> T convertStringToObject(String payload, Class<T> classToConvert) {
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(payload, classToConvert);
    }

    private static boolean checkMessage(Message message, String id) {
        JSONObject messageBodyJsonObj = new JSONObject(message.body());
        String source = messageBodyJsonObj.get("source").toString();
        String detailType = messageBodyJsonObj.get("detail-type").toString();
        JSONObject detailJsonObj = messageBodyJsonObj.getJSONObject("detail");

        if (source.equals(GESTORE_DISPONIBILITA_EVENT_NAME)) {
            NotificationMessage notificationMessage = convertStringToObject(detailJsonObj.toString(), NotificationMessage.class);
            return detailType.equals(EVENT_BUS_SOURCE_AVAILABLE_DOCUMENT) && notificationMessage.getKey().equals(id);
        } else if (source.equals(NOTIFICATION_TRACKER_EVENT_NAME)) {
            SingleStatusUpdate singleStatusUpdate = convertStringToObject(detailJsonObj.toString(), SingleStatusUpdate.class);
            if (detailType.equals(EVENT_BUS_SOURCE_DIGITAL_MESSAGE) && singleStatusUpdate != null) {
                if (singleStatusUpdate.getDigitalCourtesy() != null) {
                    CourtesyMessageProgressEvent digitalCourtesy = singleStatusUpdate.getDigitalCourtesy();
                    return digitalCourtesy.getRequestId().equals(id) && digitalCourtesy.getStatus().getValue().equals("SENT");
                } else if (singleStatusUpdate.getDigitalLegal() != null) {
                    LegalMessageSentDetails digitalLegal = singleStatusUpdate.getDigitalLegal();
                    return digitalLegal.getRequestId().equals(id) && digitalLegal.getStatus().getValue().equals("SENT");
                } else if (singleStatusUpdate.getAnalogMail() != null) {
                    //TODO rivedere lo stato da controllare
                    PaperProgressStatusEvent analogMail = singleStatusUpdate.getAnalogMail();
                    return analogMail.getRequestId().equals(id) && analogMail.getStatusDescription().equals("SENT");
                }
            }
        }
        return false;
    }
}
