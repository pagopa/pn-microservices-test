package it.pagopa.pn.service.impl;

import it.pagopa.pn.service.S3Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;

import java.net.URI;

public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    public S3ServiceImpl() {
        Region region = Region.of(System.getProperty("aws.region"));
        S3ClientBuilder builder = S3Client.builder().region(region).credentialsProvider(DefaultCredentialsProvider.create());
        String testAwsS3Endpoint = System.getProperty("test.aws.s3.endpoint");
        if (testAwsS3Endpoint != null) {
            builder.endpointOverride(URI.create(testAwsS3Endpoint));
        }
        this.s3Client = builder.build();
    }

    @Override
    public String getBucketName(String prefix) {
        return s3Client.listBuckets()
                .buckets()
                .stream()
                .filter(bucket -> bucket.name().startsWith(prefix))
                .findFirst()
                .orElseThrow()
                .name();
    }

    @Override
    public ListObjectVersionsResponse listObjectVersions(String key, String bucketName) {

        ListObjectVersionsRequest request = ListObjectVersionsRequest.builder()
                .bucket(bucketName)
                .prefix(key)
                .build();

        return s3Client.listObjectVersions(request);
    }

}
