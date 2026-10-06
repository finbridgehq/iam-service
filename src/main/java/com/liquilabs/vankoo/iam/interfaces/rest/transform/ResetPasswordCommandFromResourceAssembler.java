package com.liquilabs.vankoo.iam.interfaces.rest.transform;

import com.liquilabs.vankoo.iam.domain.model.commands.ResetPasswordCommand;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Password;
import com.liquilabs.vankoo.iam.interfaces.rest.resources.ResetPasswordResource;

public class ResetPasswordCommandFromResourceAssembler {
    public static ResetPasswordCommand toCommandFromResource(ResetPasswordResource resetPasswordResource) {
        return new ResetPasswordCommand(resetPasswordResource.token(), new Password(resetPasswordResource.password()));
    }
}
