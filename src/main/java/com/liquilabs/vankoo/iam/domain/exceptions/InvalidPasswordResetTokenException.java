package com.liquilabs.vankoo.iam.domain.exceptions;

/**
 * Raised when a reset link cannot be honoured, whatever the reason.
 *
 * One exception for "no such token", "expired" and "already used", on the same
 * grounds as {@link InvalidCredentialsException}: telling them apart would turn the
 * endpoint into a way of finding out which links have existed, and no client would
 * act differently anyway — all three mean "ask for a new one".
 *
 * The token is not in the message. It is a bearer secret, and a log line is
 * somewhere other people can read.
 */
public class InvalidPasswordResetTokenException extends DomainException {

    public InvalidPasswordResetTokenException() {
        super("Password reset failed: unknown, expired or already used token");
    }
}
