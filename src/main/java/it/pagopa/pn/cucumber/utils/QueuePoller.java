package it.pagopa.pn.cucumber.utils;
import groovy.lang.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Getter
@Setter
@Singleton
public class QueuePoller {


        private static QueuePoller instance;
        private final SqsClient sqsClient;
        private String queueUrl;
        private final ConcurrentMap<String, Message> messageMap;
        private long pollingTimeoutMillis;

        private QueuePoller() {
            sqsClient = SqsClient.create();
            messageMap = new ConcurrentHashMap<>();
        }

        public static synchronized QueuePoller getInstance() {
            if (instance == null) {
                instance = new QueuePoller();
            }
            return instance;
        }

        public void setPollingTimeout(long timeout) {
          //  this.pollingTimeoutMillis = timeUnit.toMillis(timeout);
            this.pollingTimeoutMillis = Long.parseLong(System.getProperty("polling.timeout.millis"));
        }

        public void startPolling() {
            Thread pollingThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        ReceiveMessageResponse response = sqsClient.receiveMessage(builder -> builder.queueUrl(queueUrl).maxNumberOfMessages(10));

                        List<Message> messages = response.messages();
                        for (Message message : messages) {
                            String messageId = message.messageId();
                            messageMap.put(messageId, message);
                           // log.info("Received message: " + message.body());
                            sqsClient.deleteMessage(builder -> builder.queueUrl(queueUrl).receiptHandle(message.receiptHandle()));
                        }

                        // Attesa prima del nuovo polling
                        Thread.sleep(pollingTimeoutMillis);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Avvia il thread di polling
            pollingThread.start();

        }


    public static void main(String[] args) {
        QueuePoller queuePoller = QueuePoller.getInstance();
        queuePoller.setPollingTimeout(Long.parseLong(System.getProperty("polling.timeout.millis")));
        queuePoller.startPolling();
    }

}
