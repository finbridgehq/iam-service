package com.liquilabs.vankoo.iam.application.internal.outboundservices.mail;

import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;

import java.util.Date;

/**
 * Reaching a person, as opposed to reaching another service.
 *
 * Shaped as one method per message rather than a generic send(to, subject, body):
 * what a reset mail says, what it looks like and where its links point are all
 * infrastructure, and the application layer has no business assembling a URL. It
 * hands over a secret and a deadline; how that becomes an inbox is not its problem.
 *
 * This is also the seam that keeps Mailpit out of the code. Locally the adapter talks
 * to a fake relay, in production to a real one, and nothing above this line changes.
 */
public interface MailService {

    void sendPasswordResetEmail(Email recipient, String token, Date expiresAt);
}
