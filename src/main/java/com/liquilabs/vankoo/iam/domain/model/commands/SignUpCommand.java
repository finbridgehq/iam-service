package com.liquilabs.vankoo.iam.domain.model.commands;

import com.liquilabs.vankoo.iam.domain.model.entities.Role;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Password;

import java.util.List;

public record SignUpCommand(
        Email email,
        Password password,
        List<Role> roles
) {
}
