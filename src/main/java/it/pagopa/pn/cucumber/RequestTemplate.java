package it.pagopa.pn.cucumber;

import it.pagopa.pn.ec.rest.v1.api.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class RequestTemplate {

    //SMS
    public static DigitalCourtesySmsRequest createSmsRequest(String requestId){
        String defaultStringInit = "stringDefault";

        DigitalCourtesySmsRequest digitalCourtesySmsRequestFactory= new DigitalCourtesySmsRequest();
        digitalCourtesySmsRequestFactory.setRequestId(requestId);
        digitalCourtesySmsRequestFactory.eventType(defaultStringInit);
        digitalCourtesySmsRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        digitalCourtesySmsRequestFactory.setQos(DigitalCourtesySmsRequest.QosEnum.INTERACTIVE);
        digitalCourtesySmsRequestFactory.setReceiverDigitalAddress(System.getProperty("sms.receiver.digital.address"));
        digitalCourtesySmsRequestFactory.setMessageText(defaultStringInit);
        digitalCourtesySmsRequestFactory.channel(DigitalCourtesySmsRequest.ChannelEnum.SMS);
        return digitalCourtesySmsRequestFactory;
    }

    //EMAIL
    public static DigitalCourtesyMailRequest createMailRequest(String requestId) {
        String defaultStringInit = "stringDefault";

        DigitalCourtesyMailRequest digitalCourtesyMailRequestFactory= new DigitalCourtesyMailRequest();
        digitalCourtesyMailRequestFactory.setRequestId(requestId);
        digitalCourtesyMailRequestFactory.eventType(defaultStringInit);
        digitalCourtesyMailRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        digitalCourtesyMailRequestFactory.setQos(DigitalCourtesyMailRequest.QosEnum.INTERACTIVE);
        digitalCourtesyMailRequestFactory.setSenderDigitalAddress(System.getProperty("email.sender.digital.address"));
        digitalCourtesyMailRequestFactory.setReceiverDigitalAddress(System.getProperty("email.receiver.digital.address"));
        digitalCourtesyMailRequestFactory.setMessageText(defaultStringInit);
        digitalCourtesyMailRequestFactory.channel(DigitalCourtesyMailRequest.ChannelEnum.EMAIL);
        digitalCourtesyMailRequestFactory.setMessageContentType(DigitalCourtesyMailRequest.MessageContentTypeEnum.PLAIN);
        digitalCourtesyMailRequestFactory.setSubjectText("test");
        return digitalCourtesyMailRequestFactory;
    }

//PEC
    public static DigitalNotificationRequest createDigitalNotificationRequest(String requestId){
        String defaultStringInit = "stringDefault";


        DigitalNotificationRequest digitalNotificationRequestFactory = new DigitalNotificationRequest();
        digitalNotificationRequestFactory.setRequestId(requestId);
        digitalNotificationRequestFactory.eventType(defaultStringInit);
        digitalNotificationRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        digitalNotificationRequestFactory.setQos(DigitalNotificationRequest.QosEnum.INTERACTIVE);
        digitalNotificationRequestFactory.setSenderDigitalAddress(System.getProperty("pec.sender.digital.address"));
        digitalNotificationRequestFactory.setReceiverDigitalAddress(System.getProperty("pec.receiver.digital.address"));
        digitalNotificationRequestFactory.setMessageText(defaultStringInit);
        digitalNotificationRequestFactory.channel(DigitalNotificationRequest.ChannelEnum.PEC);
        digitalNotificationRequestFactory.setSubjectText("test");
        digitalNotificationRequestFactory.setTags(null);
        digitalNotificationRequestFactory.setMessageContentType(DigitalNotificationRequest.MessageContentTypeEnum.PLAIN);
        return digitalNotificationRequestFactory;
    }

    //CARTACEO

    public static PaperEngageRequest createPaperEngageRequest(String requestId) {
        PaperEngageRequest paperEngageRequestFactory = new PaperEngageRequest();

      //  List<PaperEngageRequestAttachments> paperEngageRequestAttachmentsList = getPaperEngageRequestAttachments();
        // paperEngageRequestFactory.setAttachments(paperEngageRequestAttachmentsList);
        paperEngageRequestFactory.setReceiverName("Paolo Rossi");
        paperEngageRequestFactory.setReceiverNameRow2("c/o famiglia Bianchi");
        paperEngageRequestFactory.setReceiverAddress("via Roma 13");
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
        paperEngageRequestFactory.setSenderDigitalAddress("via napoli 1");
        paperEngageRequestFactory.setArName("String");
        paperEngageRequestFactory.setArAddress("string");
        paperEngageRequestFactory.setArCap("0000");
        paperEngageRequestFactory.setArCity("Roma");
        var vas = new HashMap<String, String>();
        vas.put("additionalProp1", "string");
        paperEngageRequestFactory.setVas(vas);
        paperEngageRequestFactory.setIun("iun123456789");
        paperEngageRequestFactory.setRequestPaId("00414580183");
        paperEngageRequestFactory.setProductType("AR");
        paperEngageRequestFactory.setPrintType("BN_FRONTE_RETRO");
        paperEngageRequestFactory.setRequestId(requestId);
        paperEngageRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        return paperEngageRequestFactory;

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
