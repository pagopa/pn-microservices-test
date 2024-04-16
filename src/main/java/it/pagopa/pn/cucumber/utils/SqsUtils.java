package it.pagopa.pn.cucumber.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.pn.cucumber.dto.MessageBodyDto;
import it.pagopa.pn.cucumber.dto.NotificationMessage;
import it.pagopa.pn.cucumber.dto.RicezioneEsitiDto;
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
import java.util.function.BiFunction;

@Slf4j
public class SqsUtils {


    private static final SqsClient sqsClient = SqsClient.create();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String EVENT_BUS_SOURCE_AVAILABLE_DOCUMENT = "SafeStorageOutcomeEvent";
    public static final String EVENT_BUS_SOURCE_DIGITAL_MESSAGE = "ExternalChannelOutcomeEvent";
    public static final String GESTORE_DISPONIBILITA_EVENT_NAME = "GESTORE DISPONIBILITA";
    public static final String NOTIFICATION_TRACKER_EVENT_NAME = "NOTIFICATION TRACKER";
    public static final String EVENT_CODE_SENT_SMS = "S003";
    public static final String EVENT_CODE_SENT_EMAIL = "M003";


    public static boolean checkMessageInDebugQueue(String id, String queueName, BiFunction<Message, String, Boolean> messageChecker) {
        long pollingInterval = Long.parseLong(System.getProperty("document.availability.timeout.millis"));
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
                    try {
                        hasFoundMessage = messageChecker.apply(message, id);
                    } catch (Exception e) {
                        throw new RuntimeException();
                    }
                    if (hasFoundMessage) {
                        log.info("Message found! Deleting message '{}' from queue...", message.messageId());
                        sqsClient.deleteMessage(builder -> builder.queueUrl(queueUrl).receiptHandle(message.receiptHandle()));
                        break;
                    }
                }
            }
            try {
                log.debug("Waiting for next polling...");
                Thread.sleep(pollingInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return hasFoundMessage;
    }
    public static boolean checkMessageInEcDebugQueue(String id, String queueName, String statusToCheck) {
        return checkMessageInDebugQueue(id, queueName, (message, ecId) -> checkEcMessage(message, ecId, statusToCheck));
    }

    public static boolean checkMessageInSsDebugQueue(String id, String queueName) {
        return checkMessageInDebugQueue(id, queueName, SqsUtils::checkSsMessage);
    }
    public static boolean checkEcMessage(Message message, String id, String statusToCheck) {
        log.debug("id {}", id);
        JSONObject messageBodyJsonObj = new JSONObject(message.body());
        String source = messageBodyJsonObj.get("source").toString();
        String detailType = messageBodyJsonObj.get("detail-type").toString();
        JSONObject detailJsonObj = messageBodyJsonObj.getJSONObject("detail");
        if (source.equals(NOTIFICATION_TRACKER_EVENT_NAME)) {
            SingleStatusUpdate singleStatusUpdate = convertStringToObject(detailJsonObj.toString(), SingleStatusUpdate.class);
            if (detailType.equals(EVENT_BUS_SOURCE_DIGITAL_MESSAGE) && singleStatusUpdate != null) {
                if (singleStatusUpdate.getDigitalCourtesy() != null) {
                    CourtesyMessageProgressEvent digitalCourtesy = singleStatusUpdate.getDigitalCourtesy();
                    log.info(digitalCourtesy.toString());
                    return digitalCourtesy.getRequestId().equals(id) && digitalCourtesy.getEventCode().getValue().equalsIgnoreCase(statusToCheck);
                } else if (singleStatusUpdate.getDigitalLegal() != null) {
                    LegalMessageSentDetails digitalLegal = singleStatusUpdate.getDigitalLegal();
                    return digitalLegal.getRequestId().equals(id) && digitalLegal.getEventCode().getValue().equalsIgnoreCase(statusToCheck);
                } else if (singleStatusUpdate.getAnalogMail() != null) {
                    //TODO rivedere lo stato da controllare
                    PaperProgressStatusEvent analogMail = singleStatusUpdate.getAnalogMail();
                    return analogMail.getRequestId().equals(id) && analogMail.getStatusCode().equalsIgnoreCase(statusToCheck);
                }
            }
        }
        return false;
    }

    public static boolean checkSsMessage(Message message, String id) {
        log.debug("id {}", id);
        MessageBodyDto messageBodyDto = parseMessageBody(message);

        if (messageBodyDto.getSource().equals(GESTORE_DISPONIBILITA_EVENT_NAME)) {
            NotificationMessage notificationMessage = convertStringToObject(messageBodyDto.getDetail(), NotificationMessage.class);
            return messageBodyDto.getDetailType().equals(EVENT_BUS_SOURCE_AVAILABLE_DOCUMENT) && notificationMessage.getKey().equals(id);
        }
        return false;
    }

    private static RicezioneEsitiDto ricezioneEsiti() {
        return null;
    }


    @SneakyThrows
    private static <T> T convertStringToObject(String payload, Class<T> classToConvert) {
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(payload, classToConvert);
    }

    public static MessageBodyDto parseMessageBody(Message message) {
        JSONObject messageBodyJsonObj = new JSONObject(message);
        MessageBodyDto messageBodyDto = new MessageBodyDto();
        messageBodyDto.setSource(messageBodyJsonObj.get("source").toString());
        messageBodyDto.setDetailType(messageBodyJsonObj.get("detail-type").toString());
        messageBodyDto.setDetail(messageBodyJsonObj.getJSONObject("detail").toString());
        return messageBodyDto;
    }

}
