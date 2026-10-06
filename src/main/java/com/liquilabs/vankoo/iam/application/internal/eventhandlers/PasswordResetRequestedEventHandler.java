package com.liquilabs.vankoo.iam.application.internal.eventhandlers;

import com.liquilabs.vankoo.iam.application.internal.outboundservices.mail.MailService;
import com.liquilabs.vankoo.iam.domain.model.events.PasswordResetRequestedEvent;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class PasswordResetRequestedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetRequestedEventHandler.class);

    private final MailService mailService;

    public PasswordResetRequestedEventHandler(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * AFTER_COMMIT para no mandar un enlace de un token que la base de datos acabó
     * rechazando, y @Async porque sin él esto corre en el hilo de la petición.
     * <p>
     * Lo segundo no es una optimización: una conexión SMTP tarda cientos de
     * milisegundos, así que el endpoint tardaría notablemente más cuando la cuenta
     * existe que cuando no. Toda la molestia de responder lo mismo en los dos casos se
     * vendría abajo midiendo el reloj.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PasswordResetRequestedEvent event) {
        try {
            mailService.sendPasswordResetEmail(new Email(event.email()), event.token(), event.expiresAt());
        } catch (Exception exception) {
            // Quien lo pidió se queda esperando un correo que no va a llegar y volverá
            // a intentarlo. Esta línea es el único rastro de por qué.
            LOGGER.error("Could not send the password reset email", exception);
        }
    }
}
