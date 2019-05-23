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
            .setSasKey("...")

        // Endpoint=sb://iothub-ns-csucsa-iot-1356663-1595b19cba.servicebus.windows.net/;SharedAccessKeyName=iothubowner;SharedAccessKey=6f2W/C/Hz5TuS0X6UuiJw6MUQiwrMI0JmYXB5DoYZ00=;EntityPath=csucsa-iot-demo
        //var ehClient = EventHubClient.createSync(connStr.toString(), executor)
        var ehClient = EventHubClient.createSync("", executor)

        val eventHubInfo = ehClient.getRuntimeInformation().get()
        val partitionId = eventHubInfo.getPartitionIds()[0] // get first partition's id

        //val partitionId = "1"

        val receiver = ehClient.createReceiverSync(
            EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
            partitionId,
            EventPosition.fromStartOfStream()
//            EventPosition.fromEnqueuedTime(instant)
        )

        //receiver.receiveTimeout = Duration.ofSeconds(5)
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