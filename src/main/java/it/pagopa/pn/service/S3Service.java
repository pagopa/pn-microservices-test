package it.pagopa.pn.service;

import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;

public interface S3Service {

    String getBucketName(String prefix);
    ListObjectVersionsResponse listObjectVersions(String key, String bucket);
    GetObjectTaggingResponse getObjectTagging(String key, String bucket);



}
