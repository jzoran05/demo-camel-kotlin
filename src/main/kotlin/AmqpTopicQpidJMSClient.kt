import org.apache.qpid.jms.JmsTopicSession
import java.util.*
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
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.net.URLEncoder
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.jms.Queue


class AmqpTopicQpidJMSClient {


    private fun generate_sas_token(resourceUri: String, keyName: String, key: String): String? {
        val epoch = System.currentTimeMillis() / 1000L
        val week = 60 * 60 * 24 * 7
        val expiry = java.lang.Long.toString(epoch + week)

        var sasToken: String? = null
        try {
            val stringToSign = URLEncoder.encode(resourceUri, "UTF-8") + "\n" + expiry
            val signature = getHMAC256(key, stringToSign)
            sasToken = "SharedAccessSignature sr=" + URLEncoder.encode(resourceUri, "UTF-8") + "&sig=" +
                    URLEncoder.encode(signature, "UTF-8") + "&se=" + expiry + "&skn=" + keyName
        } catch (e: UnsupportedEncodingException) {

            e.printStackTrace()
        }

        return sasToken
    }


    fun getHMAC256(key: String, input: String): String? {
        var sha256_HMAC: Mac? = null
        var hash: String? = null
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256")
            val secret_key = SecretKeySpec(key.toByteArray(), "HmacSHA256")
            sha256_HMAC!!.init(secret_key)
            val encoder = Base64.getEncoder()

            hash = String(encoder.encode(sha256_HMAC!!.doFinal(input.toByteArray(charset("UTF-8")))))

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return hash
    }

    private class MyExceptionListener : ExceptionListener {
        override fun onException(exception: JMSException) {
            println("Connection ExceptionListener fired, exiting.")
            exception.printStackTrace(System.out)
            System.exit(1)
        }
    }

    fun receive() {


        val iot_hub_name = "csucsa-iot-demo"
        val hostname = "$iot_hub_name.azure-devices.net"
        val policy_name = "iothubowner" // old value service
        val access_key = "..."
        val consumer_group="\$Default"
        val p_id = "0"
        val operation = "/messages/events/ConsumerGroups/$consumer_group/Partitions/$p_id"
        val username = "$policy_name@sas.root.$iot_hub_name"
        val sas_token = generate_sas_token(hostname, access_key, policy_name)


        val hashtable = Hashtable<String, String>()
        hashtable["connectionfactory.myFactoryLookup"] = "amqps://$policy_name:$sas_token@$hostname"
        hashtable["topic.myTopicLookup"] = "/messages/events/ConsumerGroups/\$Default/Partitions/0"
        hashtable[Context.INITIAL_CONTEXT_FACTORY] = "org.apache.qpid.jms.jndi.JmsInitialContextFactory"
        val context = InitialContext(hashtable)

        val factory = context.lookup("myFactoryLookup") as ConnectionFactory
        val queue = context.lookup("myTopicLookup") as Queue

        // connectionfactory.myFactoryLookup = amqps://[SASPolicyName]:[SASPolicyKey]@[hostname]
        val connection = factory.createConnection(username, sas_token)
        connection.exceptionListener = MyExceptionListener()
        connection.start()

        val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE) as JmsTopicSession

        //val messageConsumer = session.createConsumer(queue)
        val messageConsumer = session.createReceiver(queue)

        //val endpoint_filter = b'amqp.annotation.x-opt-sequence-number > -1'

        val message = messageConsumer.receive()
        println(message)
        //val uri = "amqps://{}:{}@{}{}".format(urllib.quote_plus(username), urllib.quote_plus(sas_token), hostname, operation)

        /*
        # Optional filtering predicates can be specified using endpiont_filter
        # Valid predicates include:
        # - amqp.annotation.x-opt-sequence-number
        # - amqp.annotation.x-opt-offset
        # - amqp.annotation.x-opt-enqueued-time
        # Set endpoint_filter variable to None if no filter is needed
        val endpoint_filter = b'amqp.annotation.x-opt-sequence-number > -1'
        */

        /*
        val receive_client = uamqp.ReceiveClient(set_endpoint_filter(uri, endpoint_filter), debug=True)
        try:
        batch = receive_client.receive_message_batch(max_batch_size=5)
        except uamqp.errors.LinkRedirect as redirect:
        //# Once a redirect error is received, close the original client and recreate a new one to the re-directed address
        receive_client.close()

        sas_auth = uamqp.authentication.SASTokenAuth.from_shared_access_key(redirect.address, policy_name, access_key)
        receive_client = uamqp.ReceiveClient(set_endpoint_filter(redirect.address, endpoint_filter), auth=sas_auth, debug=True)

        # Start receiving messages in batches
        batch = receive_client.receive_message_batch(max_batch_size=5)
        for msg in batch:
        print('*** received a message ***')
        print(''.join(msg.get_data()))
        print('\t: ' + str(msg.annotations['x-opt-sequence-number']))
        print('\t: ' + str(msg.annotations['x-opt-offset']))
        print('\t: ' + str(msg.annotations['x-opt-enqueued-time']))

         */
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            AmqpTopicQpidJMSClient().receive()
        }
    }
    /*
        // # Helper function to set the filtering predicate on the source URI
        fun set_endpoint_filter(uri: String, endpoint_filter: String ="") {
            val source_uri = uamqp.address.Source(uri)
            val source_uri.set_filter(endpoint_filter)
            return source_uri
        }
*/
}