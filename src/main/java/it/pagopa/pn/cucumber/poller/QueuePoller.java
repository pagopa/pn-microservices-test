package it.pagopa.pn.cucumber.poller;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import jakarta.jms.*;
import lombok.CustomLog;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@CustomLog
public abstract class QueuePoller implements MessageListener {
    protected String queueName;
    protected final ConcurrentHashMap<String, Set<String>> messageMap;
    private SQSConnection connection;

    protected QueuePoller() {
        this.messageMap = new ConcurrentHashMap<>();
    }

    protected QueuePoller(String queueName) {
        this.messageMap = new ConcurrentHashMap<>();
        this.queueName = queueName;
    }


    public void startPolling() throws JMSException {
        // Create a new connection factory with all defaults (credentials and region) set automatically
        SqsClientBuilder builder = SqsClient.builder().region(Region.EU_SOUTH_1).credentialsProvider(DefaultCredentialsProvider.create());
        String testAwsSqsEndpoint = System.getProperty("test.aws.sqs.endpoint");
        if (testAwsSqsEndpoint != null) {
            builder.endpointOverride(URI.create(testAwsSqsEndpoint));
        }

        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                builder.build()
        );

        // Create the connection.
        if (this.connection == null)
            this.connection = connectionFactory.createConnection();

        Session session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(this.queueName);

        //Create a consumer for the queue.
        MessageConsumer consumer = session.createConsumer(queue);

        //Instantiate and set the message listener for the consumer.
        consumer.setMessageListener(this);

        // Start receiving incoming messages.
        this.connection.start();
    }

    public void close() throws JMSException {
        this.connection.close();
    }

}
