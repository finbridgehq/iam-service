package com.liquilabs.vankoo.iam.interfaces.rest.controllers;

import com.liquilabs.vankoo.iam.domain.services.UserCommandService;
import com.liquilabs.vankoo.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.liquilabs.vankoo.iam.interfaces.rest.resources.ForgotPasswordResource;
import com.liquilabs.vankoo.iam.interfaces.rest.resources.ResetPasswordResource;
import com.liquilabs.vankoo.iam.interfaces.rest.resources.SignInResource;
import com.liquilabs.vankoo.iam.interfaces.rest.resources.SignUpResource;
import com.liquilabs.vankoo.iam.interfaces.rest.resources.UserResource;
import com.liquilabs.vankoo.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.liquilabs.vankoo.iam.interfaces.rest.transform.RequestPasswordResetCommandFromResourceAssembler;
import com.liquilabs.vankoo.iam.interfaces.rest.transform.ResetPasswordCommandFromResourceAssembler;
import com.liquilabs.vankoo.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.liquilabs.vankoo.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.liquilabs.vankoo.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Available Authentication Endpoints")
public class AuthenticationController {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Sign-up", description = "Create a new user account with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid body, or a role that cannot be self-assigned",
                    content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "The email address already has an account",
                    content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
    })
    public ResponseEntity<UserResource> signUp(@Valid @RequestBody SignUpResource resource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        // The command service either returns the new user or throws; there is no empty
        // Optional to guard against, and pretending otherwise used to leave a dead
        // branch documenting a 400 that could never happen.
        var user = userCommandService.handle(signUpCommand).orElseThrow();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user);
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Sign-in", description = "Authenticate a user with the provided credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid body",
                    content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401",
                    description = "Authentication failed. Returned identically whether the account does not exist "
                            + "or the password is wrong, so that the endpoint cannot be used to discover accounts",
                    content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
    })
    public ResponseEntity<AuthenticatedUserResource> signIn(@Valid @RequestBody SignInResource resource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var authenticatedUser = userCommandService.handle(signInCommand).orElseThrow();
        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler
                .toResourceFromEntity(authenticatedUser.getLeft(), authenticatedUser.getRight());
        return ResponseEntity.ok(authenticatedUserResource);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password",
            description = "Request a password reset link. Answers 202 with an empty body whether or not the "
                    + "address has an account, so that the endpoint cannot be used to discover accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202",
                    description = "Request accepted. If the address has an account a reset link is on its way, "
                            + "and the answer is identical when it does not"),
            @ApiResponse(responseCode = "400", description = "Invalid body",
                    content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
    })
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordResource resource) {
        var command = RequestPasswordResetCommandFromResourceAssembler.toCommandFromResource(resource);
        // The service returns nothing, so there is nothing here to branch on. That is
        // the point: 202 is the only answer this method can give.
        userCommandService.handle(command);
        // Accepted rather than 200 or 204 because it is true: the mail leaves after the
        // transaction commits, on another thread, so the outcome is not known yet.
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Set a new password using a token received by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid body, or a token that is unknown, expired or already used. "
                            + "The three token failures are reported identically",
                    content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
    })
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordResource resource) {
        var command = ResetPasswordCommandFromResourceAssembler.toCommandFromResource(resource);
        userCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}
