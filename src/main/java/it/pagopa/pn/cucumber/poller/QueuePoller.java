package it.pagopa.pn.cucumber.poller;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import jakarta.jms.*;
import lombok.CustomLog;
import software.amazon.awssdk.services.sqs.SqsClient;

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
        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                SqsClient.create()
        );

        // Create the connection.
        if (this.connection == null)
            this.connection = connectionFactory.createConnection();

        try (Session session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            Queue queue = session.createQueue(this.queueName);
            MessageConsumer consumer = session.createConsumer(queue);
            consumer.setMessageListener(this);
        }

        // Start receiving incoming messages.
        this.connection.start();
    }

    public void close() throws JMSException {
        this.connection.close();
    }

}
