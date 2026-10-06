package com.liquilabs.vankoo.iam.domain.model.commands;

import com.liquilabs.vankoo.iam.domain.model.valueobjects.Password;

/**
 * El token viaja como String y no como value object a propósito: hasta que no se
 * digiere y se encuentra en la base de datos no es nada, y darle un tipo del dominio
 * sugeriría que ya significa algo.
 */
public record ResetPasswordCommand(String token, Password password) {}
