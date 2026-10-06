package com.liquilabs.vankoo.iam.application.internal.commandservices;

import com.liquilabs.vankoo.iam.domain.model.commands.PurgeExpiredPasswordResetTokensCommand;
import com.liquilabs.vankoo.iam.domain.services.PasswordResetTokenCommandService;
import com.liquilabs.vankoo.iam.infrastructure.persistence.jpa.repositories.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PasswordResetTokenCommandServiceImpl implements PasswordResetTokenCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetTokenCommandServiceImpl.class);

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetTokenCommandServiceImpl(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    /**
     * Higiene, no seguridad: un token caducado o consumido ya no sirve para nada, y
     * pedir uno nuevo borra el anterior. Esto solo evita que la tabla acumule una fila
     * por cada recuperación que alguien pidió alguna vez.
     */
    @Override
    @Transactional
    public void handle(PurgeExpiredPasswordResetTokensCommand command) {
        var deleted = passwordResetTokenRepository.deleteExpiredOrConsumed(new Date());
        if (deleted > 0) LOGGER.debug("Purged {} password reset tokens", deleted);
    }
}
