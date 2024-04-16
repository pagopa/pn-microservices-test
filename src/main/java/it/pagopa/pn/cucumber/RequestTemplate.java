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
        digitalNotificationRequestFactory.setSenderDigitalAddress("pec.sender.digital.address");
        digitalNotificationRequestFactory.setReceiverDigitalAddress("pec.receiver.digital.address");
        digitalNotificationRequestFactory.setMessageText(defaultStringInit);
        digitalNotificationRequestFactory.channel(DigitalNotificationRequest.ChannelEnum.PEC);
        digitalNotificationRequestFactory.setSubjectText("test");
        digitalNotificationRequestFactory.setTags(null);
        digitalNotificationRequestFactory.setAttachmentUrls(null);
        return digitalNotificationRequestFactory;
    }

    //CARTACEO

    public static PaperEngageRequest createPaperEngageRequest(String requestId){
        String defaultAttachmentUrl = "safestorage://test.pdf";
        PaperEngageRequest paperEngageRequestFactory = new PaperEngageRequest();
        PaperEngageRequestAttachments paperEngageRequestAttachmentsFactory = new PaperEngageRequestAttachments();

        paperEngageRequestAttachmentsFactory.setUri(defaultAttachmentUrl);
        paperEngageRequestAttachmentsFactory.setOrder(BigDecimal.valueOf(1));
        paperEngageRequestAttachmentsFactory.setDocumentType("TEST");
        paperEngageRequestAttachmentsFactory.setSha256("stringstringstringstringstringstringstri");
        List<PaperEngageRequestAttachments> paperEngageRequestAttachmentsList = new ArrayList<>();
        paperEngageRequestAttachmentsList.add(paperEngageRequestAttachmentsFactory);
        paperEngageRequestFactory.setAttachments(paperEngageRequestAttachmentsList);
        paperEngageRequestFactory.setReceiverName("");
        paperEngageRequestFactory.setReceiverNameRow2("");
        paperEngageRequestFactory.setReceiverAddress("");
        paperEngageRequestFactory.setReceiverAddressRow2("");
        paperEngageRequestFactory.setReceiverCap("");
        paperEngageRequestFactory.setReceiverCity("");
        paperEngageRequestFactory.setReceiverCity2("");
        paperEngageRequestFactory.setReceiverPr("");
        paperEngageRequestFactory.setReceiverCountry("");
        paperEngageRequestFactory.setReceiverFiscalCode("");
        paperEngageRequestFactory.setSenderName("");
        paperEngageRequestFactory.setSenderAddress("");
        paperEngageRequestFactory.setSenderCity("");
        paperEngageRequestFactory.setSenderPr("");
        paperEngageRequestFactory.setSenderDigitalAddress("");
        paperEngageRequestFactory.setArName("");
        paperEngageRequestFactory.setArAddress("");
        paperEngageRequestFactory.setArCap("");
        paperEngageRequestFactory.setArCity("");
        var vas = new HashMap<String, String>();
        paperEngageRequestFactory.setVas(vas);
        paperEngageRequestFactory.setIun("iun123456789");
        paperEngageRequestFactory.setRequestPaId("PagoPa");
        paperEngageRequestFactory.setProductType("AR");
        paperEngageRequestFactory.setPrintType("B/N12345");
        paperEngageRequestFactory.setRequestId(requestId);
        paperEngageRequestFactory.setClientRequestTimeStamp(Date.from(Instant.now()));
        return paperEngageRequestFactory;
    }

    //CONSOLIDATORE
    public static ConsolidatoreIngressPaperProgressStatusEvent createConsolidatoreIngressPaper(String requestId) {
        ConsolidatoreIngressPaperProgressStatusEvent consolidatore = new ConsolidatoreIngressPaperProgressStatusEvent();
        consolidatore.setRequestId(requestId);
     //   consolidatore.setAttachments();
        consolidatore.setIun("");
        //consolidatore.set
        return consolidatore;

    }


}
