package it.pagopa.pn.service;

import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public interface DynamoDbService {

    QueryResponse queryByRequestId(String tableName, String requestId);
}
