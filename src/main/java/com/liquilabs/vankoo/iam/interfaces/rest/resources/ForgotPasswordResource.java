package com.liquilabs.vankoo.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * A password recovery request.
 *
 * Unlike {@link SignInResource} the address is checked for shape here, and that is
 * not an inconsistency: what this endpoint hides is whether an account exists, which
 * never depends on syntax. A malformed address is a broken client, and saying so
 * gives nothing away.
 */
public record ForgotPasswordResource(
        @NotBlank @Email String email
) {
}
