package com.liquilabs.vankoo.iam.infrastructure.authorization.sfs.pipeline;

import com.liquilabs.vankoo.iam.interfaces.rest.problems.ApiProblem;
import com.liquilabs.vankoo.iam.interfaces.rest.problems.ProblemDetailWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Answers requests that arrive without a usable token.
 *
 * This runs in the filter chain, so the controller advice never sees it: the body has
 * to be written here or the container serves its own error page and the response falls
 * out of the agreed format. The reason for the failure is logged and deliberately not
 * sent — whether the token was missing, expired or forged is nobody's business but ours.
 */
@Component
public class UnauthorizedRequestHandlerEntryPoint implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnauthorizedRequestHandlerEntryPoint.class);

    private final ProblemDetailWriter problemDetailWriter;

    public UnauthorizedRequestHandlerEntryPoint(ProblemDetailWriter problemDetailWriter) {
        this.problemDetailWriter = problemDetailWriter;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException {
        // A warning, not an error: an unauthenticated request is ordinary traffic, and
        // logging it at error level buries the failures that do need looking at.
        LOGGER.warn("Unauthenticated request to {}: {}", request.getRequestURI(), authenticationException.getMessage());
        // Without this header the 401 does not say what kind of credentials it wants,
        // which RFC 9110 requires and every HTTP client expects.
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        problemDetailWriter.write(request, response, ApiProblem.UNAUTHENTICATED);
    }
}
