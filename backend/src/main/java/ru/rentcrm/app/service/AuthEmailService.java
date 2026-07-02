package ru.rentcrm.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AuthEmailService {
    private static final Logger log = LoggerFactory.getLogger(AuthEmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean devLinksEnabled;
    private final String from;
    private final String mailHost;

    public AuthEmailService(
        ObjectProvider<JavaMailSender> mailSenderProvider,
        @Value("${app.auth.dev-links-enabled:false}") boolean devLinksEnabled,
        @Value("${app.mail.from:noreply@rentcrm.local}") String from,
        @Value("${spring.mail.host:}") String mailHost
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.devLinksEnabled = devLinksEnabled;
        this.from = from;
        this.mailHost = mailHost;
    }

    public boolean devLinksEnabled() {
        return devLinksEnabled;
    }

    public void sendVerification(String email, String link) {
        send(email, "Подтвердите email в RentCRM", """
            Здравствуйте!

            Для подтверждения email в RentCRM перейдите по ссылке:
            %s

            Если вы не создавали кабинет RentCRM, просто проигнорируйте это письмо.
            """.formatted(link));
    }

    public void sendPasswordReset(String email, String link) {
        send(email, "Сброс пароля RentCRM", """
            Здравствуйте!

            Для смены пароля в RentCRM перейдите по ссылке:
            %s

            Ссылка действует ограниченное время. Если вы не запрашивали сброс пароля, просто проигнорируйте это письмо.
            """.formatted(link));
    }

    private void send(String to, String subject, String text) {
        if (mailHost == null || mailHost.isBlank()) {
            log.warn("SMTP host is not configured; auth email was not sent to {}. Body:\n{}", to, text);
            return;
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("JavaMailSender is not available; auth email was not sent to {}", to);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send auth email to {}", to, ex);
        }
    }
}
