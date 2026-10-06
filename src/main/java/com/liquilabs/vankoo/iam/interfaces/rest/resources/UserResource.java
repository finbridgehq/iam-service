package com.liquilabs.vankoo.iam.interfaces.rest.resources;

import java.util.List;

public record UserResource(
        String id,
        String email,
        List<String> roles
) {
}
