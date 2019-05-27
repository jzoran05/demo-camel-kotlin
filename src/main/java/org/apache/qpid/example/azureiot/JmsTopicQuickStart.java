
package org.apache.qpid.example.azureiot;
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * This sample demonstrates how to send messages from a JMS Topic producer into
 * an Azure Service Bus Topic, and receive the message from a Service Bus topic
 * subscription using a message consumer that treats the subscription as a
 * JMS Queue.
 */
public class JmsTopicQuickStart  {

    // number of messages to send
    private static int totalSend = 10;
    // log4j logger
    private static Logger logger = org.apache.logging.log4j.LogManager.getLogger();

    public void run(String connectionString) throws Exception {


        // The connection string builder is the only part of the azure-servicebus SDK library
        // we use in this JMS sample and for the purpose of robustly parsing the Service Bus
        // connection string.
        ConnectionStringBuilder csb = new ConnectionStringBuilder(connectionString);

        // set up the JNDI context
        Hashtable<String, String> hashtable = new Hashtable<>();
        hashtable.put("connectionfactory.SBCF", "amqps://" + csb.getEndpoint().getHost() + "?amqp.idleTimeout=120000&amqp.traceFrames=true");
        hashtable.put("topic.TOPIC", "ConsumerGroups/$Default/Partitions/1");
        //hashtable.put("queue.SUBSCRIPTION1", "BasicTopic/Subscriptions/Subscription1");
        //hashtable.put("queue.SUBSCRIPTION2", "BasicTopic/Subscriptions/Subscription2");
        //hashtable.put("queue.SUBSCRIPTION3", "BasicTopic/Subscriptions/Subscription3");
        hashtable.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        Context context = new InitialContext(hashtable);

        ConnectionFactory cf = (ConnectionFactory) context.lookup("SBCF");

        // Look up the topic
        Destination topic = (Destination) context.lookup("TOPIC");


        // Look up the subscription (pretending it's a queue)
        receiveFromSubscription(csb, context, cf, "TOPIC");
        //receiveFromSubscription(csb, context, cf, "SUBSCRIPTION2");
        //receiveFromSubscription(csb, context, cf, "SUBSCRIPTION3");

        System.out.printf("Received all messages, exiting the sample.\n");
        System.out.printf("Closing queue client.\n");
    }

    private void receiveFromSubscription(ConnectionStringBuilder csb, Context context, ConnectionFactory cf, String name)
            throws NamingException, JMSException, InterruptedException {
        AtomicInteger totalReceived = new AtomicInteger(0);
        System.out.printf("Subscription %s: \n", name);

        Destination subscription = (Destination) context.lookup(name);
        // Create Connection
        Connection connection = cf.createConnection(csb.getSasKeyName(), csb.getSasKey());
        connection.start();
        // Create Session, no transaction, client ack
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        // Create consumer
        MessageConsumer consumer = session.createConsumer(subscription);
        // Set callback listener. Gets called for each received message.
        consumer.setMessageListener(message -> {
            try {
                System.out.printf("Received message %d with sq#: %s\n",
                        totalReceived.incrementAndGet(),  // increments the counter
                        message.getJMSMessageID());
                message.acknowledge();
            } catch (Exception e) {
                System.out.printf("%s", e.toString());
            }
        });

        // wait on the main thread until all sent messages have been received
        while (totalReceived.get() < totalSend) {
            Thread.sleep(1000);
        }
        consumer.close();
        session.close();
        connection.stop();
        connection.close();
    }

    public static void main(String[] args) {

        System.exit(runApp(args, (connectionString) -> {
            JmsTopicQuickStart app = new JmsTopicQuickStart();
            try {
                app.run(connectionString);
                return 0;
            } catch (Exception e) {
                System.out.printf("%s", e.toString());
                return 1;
            }
        }));
    }

    static final String SB_SAMPLES_CONNECTIONSTRING = "SB_SAMPLES_CONNECTIONSTRING";

    public static int runApp(String[] args, Function<String, Integer> run) {
        try {

            String connectionString = null;

            // parse connection string from command line
            Options options = new Options();
            options.addOption(new Option("c", true, "Connection string"));
            CommandLineParser clp = new DefaultParser();
            CommandLine cl = clp.parse(options, args);
            if (cl.getOptionValue("c") != null) {
                connectionString = cl.getOptionValue("c");
            }

            // get overrides from the environment
            String env = System.getenv(SB_SAMPLES_CONNECTIONSTRING);
            if (env != null) {
                connectionString = env;
            }

            if (connectionString == null) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("run jar with", "", options, "", true);
                return 2;
            }
            return run.apply(connectionString);
        } catch (Exception e) {
            System.out.printf("%s", e.toString());
            return 3;
        }
    }
}
