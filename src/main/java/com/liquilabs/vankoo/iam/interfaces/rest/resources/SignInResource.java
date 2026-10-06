package com.liquilabs.vankoo.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * A sign-in request.
 *
 * The address is only checked for being present, not for being well formed: rejecting
 * it on format would answer differently depending on what was typed, and every
 * sign-in failure has to look the same.
 */
public record SignInResource(
        @NotBlank String email,
        @NotBlank String password
) {
}
