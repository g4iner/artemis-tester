# artemis-tester

Example Artemis sender and listener implementation with JMS, Spring Boot which causes AMQ224016 error on server side of Artemis ActiveMQ (even using latest 2.32.0 version with default configuration, newly created single instance).

Application sends example message to queue and then listens to same queue.

Stacktrace:
```
2024-03-18 15:16:41,914 ERROR [org.apache.activemq.artemis.core.server] AMQ224016: Caught exception
org.apache.activemq.artemis.api.core.ActiveMQIllegalStateException: AMQ229027: Could not find reference on consumer ID=0, messageId = 30086218024 queue = example.queue
	at org.apache.activemq.artemis.core.server.impl.ServerConsumerImpl.individualAcknowledge(ServerConsumerImpl.java:1013) ~[artemis-server-2.32.0.jar:2.32.0]
	at org.apache.activemq.artemis.core.server.impl.ServerSessionImpl.individualAcknowledge(ServerSessionImpl.java:1314) ~[artemis-server-2.32.0.jar:2.32.0]
	at org.apache.activemq.artemis.core.protocol.core.ServerSessionPacketHandler.slowPacketHandler(ServerSessionPacketHandler.java:618) ~[artemis-server-2.32.0.jar:2.32.0]
	at org.apache.activemq.artemis.core.protocol.core.ServerSessionPacketHandler.onMessagePacket(ServerSessionPacketHandler.java:319) ~[artemis-server-2.32.0.jar:2.32.0]
	at org.apache.activemq.artemis.utils.actors.Actor.doTask(Actor.java:32) ~[artemis-commons-2.32.0.jar:2.32.0]
	at org.apache.activemq.artemis.utils.actors.ProcessorBase.executePendingTasks(ProcessorBase.java:68) ~[artemis-commons-2.32.0.jar:2.32.0]
	at org.apache.activemq.artemis.utils.actors.OrderedExecutor.doTask(OrderedExecutor.java:57) ~[artemis-commons-2.32.0.jar:2.32.0]
	at org.apache.activemq.artemis.utils.actors.OrderedExecutor.doTask(OrderedExecutor.java:32) ~[artemis-commons-2.32.0.jar:2.32.0]
	at org.apache.activemq.artemis.utils.actors.ProcessorBase.executePendingTasks(ProcessorBase.java:68) ~[artemis-commons-2.32.0.jar:2.32.0]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128) [?:?]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628) [?:?]
	at org.apache.activemq.artemis.utils.ActiveMQThreadFactory$1.run(ActiveMQThreadFactory.java:118) [artemis-commons-2.32.0.jar:2.32.0]
```

After downgrading back to Spring Boot 2.7.18 along with "javax" imports the problem is gone.
