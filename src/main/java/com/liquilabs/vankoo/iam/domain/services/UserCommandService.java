package com.liquilabs.vankoo.iam.domain.services;

import com.liquilabs.vankoo.iam.domain.model.aggregates.User;
import com.liquilabs.vankoo.iam.domain.model.commands.RequestPasswordResetCommand;
import com.liquilabs.vankoo.iam.domain.model.commands.ResetPasswordCommand;
import com.liquilabs.vankoo.iam.domain.model.commands.SignInCommand;
import com.liquilabs.vankoo.iam.domain.model.commands.SignUpCommand;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

public interface UserCommandService {

    Optional<User> handle(SignUpCommand command);

    Optional<ImmutablePair<User, String>> handle(SignInCommand command);

    /**
     * Issues a reset link for an address, if it has an account.
     *
     * Returns nothing, and that is the whole design. Handing the caller a User — or
     * even a boolean — would give it the one fact this operation exists to withhold,
     * and sooner or later a controller would branch on it and put account enumeration
     * back into the API. There is nothing to leak if there is nothing to return.
     */
    void handle(RequestPasswordResetCommand command);

    void handle(ResetPasswordCommand command);
}
