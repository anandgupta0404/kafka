[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Custom Principal Builder

This repository contains an example of how you can build and use a custom principal builder class in [Strimzi-powered Apache Kafka clusters](https://strimzi.io).
It provides a custom principal builder that:
* It keeps the default mapping rules for mTLS users for the replication and control plane listeners used by internal Strimzi components such as the operators, Kafka nodes etc.. 
  That is important to keep Strimzi working.
* But it allows to customize the mapping rules for the other listeners.
  In the example in this repository, the users will be mapped to `<Certificate-CN>@my-cluster`.

## How to use the custom principal builder?

1. Customize the mapping rules in any way you want.
   The custom rule is currently stored inside the code in the `CUSTOM_SSL_PRINCIPAL_MAPPING_RULES` constant in the `CustomPrincipalBuilder` class.
   The [Apache Kafka documentation](https://kafka.apache.org/documentation/#security_authz_ssl) has more details on how the rules should look like.
2. Build the project with Maven:
   ```
   mvn clean package
   ```
3. Edit the `Dockerfile` file and update the `FROM` defintion to match the Strimzi and Kafka version you want to use.
4. Build a new custom container image with the JAR and push it into your container repository.
   For example:
   ```
   docker build -t quay.io/scholzj/kafka:0.38.0-kafka-3.6.0 .
   docker push quay.io/scholzj/kafka:0.38.0-kafka-3.6.0
   ```
5. Use the image in your Kafka cluster and configure the custom principal builder class:
   ```yaml
   apiVersion: kafka.strimzi.io/v1beta2
   kind: Kafka
   metadata:
     name: my-cluster
   spec:
     # ...
     kafka:
       image: quay.io/scholzj/kafka:0.38.0-kafka-3.6.0
       config:
         principal.builder.class: cz.scholz.customprincipalbuilder.CustomPrincipalBuilder
       # ...  
   ```