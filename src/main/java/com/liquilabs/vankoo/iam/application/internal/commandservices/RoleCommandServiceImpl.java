package com.liquilabs.vankoo.iam.application.internal.commandservices;

import com.liquilabs.vankoo.iam.domain.model.commands.SeedRolesCommand;
import com.liquilabs.vankoo.iam.domain.model.entities.Role;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.RoleName;
import com.liquilabs.vankoo.iam.domain.services.RoleCommandService;
import com.liquilabs.vankoo.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class RoleCommandServiceImpl implements RoleCommandService {

    private final RoleRepository roleRepository;

    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
     }

    @Override
    public void handle(SeedRolesCommand command) {
        Arrays.stream(RoleName.values()).forEach(role -> {
            if (!roleRepository.existsByName(role)) {
                roleRepository.save(new Role(RoleName.valueOf(role.name())));
            }
        });
    }
}
