package it.pagopa.pn.service;

import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public interface DynamoDbService {

    QueryResponse queryByRequestId(String tableName, String requestId);

    GetItemResponse getItemByKey(String tableName, String keyName, String keyValue);
}
