package com.liquilabs.vankoo.iam.domain.model.queries;

import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;

public record GetUserByEmailQuery(
        Email email
) {
}
