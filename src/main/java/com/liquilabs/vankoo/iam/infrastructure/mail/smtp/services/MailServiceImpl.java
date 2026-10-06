package com.liquilabs.vankoo.iam.infrastructure.mail.smtp.services;

import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;
import com.liquilabs.vankoo.iam.infrastructure.mail.smtp.SmtpMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Envía el correo de recuperación por SMTP.
 * <p>
 * El mensaje lleva dos enlaces con el mismo token. El botón principal abre la web,
 * que funciona en cualquier sitio; debajo va el esquema de la app, para quien lea el
 * correo en el móvil. El día que haya un dominio propio con App Links, el segundo
 * sobra y no cambia nada más: el backend seguirá emitiendo un solo token.
 */
@Service
public class MailServiceImpl implements SmtpMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailServiceImpl.class);

    private static final String TEMPLATE = "mail/password-reset";
    private static final String SUBJECT = "Recupera tu contraseña de Vankoo";
    private static final String TOKEN_PARAMETER = "token";

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;
    private final String from;
    private final String webUrl;
    private final String mobileUrl;

    public MailServiceImpl(
            JavaMailSender mailSender,
            ITemplateEngine templateEngine,
            @Value("${mail.from}") String from,
            @Value("${mail.password-reset.web-url}") String webUrl,
            @Value("${mail.password-reset.mobile-url}") String mobileUrl
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.from = from;
        this.webUrl = webUrl;
        this.mobileUrl = mobileUrl;
    }

    @Override
    public void sendPasswordResetEmail(Email recipient, String token, Date expiresAt) {
        var context = new Context();
        context.setVariable("webLink", linkOf(webUrl, token));
        context.setVariable("mobileLink", linkOf(mobileUrl, token));
        context.setVariable("expirationMinutes", minutesUntil(expiresAt));

        // Un MimeMessagePreparator y no un MimeMessage montado a mano: su lambda puede
        // lanzar excepciones comprobadas, así que arma el mensaje sin un try/catch que
        // no sabría qué hacer con el fallo.
        MimeMessagePreparator preparator = mimeMessage -> {
            var helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(recipient.email());
            helper.setSubject(SUBJECT);
            helper.setText(templateEngine.process(TEMPLATE, context), true);
        };
        mailSender.send(preparator);
        // Sin el token y sin la dirección completa: esto es un log, y el correo ya
        // llegó a quien tenía que llegar.
        LOGGER.info("Password reset email sent");
    }

    /**
     * Un solo sitio donde se pega el token a una URL, para que la codificación del
     * parámetro esté escrita una vez. Vale igual para https y para vankoo://.
     */
    private String linkOf(String base, String token) {
        return UriComponentsBuilder.fromUriString(base)
                .queryParam(TOKEN_PARAMETER, token)
                .build()
                .toUriString();
    }

    private long minutesUntil(Date expiresAt) {
        var remaining = expiresAt.getTime() - System.currentTimeMillis();
        return Math.max(1, TimeUnit.MILLISECONDS.toMinutes(remaining));
    }
}
