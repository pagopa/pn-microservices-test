package it.pagopa.pn.cucumber;

import it.pagopa.pn.ec.rest.v1.api.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;

@Slf4j
public class RequestTemplate {

    protected static DigitalCourtesySmsRequest createSmsRequest(String requestId, String receiver, String messageText) {
        DigitalCourtesySmsRequest digitalCourtesySmsRequestFactory = new DigitalCourtesySmsRequest();
        digitalCourtesySmsRequestFactory.setRequestId(requestId);
        digitalCourtesySmsRequestFactory.eventType("eventType");
        digitalCourtesySmsRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        digitalCourtesySmsRequestFactory.setQos(DigitalCourtesySmsRequest.QosEnum.INTERACTIVE);
        digitalCourtesySmsRequestFactory.setReceiverDigitalAddress(receiver);
        digitalCourtesySmsRequestFactory.setMessageText(messageText);
        digitalCourtesySmsRequestFactory.channel(DigitalCourtesySmsRequest.ChannelEnum.SMS);
        return digitalCourtesySmsRequestFactory;
    }

    protected static DigitalCourtesyMailRequest createMailRequest(String requestId, String receiver) {
        String defaultStringInit = "stringDefault";
        DigitalCourtesyMailRequest digitalCourtesyMailRequestFactory = new DigitalCourtesyMailRequest();
        digitalCourtesyMailRequestFactory.setRequestId(requestId);
        digitalCourtesyMailRequestFactory.eventType(defaultStringInit);
        digitalCourtesyMailRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        digitalCourtesyMailRequestFactory.setQos(DigitalCourtesyMailRequest.QosEnum.INTERACTIVE);
        digitalCourtesyMailRequestFactory.setReceiverDigitalAddress(receiver);
        digitalCourtesyMailRequestFactory.setMessageText(defaultStringInit);
        digitalCourtesyMailRequestFactory.channel(DigitalCourtesyMailRequest.ChannelEnum.EMAIL);
        digitalCourtesyMailRequestFactory.setMessageContentType(DigitalCourtesyMailRequest.MessageContentTypeEnum.PLAIN);
        digitalCourtesyMailRequestFactory.setAttachmentUrls(List.of());
        digitalCourtesyMailRequestFactory.setSubjectText("test");
        return digitalCourtesyMailRequestFactory;
    }

    protected static DigitalNotificationRequest createDigitalNotificationRequest(String requestId, String receiver, String channel, String messageText) {
        DigitalNotificationRequest digitalNotificationRequestFactory = new DigitalNotificationRequest();
        digitalNotificationRequestFactory.setRequestId(requestId);
        digitalNotificationRequestFactory.eventType("eventType");
        digitalNotificationRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        digitalNotificationRequestFactory.setQos(DigitalNotificationRequest.QosEnum.INTERACTIVE);
        digitalNotificationRequestFactory.setSenderDigitalAddress("default");
        digitalNotificationRequestFactory.setReceiverDigitalAddress(receiver);
        digitalNotificationRequestFactory.setMessageText(messageText);
        digitalNotificationRequestFactory.channel(DigitalNotificationRequest.ChannelEnum.fromValue(channel));
        digitalNotificationRequestFactory.setSubjectText("test");
        digitalNotificationRequestFactory.setTags(null);
        digitalNotificationRequestFactory.setMessageContentType(DigitalNotificationRequest.MessageContentTypeEnum.PLAIN);
        return digitalNotificationRequestFactory;
    }

    protected static PaperEngageRequest createPaperEngageRequest(String requestId) {
        PaperEngageRequest paperEngageRequestFactory = new PaperEngageRequest();
        paperEngageRequestFactory.setReceiverName("Paolo Rossi");
        paperEngageRequestFactory.setReceiverNameRow2("c/o famiglia Bianchi");
        paperEngageRequestFactory.setReceiverAddress(System.getProperty("paper.receiver.digital.address"));
        paperEngageRequestFactory.setReceiverAddressRow2("scala A interno 4");
        paperEngageRequestFactory.setReceiverCap("00017");
        paperEngageRequestFactory.setReceiverCity("Roma");
        paperEngageRequestFactory.setReceiverCity2("frz Mostacciano");
        paperEngageRequestFactory.setReceiverPr("RM");
        paperEngageRequestFactory.setReceiverCountry("Italia");
        paperEngageRequestFactory.setReceiverFiscalCode("MYYNA0JJART56HOZ");
        paperEngageRequestFactory.setSenderName("Giovanni");
        paperEngageRequestFactory.setSenderAddress("Verdi");
        paperEngageRequestFactory.setSenderCity("Roma");
        paperEngageRequestFactory.setSenderPr("RM");
        paperEngageRequestFactory.setSenderDigitalAddress("via Napoli 1");
        paperEngageRequestFactory.setArName("String");
        paperEngageRequestFactory.setArAddress("string");
        paperEngageRequestFactory.setArCap("0000");
        paperEngageRequestFactory.setArCity("Roma");
        var vas = new HashMap<String, String>();
        vas.put("additionalProp1", "string");
        paperEngageRequestFactory.setVas(vas);
        paperEngageRequestFactory.setIun(requestId);
        paperEngageRequestFactory.setRequestPaId("00414580183");
        paperEngageRequestFactory.setProductType("AR");
        paperEngageRequestFactory.setPrintType("BN_FRONTE_RETRO");
        paperEngageRequestFactory.setRequestId(requestId);
        paperEngageRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        return paperEngageRequestFactory;
    }

    protected static ClientConfigurationDto createClientConfigurationRequest() {
        ClientConfigurationDto clientConfigurationDto = new ClientConfigurationDto();
        clientConfigurationDto.setSqsArn("");
        clientConfigurationDto.setSqsName("");
        clientConfigurationDto.setSenderPhysicalAddress(new SenderPhysicalAddressDto());
        clientConfigurationDto.setMailReplyTo("");
        clientConfigurationDto.setPecReplyTo("");
        return clientConfigurationDto;
    }

}
