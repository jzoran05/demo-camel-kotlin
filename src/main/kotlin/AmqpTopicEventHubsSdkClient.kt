import com.microsoft.azure.eventhubs.*
import java.util.concurrent.Executors
import com.microsoft.azure.eventhubs.EventHubClient
import com.microsoft.azure.eventhubs.EventPosition
import com.microsoft.azure.eventhubs.PartitionReceiver
import java.time.Duration
import com.microsoft.azure.eventhubs.EventData




class AmqpTopicEventHubsSdkClient {


    fun receive() {

        val executor = Executors.newScheduledThreadPool(8)

        var connStr = ConnectionStringBuilder()
            .setNamespaceName("iothub-ns-csucsa-iot-1356663-1595b19cba")
            .setEventHubName("csucsa-iot-demo")
            .setSasKeyName("iothubowner")
            .setSasKey("")

        var ehClient = EventHubClient.createSync(connStr.toString(), executor)

        val partitionId = "0"

        val receiver = ehClient.createReceiverSync(
            EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
            partitionId,
            EventPosition.fromStartOfStream()
//            EventPosition.fromEnqueuedTime(instant)
        )

        receiver.receiveTimeout = Duration.ofSeconds(5)
        val receivedEvents = receiver.receiveSync(10)

        receivedEvents?.forEach {
            println("The element is $it")
        }

        println("Receiving completed...")

    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            AmqpTopicEventHubsSdkClient().receive()
        }
    }
}