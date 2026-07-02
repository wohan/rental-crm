package ru.rentcrm.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.dto.ChangePasswordRequest;
import ru.rentcrm.app.exception.ApiException;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.repository.UserRepository;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.AuditService;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountSecurityController {
    private final CurrentUser current;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuditService auditService;

    public AccountSecurityController(CurrentUser current, UserRepository users, PasswordEncoder encoder, AuditService auditService) {
        this.current = current;
        this.users = users;
        this.encoder = encoder;
        this.auditService = auditService;
    }

    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        AppUser user = current.get();
        if (!encoder.matches(request.currentPassword(), user.passwordHash)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Текущий пароль указан неверно");
        }
        user.passwordHash = encoder.encode(request.newPassword());
        user.lastPasswordChangeAt = Instant.now();
        users.save(user);
        auditService.record(user, "UPDATE", "ACCOUNT_SECURITY", user.id, "Смена пароля");
        return Map.of("ok", true, "message", "Пароль изменен");
    }
}
