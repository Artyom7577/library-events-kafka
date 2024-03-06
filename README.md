# Kafka Event Streaming System with H2 Database Integration

This project demonstrates a simple Kafka event streaming system with H2 database integration. The system includes components for Kafka, Zookeeper, a Kafka producer, a Kafka consumer, and an H2 database.

## Prerequisites

- Docker
- Docker Compose
- JDK 17

## Setup

1. **Clone the Repository:**

   ```bash
   https://github.com/Artyom7577/library-events-kafka.git
   ```

2. **Run Docker Compose:**

   Start Zookeeper, Kafka brokers, and the H2 database by running the following command:

   ```bash
   docker-compose up -d
   ```

   This will start three Kafka brokers (`kafka1`, `kafka2`, `kafka3`), one Zookeeper instance (`zoo1`), the Kafka producer, Kafka consumer, and the H2 database.

3. **Wait for Services to Start:**

   Monitor the logs to ensure that Kafka, Zookeeper, and other services are up and running:

   ```bash
   docker-compose logs -f
   ```

   Wait for the Kafka and Zookeeper services to start. Once ready, proceed to run the Kafka producer and consumer.

4. **Run Kafka Producer:**

   ### Build and run the Kafka producer:


5. **Run Kafka Consumer:**

   ### Build and run the Kafka consumer:


6. **Access H2 Database:**

   Visit [http://localhost:8081/h2-console](http://localhost:8081/h2-console) to access the H2 database. Use the connection details specified in your consumer application YAML file to connect to the database.


    