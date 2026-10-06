package com.liquilabs.vankoo.iam.interfaces.rest.transform;

import com.liquilabs.vankoo.iam.domain.model.commands.SignUpCommand;
import com.liquilabs.vankoo.iam.domain.model.entities.Role;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Password;
import com.liquilabs.vankoo.iam.interfaces.rest.resources.SignUpResource;

import java.util.ArrayList;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        var roles = resource.roles() != null ? resource.roles().stream().map(Role::toSelfAssignableRoleFromName).toList() : new ArrayList<Role>();
        return new SignUpCommand(new Email(resource.email()), new Password(resource.password()), roles);
    }
}
