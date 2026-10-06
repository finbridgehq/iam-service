package com.liquilabs.vankoo.iam.infrastructure.brokers.logging.services;

import com.liquilabs.vankoo.iam.application.internal.outboundservices.events.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * PARCHE TEMPORAL — publicador que no publica.
 *
 * Existe porque en Azure (Sprint 2) todavía no hay broker: el IAM se despliega solo,
 * sin Kafka ni Event Hubs. Sin este bean, {@code UserCreatedEventHandler} intentaría
 * mandar el evento en AFTER_COMMIT y bloquearía el hilo de la petición hasta
 * {@code max.block.ms} (60 s) antes de fallar: el usuario quedaría creado en la base
 * de datos pero el cliente recibiría un 500.
 *
 * Se activa con {@code vankoo.events.enabled=false} (perfil {@code azure}). Los eventos
 * se registran en el log a nivel WARN y se descartan: Profile no recibirá los
 * {@code UserCreatedEvent} de este entorno hasta que exista un broker.
 *
 * Retirar cuando se aprovisione Event Hubs y el perfil {@code azure} vuelva a
 * {@code vankoo.events.enabled=true}.
 */
@Service
@ConditionalOnProperty(name = "vankoo.events.enabled", havingValue = "false")
public class LoggingEventService implements EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEventService.class);

    @Override
    public void publishEvent(Object event) {
        LOGGER.warn("Eventos desactivados (vankoo.events.enabled=false); se descarta {}: {}",
                event.getClass().getSimpleName(), event);
    }
}
