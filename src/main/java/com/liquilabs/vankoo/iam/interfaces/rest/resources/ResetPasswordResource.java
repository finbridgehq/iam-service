package com.liquilabs.vankoo.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * A new password, together with the link it came from.
 *
 * The password is only checked for being present, exactly as in {@link SignUpResource}:
 * this service has no password policy anywhere, and adding one on this endpoint alone
 * would let someone choose a password here that signing up would have refused.
 */
public record ResetPasswordResource(
        @NotBlank String token,
        @NotBlank String password
) {
}
