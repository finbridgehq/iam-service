package com.liquilabs.vankoo.iam.domain.model.commands;

import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;

public record RequestPasswordResetCommand(Email email) {}
