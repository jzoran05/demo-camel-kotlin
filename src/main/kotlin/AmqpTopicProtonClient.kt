import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.jms.Connection
import javax.jms.ConnectionFactory
import javax.jms.Destination
import javax.jms.Session
import javax.naming.Context
import javax.naming.InitialContext

class AmqpTopicProtonClient {

    fun receiveAmqp(connection: String) {

        val csb = ConnectionStringBuilder(connection)
        val totalReceived = AtomicInteger(0)
        val hashtable = Hashtable<String, String>()
        hashtable["connectionfactory.SBCF"] = "amqps://" + csb.getEndpoint().getHost() + "?amqp.idleTimeout=120000&amqp.traceFrames=true"
        hashtable["topic.TOPIC"] = "/messages/events/ConsumerGroups/\$Default/Partitions/0"
        hashtable[Context.INITIAL_CONTEXT_FACTORY] = "org.apache.qpid.jms.jndi.JmsInitialContextFactory"
        val context = InitialContext(hashtable)

        val cf = context.lookup("SBCF") as ConnectionFactory

        // Look up the topic
        val topic = context.lookup("TOPIC") as Destination

        val connection = cf.createConnection(csb.getSasKeyName(), csb.getSasKey())
        connection.start()
        // Create Session, no transaction, client ack
        val session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE)

        val consumer = session.createConsumer(topic)

        consumer.setMessageListener { message ->
            try {
                println("Received message %d with sq#: %s\n" + totalReceived.incrementAndGet() + message.jmsMessageID )
                message.acknowledge()
            } catch (e: Exception) {
                System.out.printf("%s", e.toString())
            }
        }
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            AmqpTopicProtonClient().receiveAmqp(args[0])
        }
    }
}