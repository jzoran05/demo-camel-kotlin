package org.apache.qpid.example.azureiot;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

public class JmsTopicReceiver {


    private static final int DEFAULT_COUNT = 10;

    public static void main(String[] args) throws Exception {
        int count = DEFAULT_COUNT;
        if (args.length == 0) {
            System.out.println("Consuming up to " + count + " messages.");
            System.out.println("Specify a message count as the program argument if you wish to consume a different amount.");
        } else {
            count = Integer.parseInt(args[0]);
            System.out.println("Consuming up to " + count + " messages.");
        }

        try {
            // The configuration for the Qpid InitialContextFactory has been supplied in
            // a jndi.properties file in the classpath, which results in it being picked
            // up automatically by the InitialContext constructor.
            Context context = new InitialContext();

            ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
            Destination queue = (Destination) context.lookup("myTopicLookup");

            Connection connection = factory.createConnection("iothubowner", "6f2W/C/Hz5TuS0X6UuiJw6MUQiwrMI0JmYXB5DoYZ00=");
            //Connection connection = factory.createConnection(System.getProperty("USER"), System.getProperty("PASSWORD"));
            connection.setExceptionListener(new MyExceptionListener());
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer messageConsumer = session.createConsumer(queue);

            long start = System.currentTimeMillis();

            int actualCount = 0;
            boolean deductTimeout = false;
            int timeout = 1000;
            for (int i = 1; i <= count; i++, actualCount++) {
                Message message = messageConsumer.receive(timeout);
                if (message == null) {
                    System.out.println("Message " + i + " not received within timeout, stopping.");
                    deductTimeout = true;
                    break;
                }
                if (i % 100 == 0) {
                    System.out.println("Got message " + i);
                }
            }

            long finish = System.currentTimeMillis();
            long taken = finish - start;
            if (deductTimeout) {
                taken -= timeout;
            }
            System.out.println("Received " + actualCount + " messages in " + taken + "ms");

            connection.close();
        } catch (Exception exp) {
            System.out.println("Caught exception, exiting.");
            exp.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static class MyExceptionListener implements ExceptionListener {
        @Override
        public void onException(JMSException exception) {
            System.out.println("Connection ExceptionListener fired, exiting.");
            exception.printStackTrace(System.out);
            System.exit(1);
        }
    }

}
