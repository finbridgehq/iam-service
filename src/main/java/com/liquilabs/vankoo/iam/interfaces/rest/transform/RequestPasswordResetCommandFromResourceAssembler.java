package com.liquilabs.vankoo.iam.interfaces.rest.transform;

import com.liquilabs.vankoo.iam.domain.model.commands.RequestPasswordResetCommand;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;
import com.liquilabs.vankoo.iam.interfaces.rest.resources.ForgotPasswordResource;

public class RequestPasswordResetCommandFromResourceAssembler {
    public static RequestPasswordResetCommand toCommandFromResource(ForgotPasswordResource forgotPasswordResource) {
        return new RequestPasswordResetCommand(new Email(forgotPasswordResource.email()));
    }
}
