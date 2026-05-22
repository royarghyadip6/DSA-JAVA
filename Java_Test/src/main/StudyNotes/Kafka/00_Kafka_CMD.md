# Kafka Commands

## Run Zookeeper

```bash
docker run -d --name zookeeper -p 2181:2181 -e ZOOKEEPER_CLIENT_PORT=2181 -e ZOOKEEPER_TICK_TIME=2000 confluentinc/cp-zookeeper:7.5.0
```
## Run Kafka

```bash
docker run -d --name kafka -p 9092:9092 -e KAFKA_BROKER_ID=1 -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 --link zookeeper confluentinc/cp-kafka:7.5.0
```

## List Topics

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --list
```
## Create Topic

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic my-topic --partitions 3 --replication-factor 1
``` 
## Produce Messages

```bash
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic my-topic
```
## Consume Messages

```bash
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic my-topic --from-beginning
```
## Describe Topic

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic my-topic
```
## Delete Topic

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic my-topic
```
## List Consumer Groups
```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```
## Describe Consumer Group
```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group my-group
```

## Reset Consumer Group Offset
```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --reset-offsets --to-earliest --group my-group --topic my-topic
```
## Check Broker Status
```bash
kafka-broker-api-versions.sh --bootstrap-server localhost:9092
```

## Check Zookeeper Status
```bash
zookeeper-shell.sh localhost:2181
```
## Check Kafka Logs
```bash
docker logs kafka
```

## Check Zookeeper Logs
```bash
docker logs zookeeper
```