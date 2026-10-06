package com.liquilabs.vankoo.iam.domain.services;

import com.liquilabs.vankoo.iam.domain.model.commands.PurgeExpiredPasswordResetTokensCommand;

/**
 * Kept off UserCommandService on purpose: sweeping dead rows is maintenance on the
 * token aggregate, not something anyone does to a user.
 */
public interface PasswordResetTokenCommandService {

    void handle(PurgeExpiredPasswordResetTokensCommand command);
}
