package com.liquilabs.vankoo.iam.interfaces.rest.transform;

import com.liquilabs.vankoo.iam.domain.model.aggregates.User;
import com.liquilabs.vankoo.iam.interfaces.rest.resources.AuthenticatedUserResource;

import java.util.Objects;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        // The null guard that used to be here was computed and then ignored on the next
        // line, so a user without an id still threw a bare NullPointerException on the
        // sign-in path. It cannot happen — the aggregate assigns its id on construction —
        // so this states the invariant instead of pretending to recover from it.
        var userId = Objects.requireNonNull(user.getId(), "A persisted user always has an id").id();
        return new AuthenticatedUserResource(userId.toString(), user.getEmail().email(), token);
    }
}
