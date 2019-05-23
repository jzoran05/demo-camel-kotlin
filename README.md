# TOC
- Camel Console Example
- AMQP - Azure Service Bus JMS Topics Quickstart
- AMQP - Azure Service Bus JMS Queue Quickstart - qpid client JMS for AMQP (to test connectivity to Azure IoT Hub)
- AMQP - qpid Proton (Reactive) client (to test connectivity to Azure IoT Hub)
- AMQP Camel component (test options to connect to Azure IoT Hub)

## Camel Console Example

### Introduction

This is a simple example that shows how to get started with Camel.

This is a beginner's example that demonstrates how to get started with Apache Camel.
In this example we integrate with the console using the Stream component. 
The example is interactive - it reads input from the console, and then transforms the input to upper case and prints it back to the console.

This is implemented with a Camel route defined in the Spring XML 

### Build

You will need to compile this example first:

	mvn compile

### Run

To run the example type

	mvn camel:run

You can see the routing rules by looking at the XML in the directory:
  `src/main/resources/META-INF/spring`

To stop the example hit <kbd>ctrl</kbd>+<kbd>c</kbd>

You can also run the example from your editor such as Eclipse, IDEA etc,
by opening the org.apache.camel.example.console.CamelConsoleMain class
and then right click, and chose run java application.

### Forum, Help, etc

If you hit an problems please let us know on the Camel Forums
	<http://camel.apache.org/discussion-forums.html>

Please help us make Apache Camel better - we appreciate any feedback you may
have.  Enjoy!


The Camel riders!


# Azure Service Bus JMS Topics Quickstart

This sample demonstrates how to use Azure Service Bus Topics with the Java Message Service (JMS) API, 
implemented via the AMQP 1.0 to JMS mapping provcided by the Apache Qpid JMS client. The Apache Qpid 
JMS client is maintained by the Apache Foundation's Qpid Proton project.

Using Azure Service Bus topics and subscriptions through the JMS API provides basic send and receive 
capabilities and is a convenient choice when porting applications from other message brokers with JMS 
compliant APIs, even though Service Bus Topics differ from JMS Topics and require a few adjustments 
discussed below.

For cloud-native applications and applications that want to take advantage of more advanced Service Bus 
capabilities, teh native Service Bus API provides deeper and more direct access to the service 
capabilities. 

## Prerequisites

Please refer to the [overview README](../../readme.md) for prerequisites and setting up the samples 
environment, including creating a Service Bus cloud namespace. 

## JMS Topics vs. Service Bus Topics

Azure Service Bus topics route messages into named, shared, durable subscriptions that are managed through
the Azure Resource Management interface, the Azure command line tools, or through the Azure portal. Each
subscription allows for up to 2000 selection rules, each of which may have a filter condition and, for SQL 
filters, also a metadata transformation action. Each filter condition match selects the input message to 
be copied into tehj subscription.  

Receiving messages from subscriptions is identical receiving messages from queues. Each subscription 
has an associated dead-letter queue as well as the ability to automatically forward messages to another 
queue or topics. 

JMS Topics allow clients to dynamically create nondurable and durable subscribers that optionally allow 
filtering  messages with message selectors. These unshared entities are not supported by Service Bus.
The SQL filter rule syntax for Service Bus is, however, very similar to the message selector syntax 
supported by JMS. 

The JMS Topic publisher side is compatible with Service Bus, as shown in this sample, but 
dynamic subscribers are not. The following topology-related JMS APIs are not supported with Service Bus. 


| Unsupported method          | Replace with                                                                             |
|-----------------------------|------------------------------------------------------------------------------------------|
| createDurableSubscriber     | create a Topic subscription porting the message selector                                 |
| createDurableConsumer       | create a Topic subscription porting the message selector                                 |
| createSharedConsumer        | Service Bus topics are always shareable, see above                                       |
| createSharedDurableConsumer | Service Bus topics are always shareable, see above                                       |
| createTemporaryTopic        | create a topic via management API/tools/portal with *AutoDeleteOnIdle* set to an expiration period |
| createTopic                 | create a topic via management API/tools/portal                                           |
| unsubscribe                 | delete the topic management API/tools/portal                                             |
| createBrowser               | unsupported. Use the Peek() functionality of the Service Bus API                         |
| createQueue                 | create a queue via management API/tools/portal                                           | 
| createTemporaryQueue        | create a queue via management API/tools/portal with *AutoDeleteOnIdle* set to an expiration period |


## Build and run

The sample can be built independently with 

```bash
mvn clean package 
```

and then run with (or just from VS Code or another Java IDE)

```bash
java -jar ./target/azure-servicebus-samples-jmsqueuequickstart-1.0.0-jar-with-dependencies.jar
```

The sample accept two arguments that can either be supplied on the command line or via environment
variables. The setup script discussed in the overview readme sets the environment variables for you.

* -c (env: SB_SAMPLES_CONNECTIONSTRING) - Service Bus connection string with credentials or 
                                          token granting send and listen rights for the namespace
* -t (env: SB_SAMPLES_TOPICNAME)        - Name of an existing topic within the namespace
* -s (env: SB_SAMPLES_SUBSCRIPTIONNAME) - Name of an existing subscription on the given topic

## Sample Code Explained

For a discussion of the sample code, review the inline comments in [JmsTopicQuickstart.java](./src/main/java/com/microsoft/azure/servicebus/samples/jmstopicquickstart/JmsTopicQuickstart.java)
