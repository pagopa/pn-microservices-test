package it.pagopa.pn.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.service.SqsService;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.net.URI;

public class SqsServiceImpl implements SqsService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    public SqsServiceImpl() {
        Region region = Region.of(System.getProperty("aws.region"));
        SqsClientBuilder builder = SqsClient.builder().region(region).credentialsProvider(DefaultCredentialsProvider.create());
        String testAwsSqsEndpoint = System.getProperty("test.aws.sqs.endpoint");
        if (testAwsSqsEndpoint != null) {
            builder.endpointOverride(URI.create(testAwsSqsEndpoint));
        }
        this.sqsClient = builder.build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public <T> SendMessageResponse send(String queueName, T payload) throws JsonProcessingException {
        String queueUrl = sqsClient.getQueueUrl(builder -> builder.queueName(queueName)).queueUrl();
        String strPayload = objectMapper.writeValueAsString(payload);
        return sqsClient.sendMessage(builder -> builder.queueUrl(queueUrl).messageBody(strPayload));
    }

    @Override
    public SendMessageResponse send(String queueName, String payload) {
        String queueUrl = sqsClient.getQueueUrl(builder -> builder.queueName(queueName)).queueUrl();
        return sqsClient.sendMessage(builder -> builder.queueUrl(queueUrl).messageBody(payload));
    }
}
