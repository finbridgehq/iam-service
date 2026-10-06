package com.liquilabs.vankoo.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(
        String id,
        String email,
        String token
) {
}
