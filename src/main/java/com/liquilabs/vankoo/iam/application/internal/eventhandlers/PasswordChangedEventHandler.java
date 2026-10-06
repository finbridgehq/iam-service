package com.liquilabs.vankoo.iam.application.internal.eventhandlers;

import com.liquilabs.vankoo.iam.application.internal.outboundservices.events.EventService;
import com.liquilabs.vankoo.iam.domain.model.events.PasswordChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class PasswordChangedEventHandler {

    private final EventService eventService;
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordChangedEventHandler.class);

    public PasswordChangedEventHandler(EventService eventService) {
        this.eventService = eventService;
    }

    // Este sí sale a Kafka: no lleva secretos, solo quién cambió su contraseña.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PasswordChangedEvent event) {
        LOGGER.info("Handling PasswordChangedEvent for user email: {}", event.email());
        eventService.publishEvent(event);
    }
}
