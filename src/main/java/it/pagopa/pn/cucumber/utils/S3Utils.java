package it.pagopa.pn.cucumber.utils;

import software.amazon.awssdk.eventnotifications.s3.model.*;

import java.util.List;

public class S3Utils {

    private S3Utils() {
        throw new IllegalStateException("S3Utils is a utility class");
    }

    public static final String OBJECT_RESTORE_COMPLETED = "ObjectRestore:Completed";

    public static S3EventNotification createS3EventNotification(String fileKey, String eventName) {
        S3EventNotificationRecord s3EventNotificationRecord = new S3EventNotificationRecord();
        s3EventNotificationRecord.setEventName(eventName);
        S3 s3 = new S3("configurationId",
                new S3Bucket(System.getProperty("pn.ss.availability.bucket"), new UserIdentity("principalId"), "arn"),
                new S3Object(fileKey, 0L, "eTag", "versionId", "sequencer"),
                "s3SchemaVersion");
        s3EventNotificationRecord.setS3(s3);
        return new S3EventNotification(List.of(s3EventNotificationRecord));
    }

}
