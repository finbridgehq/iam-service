package com.liquilabs.vankoo.iam.domain.services;

import com.liquilabs.vankoo.iam.domain.model.aggregates.User;
import com.liquilabs.vankoo.iam.domain.model.queries.GetUserByEmailQuery;

import java.util.Optional;

public interface UserQueryService {
    Optional<User> handle(GetUserByEmailQuery query);
}
