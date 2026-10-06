package com.liquilabs.vankoo.iam.infrastructure.mail.smtp;

import com.liquilabs.vankoo.iam.application.internal.outboundservices.mail.MailService;

/**
 * Marker for the SMTP implementation, in the same shape as KafkaEventService and
 * BearerTokenService: the port names the capability, this names the technology.
 */
public interface SmtpMailService extends MailService {
}
