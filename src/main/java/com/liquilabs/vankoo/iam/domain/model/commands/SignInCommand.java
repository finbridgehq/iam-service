package com.liquilabs.vankoo.iam.domain.model.commands;

import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Password;

public record SignInCommand(
        Email email,
        Password password
) {
}
