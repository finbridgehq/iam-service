package com.liquilabs.vankoo.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * A sign-up request.
 *
 * The constraints are the first line of defence, not the only one: the value objects
 * check the same things again when the command is built. Doing it here as well is what
 * turns a malformed body into a 400 that names the offending field, instead of an
 * exception raised deep in the domain with nothing to attach it to.
 *
 * {@code roles} stays optional and unconstrained here on purpose — which roles a
 * visitor may grant themselves is a business rule, and it is enforced in the domain.
 */
public record SignUpResource(
        @NotBlank @Email String email,
        @NotBlank String password,
        List<String> roles
) {
}
