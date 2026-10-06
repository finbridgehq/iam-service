package com.liquilabs.vankoo.iam.infrastructure.authorization.sfs.pipeline;

import com.liquilabs.vankoo.iam.interfaces.rest.problems.ApiProblem;
import com.liquilabs.vankoo.iam.interfaces.rest.problems.ProblemDetailWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Answers requests from someone signed in who is not allowed to do what they asked.
 *
 * There was no handler registered for this at all, so every 403 raised by method
 * security escaped as the container's error page while the 401s next to it were
 * formatted. This closes that gap.
 */
@Component
public class ForbiddenRequestHandler implements AccessDeniedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForbiddenRequestHandler.class);

    private final ProblemDetailWriter problemDetailWriter;

    public ForbiddenRequestHandler(ProblemDetailWriter problemDetailWriter) {
        this.problemDetailWriter = problemDetailWriter;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        LOGGER.warn("Access denied to {}: {}", request.getRequestURI(), accessDeniedException.getMessage());
        problemDetailWriter.write(request, response, ApiProblem.FORBIDDEN);
    }
}
