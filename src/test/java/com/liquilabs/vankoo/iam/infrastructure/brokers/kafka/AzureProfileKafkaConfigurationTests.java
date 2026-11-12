package com.liquilabs.vankoo.iam.infrastructure.brokers.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

/**
 * Comprueba cómo resuelve el perfil {@code azure} la conexión con Azure Event Hubs a
 * partir de las variables de la Container App, sin levantar el contexto ni el broker.
 */
class AzureProfileKafkaConfigurationTests {

    private static final String BINDER = "spring.cloud.stream.kafka.binder.";

    @Test
    void keepsEventsOffWithoutTheKafkaVariables() throws IOException {
        StandardEnvironment environment = azureEnvironment(Map.of());

        assertThat(environment.getProperty("vankoo.events.enabled")).isEqualTo("false");
        assertThat(environment.getProperty("management.health.binders.enabled")).isEqualTo("false");
    }

    @Test
    void connectsToEventHubsWithTheContainerAppVariables() throws IOException {
        String connectionString = "Endpoint=sb://vankoo-events.servicebus.windows.net/;"
                + "SharedAccessKeyName=iam;SharedAccessKey=secret";
        StandardEnvironment environment = azureEnvironment(Map.of(
                "KAFKA_ENABLED", "true",
                "KAFKA_BROKERS", "vankoo-events.servicebus.windows.net:9093",
                "KAFKA_SASL_PASSWORD", connectionString));

        assertThat(environment.getProperty("vankoo.events.enabled")).isEqualTo("true");
        assertThat(environment.getProperty("management.health.binders.enabled")).isEqualTo("true");
        assertThat(environment.getProperty(BINDER + "brokers"))
                .isEqualTo("vankoo-events.servicebus.windows.net:9093");
        assertThat(environment.getProperty(BINDER + "auto-create-topics")).isEqualTo("false");
        assertThat(environment.getProperty(BINDER + "configuration.security.protocol")).isEqualTo("SASL_SSL");
        assertThat(environment.getProperty(BINDER + "configuration.sasl.mechanism")).isEqualTo("PLAIN");
        assertThat(environment.getProperty(BINDER + "configuration.sasl.jaas.config"))
                .isEqualTo("org.apache.kafka.common.security.plain.PlainLoginModule required "
                        + "username=\"$ConnectionString\" password=\"" + connectionString + "\";");
        assertThat(environment.getProperty("spring.cloud.stream.bindings.iam-out-0.destination"))
                .isEqualTo("vankoo.iam.events");
    }

    @Test
    void usesTheTimeoutsAndRequestSizeThatEventHubsRequires() throws IOException {
        StandardEnvironment environment = azureEnvironment(Map.of());

        assertThat(environment.getProperty(BINDER + "configuration.request.timeout.ms", Integer.class))
                .isGreaterThanOrEqualTo(60_000);
        assertThat(environment.getProperty(BINDER + "configuration.metadata.max.age.ms", Integer.class))
                .isLessThan(240_000);
        assertThat(environment.getProperty(BINDER + "configuration.connections.max.idle.ms", Integer.class))
                .isLessThan(240_000);
        assertThat(environment.getProperty(BINDER + "producer-properties.max.request.size", Integer.class))
                .isLessThan(1_046_528);
    }

    private static StandardEnvironment azureEnvironment(Map<String, Object> variables) throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().addFirst(new MapPropertySource("containerApp", variables));
        new YamlPropertySourceLoader()
                .load("application-azure", new ClassPathResource("application-azure.yaml"))
                .forEach(environment.getPropertySources()::addLast);
        return environment;
    }
}
