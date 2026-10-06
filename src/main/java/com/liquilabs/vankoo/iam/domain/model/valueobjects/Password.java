package com.liquilabs.vankoo.iam.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Password(
        String password
) {
    public Password() {
        this("");
    }

    public Password {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
    }
}
