package com.liquilabs.vankoo.iam.domain.exceptions;

import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;

/** Raised when sign-up is attempted with an address that already has an account. */
public class EmailAlreadyInUseException extends DomainException {

    public EmailAlreadyInUseException(Email email) {
        super("Email already in use: " + email.email());
    }
}
