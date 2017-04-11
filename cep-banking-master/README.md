
# cep-banking-use-case-impl

1. Sections below showcase an end-to-end CEP implementation scenario.
2. Project provides a REST API which can be invoked via Postman. See accompanying document for the use-case and design details.
3. A service module that implements a CEP use-case.

### Cassandra setup and some useful tips

1. The Correct Github URL for AKKA persistence is https://github.com/akka/akka-persistence-cassandra 
2. Download and install DataStax Cassandra community v3.0.9 on windows and enterprise version 3.9.0 on Mac from here https://academy.datastax.com/planet-cassandra//archived-versions-of-datastaxs-distribution-of-apache-cassandra/ 
3. Start DataStax Cassandra as a service.
4. A sample Cassandra akka conf is present here for reference, which can be overridden for the needed values. https://github.com/akka/akka-persistence-cassandra/blob/v0.17/src/main/resources/reference.conf
5. If old event sourced messages need to be deleted, then best option is below: 
    1. Login to CQL shell and execute below commands
    2. use akka;
    3. truncate messages;
    4. use akka_snapshot;
    5. truncate snapshots;
    6. All CQL used by Cassandra storage can be found here https://github.com/krasserm/akka-persistence-cassandra/blob/cassandra-3.x/src/main/scala/akka/persistence/cassandra/journal/CassandraStatements.scala#L16-L31
    7. Default table names are here https://github.com/krasserm/akka-persistence-cassandra
6. If messages need to be deleted in actor and remove from journal, to avoid replay, follow this approach.  http://doc.akka.io/docs/akka/2.4.17/scala/persistence.html#Message_deletion 

### Basic setup and bring up the application.

1. This project runs using standard Spring boot and Maven with maven dependency management best practice implemented.
2. Open the project in your editor of choice (IntelliJ is recommended) as a maven project.
3. The project will be imported as a Maven project, below single step configuration can start the application in a laptop.
4. Run `cep-banking-api/src/main/java/com/cep/api/Application` 
    1. Then edit configuration and provide the following values according to the local path on your machine and rerun the application again
    On Windows:
    ```
    -Xbootclasspath/p:C:\software\workspace\cep-master\cep-banking-use-case-impl\cep-banking-configuration   
    -Dspring.profiles.active=local
    ```
    On Mac:
    ```
    -Xbootclasspath/p:/software/workspace/distributed-computing/cep-banking-master/cep-banking-configuration   -Dspring.profiles.active=local
    ```
