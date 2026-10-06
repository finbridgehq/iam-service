package com.liquilabs.vankoo.iam.interfaces.rest.transform;

import com.liquilabs.vankoo.iam.domain.model.aggregates.User;
import com.liquilabs.vankoo.iam.domain.model.entities.Role;
import com.liquilabs.vankoo.iam.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User entity) {
        var roles = entity.getRoles().stream().map(Role::getStringName).toList();
        var userId = entity.getId() != null ? entity.getId().id().toString() : null;
        return new UserResource(userId, entity.getEmail().email(), roles);
    }
}
