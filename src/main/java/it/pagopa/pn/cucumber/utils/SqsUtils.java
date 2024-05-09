package it.pagopa.pn.cucumber.utils;

import it.pagopa.pn.cucumber.dto.MessageBodyDto;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

@Slf4j
public class SqsUtils {

    private SqsUtils() {
        throw new IllegalStateException("SqsUtils is a utility class");
    }

    public static final String EVENT_BUS_SOURCE_AVAILABLE_DOCUMENT = "SafeStorageOutcomeEvent";
    public static final String GESTORE_DISPONIBILITA_EVENT_NAME = "GESTORE DISPONIBILITA";

    public static boolean isSsMessage(MessageBodyDto messageBodyDto) {
        return messageBodyDto.getSource().equals(GESTORE_DISPONIBILITA_EVENT_NAME) &&
                messageBodyDto.getDetailType().equals(EVENT_BUS_SOURCE_AVAILABLE_DOCUMENT);
    }

    public static MessageBodyDto parseMessageBody(String messageBody) {
        JSONObject messageBodyJsonObj = new JSONObject(messageBody);
        MessageBodyDto messageBodyDto = new MessageBodyDto();
        messageBodyDto.setSource(messageBodyJsonObj.get("source").toString());
        messageBodyDto.setDetailType(messageBodyJsonObj.get("detail-type").toString());
        messageBodyDto.setDetail(messageBodyJsonObj.getJSONObject("detail").toString());
        return messageBodyDto;
    }

}
