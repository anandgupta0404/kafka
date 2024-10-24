FROM quay.io/strimzi/kafka:0.38.0-kafka-3.6.0-arm64
#FROM quay.io/strimzi/kafka:latest-kafka-3.8.0-arm64
USER root:root

COPY target/custom-principal-builder.jar /opt/kafka/libs/

USER 1001
