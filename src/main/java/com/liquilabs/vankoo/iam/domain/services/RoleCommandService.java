package com.liquilabs.vankoo.iam.domain.services;

import com.liquilabs.vankoo.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
    void handle(SeedRolesCommand command);
}
