/*
 * Copyright Jakub Scholz.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package cz.scholz.customprincipalbuilder;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.internals.BrokerSecurityConfigs;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.security.auth.AuthenticationContext;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.security.auth.KafkaPrincipalBuilder;
import org.apache.kafka.common.security.auth.KafkaPrincipalSerde;
import org.apache.kafka.common.security.authenticator.DefaultKafkaPrincipalBuilder;
import org.apache.kafka.common.security.ssl.SslPrincipalMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Custom Principal Builder class that can be used with Strimzi-based Kafka clusters. It keeps the default mapping rules
 * for mTLS users for the internal listeners used by Strimzi components. That is important to keep Strimzi working. But
 * it allows to customize the mapping rules for the other listeners using the {@code CUSTOM_SSL_PRINCIPAL_MAPPING_RULES}
 * constant.
 */
public class CustomPrincipalBuilder implements KafkaPrincipalBuilder, KafkaPrincipalSerde {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPrincipalBuilder.class);
    private static final Set<String> INTERNAL_LISTENERS = Set.of("CONTROLPLANE-9090", "REPLICATION-9091");

    // Customize the mapping rule to match you you want to do. This example matches the regular Strimzi users and givens
    // them the user principal <Username>@my-cluster. But you can customize it to for example remove some parts of the
    // distinguished name etc.
    private static final String CUSTOM_SSL_PRINCIPAL_MAPPING_RULES = "RULE:^CN=([a-zA-Z0-9.-]+),C=IN,O=MY ORG,ST=MH$/$1/";

    private final DefaultKafkaPrincipalBuilder internalPrincipalBuilder;
    private final DefaultKafkaPrincipalBuilder userDefinedPrincipalBuilder;

    /**
     * Constructs the Custom Principal Builder
     */
    public CustomPrincipalBuilder() {
        LOGGER.info("Creating new CustomPrincipalBuilder instance with default mapping");
        internalPrincipalBuilder = new DefaultKafkaPrincipalBuilder(null, SslPrincipalMapper.fromRules(BrokerSecurityConfigs.DEFAULT_SSL_PRINCIPAL_MAPPING_RULES));

        LOGGER.info("Configuring CustomPrincipalBuilder with custom mapping rules {}", CUSTOM_SSL_PRINCIPAL_MAPPING_RULES);
        userDefinedPrincipalBuilder = new DefaultKafkaPrincipalBuilder(null, SslPrincipalMapper.fromRules(CUSTOM_SSL_PRINCIPAL_MAPPING_RULES));
    }

    @Override
    public KafkaPrincipal build(AuthenticationContext context) {
        if (INTERNAL_LISTENERS.contains(context.listenerName()))    {
            return internalPrincipalBuilder.build(context);
        } else {
            if (userDefinedPrincipalBuilder != null)    {
                return userDefinedPrincipalBuilder.build(context);
            } else {
                throw new KafkaException("Custom Principal Builder was not initialized yet!");
            }
        }
    }

    @Override
    public byte[] serialize(KafkaPrincipal principal) throws SerializationException {
        // Serialization does not depend on the mapping rules, so we can just use the serialization from the internal one
        return internalPrincipalBuilder.serialize(principal);
    }

    @Override
    public KafkaPrincipal deserialize(byte[] bytes) throws SerializationException {
        // Deserialization does not depend on the mapping rules, so we can just use the deserialization from the internal one
        return internalPrincipalBuilder.deserialize(bytes);
    }
}