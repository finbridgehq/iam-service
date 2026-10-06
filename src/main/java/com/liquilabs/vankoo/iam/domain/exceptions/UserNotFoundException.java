package com.liquilabs.vankoo.iam.domain.exceptions;

import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;

/**
 * Raised when a lookup by email finds nothing.
 *
 * Only for the users endpoint, which is behind a token. Sign-in never throws this —
 * see {@link InvalidCredentialsException}.
 */
public class UserNotFoundException extends DomainException {

    public UserNotFoundException(Email email) {
        super("No user found for email: " + email.email());
    }
}
