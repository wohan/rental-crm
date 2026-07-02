package ru.rentcrm.app.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.ErrorEvent;
import ru.rentcrm.app.repository.ErrorEventRepository;
import ru.rentcrm.app.repository.UserRepository;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Service
public class ErrorTrackingService {
    private static final Logger log = LoggerFactory.getLogger(ErrorTrackingService.class);

    private final ErrorEventRepository errors;
    private final UserRepository users;
    private final JavaMailSender mailSender;
    private final String alertEmail;
    private final String fromEmail;

    public ErrorTrackingService(
        ErrorEventRepository errors,
        UserRepository users,
        JavaMailSender mailSender,
        @Value("${app.errors.alert-email:}") String alertEmail,
        @Value("${app.mail.from:noreply@rentcrm.local}") String fromEmail
    ) {
        this.errors = errors;
        this.users = users;
        this.mailSender = mailSender;
        this.alertEmail = alertEmail;
        this.fromEmail = fromEmail;
    }

    public ErrorEvent capture(Throwable throwable, HttpServletRequest request) {
        log.debug("Capturing runtime error started path={}", request.getRequestURI());
        ErrorEvent event = new ErrorEvent();
        event.method = request.getMethod();
        event.path = request.getRequestURI();
        event.exceptionClass = throwable.getClass().getName();
        event.message = throwable.getMessage();
        event.stackTrace = stackTrace(throwable);
        currentUser().ifPresent(user -> {
            event.accountId = user.accountId;
            event.actorEmail = user.email;
        });
        ErrorEvent saved = errors.save(event);
        notify(saved);
        log.debug("Capturing runtime error finished errorId={}", saved.id);
        return saved;
    }

    public List<ErrorEvent> latest() {
        return errors.findTop100ByOrderByCreatedAtDesc();
    }

    private java.util.Optional<AppUser> currentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return java.util.Optional.empty();
        }
        String email = String.valueOf(authentication.getPrincipal());
        return users.findByEmail(email);
    }

    private void notify(ErrorEvent event) {
        if (alertEmail == null || alertEmail.isBlank()) {
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(alertEmail);
        message.setSubject("RentCRM backend error: " + event.exceptionClass);
        message.setText("""
            Error ID: %s
            Path: %s %s
            Actor: %s
            Message: %s

            %s
            """.formatted(event.id, event.method, event.path, event.actorEmail, event.message, event.stackTrace));
        try {
            mailSender.send(message);
            event.notified = true;
            errors.save(event);
        } catch (MailException ex) {
            log.warn("Failed to send runtime error alert errorId={}", event.id, ex);
        }
    }

    private String stackTrace(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
