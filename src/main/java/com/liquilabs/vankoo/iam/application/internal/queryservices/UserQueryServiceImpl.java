package com.liquilabs.vankoo.iam.application.internal.queryservices;

import com.liquilabs.vankoo.iam.domain.model.aggregates.User;
import com.liquilabs.vankoo.iam.domain.model.queries.GetUserByEmailQuery;
import com.liquilabs.vankoo.iam.domain.services.UserQueryService;
import com.liquilabs.vankoo.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmailWithRoles(query.email());
    }
}
