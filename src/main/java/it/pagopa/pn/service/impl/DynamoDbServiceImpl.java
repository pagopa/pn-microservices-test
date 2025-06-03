package it.pagopa.pn.service.impl;

import it.pagopa.pn.service.DynamoDbService;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DynamoDbServiceImpl implements DynamoDbService {

    private final DynamoDbClient dynamoDbClient;

    public DynamoDbServiceImpl() {
        Region region = Region.of(System.getProperty("aws.region"));
        DynamoDbClientBuilder builder = DynamoDbClient.builder().region(region).credentialsProvider(DefaultCredentialsProvider.create());
        String testAwsEndpoint = System.getProperty("test.aws.s3.endpoint");
        if (testAwsEndpoint != null) {
            builder.endpointOverride(URI.create(testAwsEndpoint));
        }
        this.dynamoDbClient = builder.build();
    }

    public QueryResponse queryByRequestId(String tableName, String requestId) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":v_requestId", AttributeValue.builder().s(requestId).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("requestId = :v_requestId")
                .expressionAttributeValues(expressionValues)
                .build();

        return dynamoDbClient.query(queryRequest);
    }

}