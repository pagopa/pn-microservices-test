package it.pagopa.pn.cucumber;

import it.pagopa.pn.cucumber.dto.ClientConfigurationInternalDto;
import it.pagopa.pn.ec.rest.v1.api.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class RequestTemplate {

    //SMS
    public static DigitalCourtesySmsRequest createSmsRequest(String requestId, String receiver){
        String defaultStringInit = "stringDefault";

        DigitalCourtesySmsRequest digitalCourtesySmsRequestFactory= new DigitalCourtesySmsRequest();
        digitalCourtesySmsRequestFactory.setRequestId(requestId);
        digitalCourtesySmsRequestFactory.eventType(defaultStringInit);
        digitalCourtesySmsRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        digitalCourtesySmsRequestFactory.setQos(DigitalCourtesySmsRequest.QosEnum.INTERACTIVE);
        digitalCourtesySmsRequestFactory.setReceiverDigitalAddress(receiver);
        digitalCourtesySmsRequestFactory.setMessageText(defaultStringInit);
        digitalCourtesySmsRequestFactory.channel(DigitalCourtesySmsRequest.ChannelEnum.SMS);
        return digitalCourtesySmsRequestFactory;
    }

    //EMAIL
    public static DigitalCourtesyMailRequest createMailRequest(String requestId, String receiver) {
        String defaultStringInit = "stringDefault";

        DigitalCourtesyMailRequest digitalCourtesyMailRequestFactory= new DigitalCourtesyMailRequest();
        digitalCourtesyMailRequestFactory.setRequestId(requestId);
        digitalCourtesyMailRequestFactory.eventType(defaultStringInit);
        digitalCourtesyMailRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        digitalCourtesyMailRequestFactory.setQos(DigitalCourtesyMailRequest.QosEnum.INTERACTIVE);
        digitalCourtesyMailRequestFactory.setSenderDigitalAddress(System.getProperty("email.sender.digital.address"));
        digitalCourtesyMailRequestFactory.setReceiverDigitalAddress(receiver);
        digitalCourtesyMailRequestFactory.setMessageText(defaultStringInit);
        digitalCourtesyMailRequestFactory.channel(DigitalCourtesyMailRequest.ChannelEnum.EMAIL);
        digitalCourtesyMailRequestFactory.setMessageContentType(DigitalCourtesyMailRequest.MessageContentTypeEnum.PLAIN);
        digitalCourtesyMailRequestFactory.setSubjectText("test");
        return digitalCourtesyMailRequestFactory;
    }

//PEC
    public static DigitalNotificationRequest createDigitalNotificationRequest(String requestId, String receiver){
        String defaultStringInit = "stringDefault";


        DigitalNotificationRequest digitalNotificationRequestFactory = new DigitalNotificationRequest();
        digitalNotificationRequestFactory.setRequestId(requestId);
        digitalNotificationRequestFactory.eventType(defaultStringInit);
        digitalNotificationRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        digitalNotificationRequestFactory.setQos(DigitalNotificationRequest.QosEnum.INTERACTIVE);
        digitalNotificationRequestFactory.setSenderDigitalAddress(System.getProperty("pec.sender.digital.address"));
        digitalNotificationRequestFactory.setReceiverDigitalAddress(receiver);
        digitalNotificationRequestFactory.setMessageText(defaultStringInit);
        digitalNotificationRequestFactory.channel(DigitalNotificationRequest.ChannelEnum.PEC);
        digitalNotificationRequestFactory.setSubjectText("test");
        digitalNotificationRequestFactory.setTags(null);
        digitalNotificationRequestFactory.setMessageContentType(DigitalNotificationRequest.MessageContentTypeEnum.PLAIN);
        return digitalNotificationRequestFactory;
    }

    //CARTACEO

    public static PaperEngageRequest createPaperEngageRequest(String requestId, String receiver) {
        PaperEngageRequest paperEngageRequestFactory = new PaperEngageRequest();

        paperEngageRequestFactory.setReceiverName("Paolo Rossi");
        paperEngageRequestFactory.setReceiverNameRow2("c/o famiglia Bianchi");
        paperEngageRequestFactory.setReceiverAddress(receiver);
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

    public static ClientConfigurationDto createClientConfigurationRequest() {
        ClientConfigurationDto clientConfigurationDto = new  ClientConfigurationDto();
        clientConfigurationDto.setSqsArn("");
        clientConfigurationDto.setSqsName("");
        clientConfigurationDto.setSenderPhysicalAddress(new SenderPhysicalAddressDto());
        clientConfigurationDto.setMailReplyTo("");
        clientConfigurationDto.setPecReplyTo("");
        return clientConfigurationDto;
    }
    public static ClientConfigurationInternalDto createClientConfigurationInternalRequest() {
        ClientConfigurationInternalDto clientConfigurationDto = new  ClientConfigurationInternalDto();
        clientConfigurationDto.setSqsArn("");
        clientConfigurationDto.setSqsName("");
        clientConfigurationDto.setSenderPhysicalAddress(new SenderPhysicalAddressDto());
        clientConfigurationDto.setMailReplyTo("");
        clientConfigurationDto.setPecReplyTo("");
        return clientConfigurationDto;
    }

    private static List<PaperEngageRequestAttachments> getPaperEngageRequestAttachments() {
        PaperEngageRequestAttachments paperEngageRequestAttachments = new PaperEngageRequestAttachments();
        String defaultAttachmentUrl = "safestorage://test.pdf";
        paperEngageRequestAttachments.setUri(defaultAttachmentUrl);
        paperEngageRequestAttachments.setOrder(BigDecimal.valueOf(1));
        paperEngageRequestAttachments.setDocumentType("ATTO");
        paperEngageRequestAttachments.setSha256("stringstringstringstringstringstringstri");

        List<PaperEngageRequestAttachments> paperEngageRequestAttachmentsList = new ArrayList<>();
        paperEngageRequestAttachmentsList.add(paperEngageRequestAttachments);
        return paperEngageRequestAttachmentsList;
    }




}
