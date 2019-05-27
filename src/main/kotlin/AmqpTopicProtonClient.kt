import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.jms.Connection
import javax.jms.ConnectionFactory
import javax.jms.Destination
import javax.jms.Session
import javax.naming.Context
import javax.naming.InitialContext

/**
 * status: connection is working but still no data processed.
 */
class AmqpTopicProtonClient {

    fun receiveAmqp(connection: String) {

        val csb = ConnectionStringBuilder(connection)

        val hashtable = Hashtable<String, String>()
        hashtable["connectionfactory.SBCF"] = "amqps://" + csb.getEndpoint().getHost() + "?amqp.idleTimeout=120000&amqp.traceFrames=true"
        hashtable["topic.TOPIC"] = "csucsa-iot-demo/ConsumerGroups/\$Default/Partitions/2" // <EventHubName>/ConsumerGroups/<ConsumerGroupName>/Partitions/<PartitionNumber>
        hashtable[Context.INITIAL_CONTEXT_FACTORY] = "org.apache.qpid.jms.jndi.JmsInitialContextFactory"
        val context = InitialContext(hashtable)

        val cf = context.lookup("SBCF") as ConnectionFactory
        val topic = context.lookup("TOPIC") as Destination
        val connection = cf.createConnection(csb.getSasKeyName(), csb.getSasKey())
        connection.start()
        val session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE)
        val consumer = session.createConsumer(topic)

        consumer.setMessageListener { message ->
            try {
                val messageData = message.getBody(Any::class.java)
                println("Received message: $messageData.toString()")
                message.acknowledge()
            } catch (e: Exception) {
                System.out.printf("%s", e.toString())
            }
        }
        readLine()
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            AmqpTopicProtonClient().receiveAmqp(args[0])
        }
    }
}