import com.microsoft.azure.eventhubs.*
import java.util.concurrent.Executors
import com.microsoft.azure.eventhubs.EventHubClient
import com.microsoft.azure.eventhubs.EventPosition
import com.microsoft.azure.eventhubs.PartitionReceiver
import java.time.Duration
import com.microsoft.azure.eventhubs.EventData
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*


class AmqpTopicEventHubsSdkClient {


    fun receive(saasKey: String) {

        val executor = Executors.newScheduledThreadPool(4)

        var connStr = ConnectionStringBuilder()
            .setNamespaceName("iothub-ns-csucsa-iot-1356663-1595b19cba")
            .setEventHubName("csucsa-iot-demo")
            .setSasKeyName("iothubowner")
            .setSasKey(saasKey)

        // Endpoint=sb://iothub-ns-csucsa-iot-1356663-1595b19cba.servicebus.windows.net/;SharedAccessKeyName=iothubowner;SharedAccessKey=6f2W/C/Hz5TuS0X6UuiJw6MUQiwrMI0JmYXB5DoYZ00=;EntityPath=csucsa-iot-demo
        //var ehClient = EventHubClient.createSync(connStr.toString(), executor)
        var ehClient = EventHubClient.createSync(connStr.toString(), executor)

        val eventHubInfo = ehClient.getRuntimeInformation().get()
        val partitionId = eventHubInfo.getPartitionIds()[2] // get first partition's id

        //val partitionId = "1"

        val receiver = ehClient.createEpochReceiverSync(
            EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
            partitionId,
            EventPosition.fromStartOfStream(), //            EventPosition.fromEnqueuedTime(instant)
            2345L
        )

        receiver.receiveTimeout = Duration.ofSeconds(10)

        try {
            var reciverCount = 0L
            while (reciverCount++ < 100) {
                receiver.receive(100).thenAcceptAsync { data ->
                    data.forEach {
                            event -> run {
                                println("New event received...")
                                println(event.systemProperties)
                            }
                    }
                }
            }
        } finally {
            // cleaning up receivers is paramount;
            // Quota limitation on maximum number of concurrent receivers per consumergroup per partition is 5
            receiver.close()
            ehClient.close()
            executor.shutdown()
        }

        //var receivedEvents = receiver.receiveSync(450)

        /*
        receivedEvents?.forEach {
            println("The element is $it")
        }
         */

        println("Receiving completed...")

    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            AmqpTopicEventHubsSdkClient().receive(args[0])
        }
    }
}