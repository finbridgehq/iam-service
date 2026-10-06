package com.liquilabs.vankoo.iam.domain.exceptions;

/**
 * Raised when a sign-up request asks for a role nobody may grant themselves — either
 * a privileged one such as ROLE_ADMIN, or a name that is not a role at all.
 */
public class RoleNotAllowedException extends DomainException {

    public RoleNotAllowedException(String roleName) {
        super("Role cannot be self-assigned on sign-up: " + roleName);
    }
}
