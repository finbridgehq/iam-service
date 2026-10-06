package com.liquilabs.vankoo.iam.interfaces.rest.problems;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

/**
 * The catalogue of failures this service can report, in the shape RFC 9457 asks for.
 *
 * One entry per case, holding the status, the stable code and the English prose, so
 * the three can never drift apart across handlers. Everything a client decides on is
 * the {@code code}: it is the contract, and changing one breaks whoever reads it.
 *
 * {@code title} and {@code detail} are for people reading logs, Scalar or a curl.
 * They stay in English and are never rendered to an end user — each frontend keeps
 * its own copy, in its own language, keyed by the code. That is also why no detail
 * here ever carries a stack trace, an SQL fragment or a class name: this text reaches
 * the browser.
 */
public enum ApiProblem {

    EMAIL_ALREADY_IN_USE(HttpStatus.CONFLICT, "email-already-in-use", "Email already in use",
            "An account with this email address already exists."),

    // One entry for both sign-in failures. Splitting it would let anyone find out
    // which addresses are registered.
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "invalid-credentials", "Invalid credentials",
            "The email address or the password is incorrect."),

    // One entry for all three ways a reset link fails: unknown, expired, already used.
    // Splitting them would rebuild on the wire the distinction the domain refuses to
    // make, and no client would render them differently — all three mean "ask for
    // another link".
    INVALID_PASSWORD_RESET_TOKEN(HttpStatus.BAD_REQUEST, "invalid-password-reset-token",
            "Invalid password reset token",
            "The password reset link is invalid, has expired or has already been used."),

    ROLE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "role-not-allowed", "Role not allowed",
            "The requested role cannot be assigned while signing up."),

    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "validation-failed", "Validation failed",
            "The request body did not pass validation."),

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "invalid-request", "Invalid request",
            "The request contains invalid data."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "user-not-found", "User not found",
            "No user matches the given email address."),

    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "unauthenticated", "Unauthenticated",
            "This endpoint requires a valid bearer token."),

    FORBIDDEN(HttpStatus.FORBIDDEN, "forbidden", "Forbidden",
            "This account is not allowed to perform this action."),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Internal error",
            "Something went wrong on our side.");

    /**
     * Prefix of the {@code type} URI. It identifies the kind of problem and does not
     * have to resolve today; keeping it in one constant is what lets the whole
     * catalogue move if the documentation ever changes address.
     */
    public static final String TYPE_BASE = "https://docs.vankoo.dev/errors/";

    /** Name of the extension member clients switch on. */
    public static final String CODE_PROPERTY = "code";

    private final HttpStatus status;
    private final String code;
    private final String title;
    private final String detail;

    ApiProblem(HttpStatus status, String code, String title, String detail) {
        this.status = status;
        this.code = code;
        this.title = title;
        this.detail = detail;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    /** Builds the body for this problem, with {@code instance} left to the caller. */
    public ProblemDetail toProblemDetail(URI instance) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(TYPE_BASE + code));
        problem.setTitle(title);
        problem.setInstance(instance);
        problem.setProperty(CODE_PROPERTY, code);
        return problem;
    }
}
