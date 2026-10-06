package com.liquilabs.vankoo.iam.infrastructure.scheduling;

import com.liquilabs.vankoo.iam.domain.model.commands.PurgeExpiredPasswordResetTokensCommand;
import com.liquilabs.vankoo.iam.domain.services.PasswordResetTokenCommandService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara la limpieza cada hora.
 * <p>
 * Un temporizador es un adaptador de entrada como lo es un controlador, así que vive
 * en infraestructura y no decide nada: traduce «pasó una hora» a un comando.
 */
@Component
public class PasswordResetTokenCleanupScheduler {

    private final PasswordResetTokenCommandService passwordResetTokenCommandService;

    public PasswordResetTokenCleanupScheduler(PasswordResetTokenCommandService passwordResetTokenCommandService) {
        this.passwordResetTokenCommandService = passwordResetTokenCommandService;
    }

    // Con varias réplicas, cada una lo ejecuta. El DELETE es idempotente, así que no
    // rompe nada; queda dicho para que nadie se sorprenda de verlo tres veces en el log.
    @Scheduled(cron = "${authorization.password-reset.cleanup.cron}")
    public void purgeExpiredTokens() {
        passwordResetTokenCommandService.handle(new PurgeExpiredPasswordResetTokensCommand());
    }
}
