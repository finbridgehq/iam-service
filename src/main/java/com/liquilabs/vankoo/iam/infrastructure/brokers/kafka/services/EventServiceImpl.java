package com.liquilabs.vankoo.iam.infrastructure.brokers.kafka.services;

import com.liquilabs.vankoo.iam.infrastructure.brokers.kafka.KafkaEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

/**
 * Publicador real: manda los eventos de dominio al topic de Kafka.
 *
 * Es el bean por defecto. Solo se retira cuando {@code vankoo.events.enabled=false},
 * en cuyo caso entra {@link com.liquilabs.vankoo.iam.infrastructure.brokers.logging.services.LoggingEventService}.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "vankoo.events.enabled", havingValue = "true", matchIfMissing = true)
public class EventServiceImpl implements KafkaEventService {

    private final StreamBridge streamBridge;

    @Override
    public void publishEvent(Object event) {
        streamBridge.send("iam-out-0", event);
    }
}
