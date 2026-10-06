package com.liquilabs.vankoo.iam.domain.model.valueobjects;

import java.util.Optional;
import java.util.Set;

public enum RoleName {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_MYPE,
    ROLE_INVESTOR;

    /**
     * The only roles a visitor may ask for while creating their own account.
     *
     * Sign-up is open to anyone, so whatever it accepts is effectively granted to the
     * public: without this list, a request carrying ROLE_ADMIN creates an administrator.
     * Which roles can be self-assigned is a business rule, which is why it lives here
     * and not in a filter or a controller.
     */
    private static final Set<RoleName> SELF_ASSIGNABLE = Set.of(ROLE_MYPE, ROLE_INVESTOR);

    /**
     * Resolves a role name a client sent, accepting only the self-assignable ones.
     *
     * Returns empty both for a privileged role and for a string that names no role at
     * all, so the caller cannot tell the two apart — and neither can whoever is probing.
     */
    public static Optional<RoleName> selfAssignableFrom(String name) {
        return SELF_ASSIGNABLE.stream().filter(role -> role.name().equals(name)).findFirst();
    }
}
