package com.liquilabs.vankoo.iam.domain.exceptions;

/**
 * Base for the failures the domain itself recognises as failures.
 *
 * The message is written for whoever is reading a log or a stack trace, in English
 * and never shown to an end user. The HTTP status and the machine-readable code that
 * clients switch on are decided in the REST layer, not here: the domain has no
 * opinion about status codes, and keeping the mapping in one place is what stops the
 * two from drifting apart.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
