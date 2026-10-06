package com.liquilabs.vankoo.iam.interfaces.rest.advice;

import com.liquilabs.vankoo.iam.domain.exceptions.EmailAlreadyInUseException;
import com.liquilabs.vankoo.iam.domain.exceptions.InvalidCredentialsException;
import com.liquilabs.vankoo.iam.domain.exceptions.InvalidPasswordResetTokenException;
import com.liquilabs.vankoo.iam.domain.exceptions.RoleNotAllowedException;
import com.liquilabs.vankoo.iam.domain.exceptions.UserNotFoundException;
import com.liquilabs.vankoo.iam.interfaces.rest.problems.ApiProblem;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Turns every failure that reaches the REST layer into an RFC 9457 problem+json body.
 *
 * Extending {@link ResponseEntityExceptionHandler} rather than writing a bare advice
 * buys the framework's own failures — an unreadable body, a wrong method, an
 * unsupported media type — in the same format, and replaces Boot's built-in
 * ProblemDetailsExceptionHandler, which is registered only when no bean of this type
 * exists. That is also why {@code spring.mvc.problemdetails.enabled} is left alone:
 * with this class present the property has nothing to switch on.
 *
 * Two rules hold everywhere below. Sign-in failures answer identically whatever went
 * wrong, so the endpoint cannot be used to find out who has an account. And no detail
 * ever carries an exception message, a class name or a query — those go to the log,
 * because the body goes to a browser.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ProblemDetail> handleEmailAlreadyInUse(EmailAlreadyInUseException exception,
                                                                 HttpServletRequest request) {
        return respond(ApiProblem.EMAIL_ALREADY_IN_USE, request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException exception,
                                                                  HttpServletRequest request) {
        return respond(ApiProblem.INVALID_CREDENTIALS, request);
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidPasswordResetToken(InvalidPasswordResetTokenException exception,
                                                                         HttpServletRequest request) {
        // The token stays out of the log line: it is a bearer secret for as long as it
        // is alive, and a rejected one may simply have been mistyped.
        LOGGER.warn("Rejected a password reset at {}", request.getRequestURI());
        return respond(ApiProblem.INVALID_PASSWORD_RESET_TOKEN, request);
    }

    @ExceptionHandler(RoleNotAllowedException.class)
    public ResponseEntity<ProblemDetail> handleRoleNotAllowed(RoleNotAllowedException exception,
                                                              HttpServletRequest request) {
        // Worth a warning rather than a debug line: a request asking for a role it may
        // not have is either a broken client or somebody trying their luck.
        LOGGER.warn("Rejected sign-up role: {}", exception.getMessage());
        return respond(ApiProblem.ROLE_NOT_ALLOWED, request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFound(UserNotFoundException exception,
                                                            HttpServletRequest request) {
        return respond(ApiProblem.USER_NOT_FOUND, request);
    }

    /**
     * Catches what the value objects still throw.
     *
     * Bean Validation now rejects a malformed body at the edge, so reaching here means
     * something got past it — a path variable, say. The message is not repeated in the
     * response: it was written for a developer and may name a field or a constraint.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException exception,
                                                               HttpServletRequest request) {
        LOGGER.warn("Rejected request to {}: {}", request.getRequestURI(), exception.getMessage());
        return respond(ApiProblem.INVALID_REQUEST, request);
    }

    /**
     * Hands access denials back to Spring Security instead of answering them here.
     *
     * The catch-all below would otherwise turn a 403 into a 500. Rethrowing lets the
     * ExceptionTranslationFilter make the call it is there to make — 403 for someone
     * signed in without the role, 401 for someone not signed in at all — and the
     * handlers registered on the filter chain write the body.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDenied(AccessDeniedException exception) {
        throw exception;
    }

    /** Last resort, so that nothing at all escapes as the container's error page. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error("Unhandled failure at {}", request.getRequestURI(), exception);
        return respond(ApiProblem.INTERNAL_ERROR, request);
    }

    /**
     * Reports which fields failed and why, as machine-readable pairs.
     *
     * The frontend needs to know the field to place the message under the right input,
     * and the constraint to word it. It cannot use {@code message} for that: like every
     * other string here it is English, and the copy belongs to whoever drew the screen.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        var problem = ApiProblem.VALIDATION_FAILED.toProblemDetail(instanceOf(request));
        problem.setProperty("errors", violationsOf(exception));
        return handleExceptionInternal(exception, problem, headers, ApiProblem.VALIDATION_FAILED.getStatus(), request);
    }

    /**
     * Makes sure the framework's own problems carry a code too.
     *
     * The parent class formats a wrong method or an unreadable body as a
     * {@link ProblemDetail} with no {@code code}, and a client left to branch on the
     * status alone is back where it started. The code is derived from the status, so a
     * case nobody anticipated still answers in the agreed shape.
     *
     * This is the hook rather than {@code handleExceptionInternal} because the parent
     * often reaches that one with a null body and fills it in afterwards; by here the
     * body is final whichever path produced it.
     */
    @Override
    protected ResponseEntity<Object> createResponseEntity(@Nullable Object body,
                                                          HttpHeaders headers,
                                                          HttpStatusCode statusCode,
                                                          WebRequest request) {
        if (body instanceof ProblemDetail problem && !hasCode(problem)) {
            var code = codeFor(statusCode);
            problem.setType(URI.create(ApiProblem.TYPE_BASE + code));
            problem.setProperty(ApiProblem.CODE_PROPERTY, code);
            if (problem.getInstance() == null) problem.setInstance(instanceOf(request));
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    private ResponseEntity<ProblemDetail> respond(ApiProblem problem, HttpServletRequest request) {
        return ResponseEntity.status(problem.getStatus())
                .body(problem.toProblemDetail(URI.create(request.getRequestURI())));
    }

    private static List<Map<String, String>> violationsOf(MethodArgumentNotValidException exception) {
        var violations = new ArrayList<Map<String, String>>();
        // A field can break two rules at once — blank and malformed — so this is a list
        // and not a map keyed by field name.
        for (var error : exception.getBindingResult().getFieldErrors()) violations.add(violationOf(error));
        for (var error : exception.getBindingResult().getGlobalErrors()) {
            violations.add(violation(error.getObjectName(), constraintCode(error.getCode()), error.getDefaultMessage()));
        }
        return violations;
    }

    private static Map<String, String> violationOf(FieldError error) {
        return violation(error.getField(), constraintCode(error.getCode()), error.getDefaultMessage());
    }

    private static Map<String, String> violation(String field, String code, @Nullable String message) {
        var violation = new LinkedHashMap<String, String>();
        violation.put("field", field);
        violation.put("code", code);
        if (message != null) violation.put("message", message);
        return violation;
    }

    /**
     * Names the broken rule, not the annotation that expressed it.
     *
     * A client should not have to know that "blank" is spelled NotBlank in Java, and
     * swapping a constraint for an equivalent one should not break it.
     */
    private static String constraintCode(@Nullable String annotation) {
        if (annotation == null) return "invalid";
        return switch (annotation) {
            case "NotBlank", "NotNull", "NotEmpty" -> "required";
            case "Email", "Pattern" -> "invalid-format";
            case "Size", "Length" -> "invalid-length";
            case "Min", "Max", "Positive", "PositiveOrZero", "Negative", "NegativeOrZero" -> "out-of-range";
            default -> annotation.toLowerCase(Locale.ROOT);
        };
    }

    private static String codeFor(HttpStatusCode statusCode) {
        var status = HttpStatus.resolve(statusCode.value());
        return status == null ? "error" : status.getReasonPhrase().toLowerCase(Locale.ROOT).replace(' ', '-');
    }

    private static boolean hasCode(ProblemDetail problem) {
        var properties = problem.getProperties();
        return properties != null && properties.containsKey(ApiProblem.CODE_PROPERTY);
    }

    private static URI instanceOf(WebRequest request) {
        return URI.create(request.getDescription(false).replaceFirst("^uri=", ""));
    }
}
