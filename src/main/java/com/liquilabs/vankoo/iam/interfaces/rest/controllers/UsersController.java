package com.liquilabs.vankoo.iam.interfaces.rest.controllers;

import com.liquilabs.vankoo.iam.domain.exceptions.UserNotFoundException;
import com.liquilabs.vankoo.iam.domain.model.queries.GetUserByEmailQuery;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;
import com.liquilabs.vankoo.iam.domain.services.UserQueryService;
import com.liquilabs.vankoo.iam.interfaces.rest.resources.UserResource;
import com.liquilabs.vankoo.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Available User Endpoints")
public class UsersController {

    private final UserQueryService userQueryService;

    public UsersController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping(value = "/{email:.+}")
    @Operation(summary = "Get user by email", description = "Get the user with the specified email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No user with that email address",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserResource> getUserByEmail(@PathVariable String email) {
        var address = new Email(email);
        // Throwing rather than returning an empty 404 is what gets this into the agreed
        // format: a body-less response tells a client the status and nothing else.
        var user = userQueryService.handle(new GetUserByEmailQuery(address))
                .orElseThrow(() -> new UserNotFoundException(address));
        return ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(user));
    }
}
