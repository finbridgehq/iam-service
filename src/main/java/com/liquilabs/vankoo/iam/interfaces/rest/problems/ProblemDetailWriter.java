package com.liquilabs.vankoo.iam.interfaces.rest.problems;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Writes a problem+json body straight onto the servlet response.
 *
 * The security handlers need this because they run in the filter chain, outside the
 * DispatcherServlet: there is no controller advice to catch anything and no message
 * converter to render a return value. Without it, a 401 falls back to the container's
 * error page and leaves the contract at the door.
 *
 * The body is assembled as an explicit map rather than handed to the mapper as a
 * {@link ProblemDetail}, whose serialisation depends on a Jackson mixin that is only
 * registered on the MVC path. Spelling it out keeps both paths byte-compatible.
 */
@Component
public class ProblemDetailWriter {

    private final ObjectMapper objectMapper;

    public ProblemDetailWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response, ApiProblem problem) throws IOException {
        var detail = problem.toProblemDetail(URI.create(request.getRequestURI()));
        response.setStatus(problem.getStatus().value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), asBody(detail));
        response.flushBuffer();
    }

    private Map<String, Object> asBody(ProblemDetail detail) {
        var body = new LinkedHashMap<String, Object>();
        body.put("type", detail.getType().toString());
        body.put("title", detail.getTitle());
        body.put("status", detail.getStatus());
        body.put("detail", detail.getDetail());
        body.put("instance", detail.getInstance() == null ? null : detail.getInstance().toString());
        var properties = detail.getProperties();
        if (properties != null) body.putAll(properties);
        return body;
    }
}
