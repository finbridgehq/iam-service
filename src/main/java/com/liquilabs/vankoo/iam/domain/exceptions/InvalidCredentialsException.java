package com.liquilabs.vankoo.iam.domain.exceptions;

/**
 * Raised when sign-in fails, whatever the reason.
 *
 * Deliberately one single exception for both "no such account" and "wrong password".
 * Telling them apart would turn sign-in into a way of finding out which addresses are
 * registered, so the distinction is not lost in the REST layer — it is never made in
 * the first place, and cannot be reintroduced by someone adding a handler later.
 *
 * For the same reason the message carries no email address: it would put the answer
 * in the logs.
 */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Sign-in failed: unknown account or wrong password");
    }
}
