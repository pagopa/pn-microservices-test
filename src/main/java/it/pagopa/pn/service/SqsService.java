package it.pagopa.pn.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

public interface SqsService {

    <T> SendMessageResponse send(String queueName, T payload) throws JsonProcessingException;

    SendMessageResponse send(String queueName, String payload);

}
