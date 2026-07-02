package ru.rentcrm.app.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.dto.AuthResponse;
import ru.rentcrm.app.dto.ForgotPasswordRequest;
import ru.rentcrm.app.dto.LoginRequest;
import ru.rentcrm.app.dto.RegisterRequest;
import ru.rentcrm.app.dto.ResetPasswordRequest;
import ru.rentcrm.app.exception.ApiException;
import ru.rentcrm.app.model.Account;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.AuthToken;
import ru.rentcrm.app.model.AuthTokenType;
import ru.rentcrm.app.model.Tariff;
import ru.rentcrm.app.repository.AccountRepository;
import ru.rentcrm.app.repository.UserRepository;
import ru.rentcrm.app.security.JwtService;
import ru.rentcrm.app.service.AuthEmailService;
import ru.rentcrm.app.service.AuthRateLimitService;
import ru.rentcrm.app.service.AuthTokenService;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountRepository accounts;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final AuthRateLimitService rateLimit;
    private final AuthTokenService authTokens;
    private final AuthEmailService emailService;
    private final String configuredPublicBaseUrl;

    public AuthController(
        AccountRepository accounts,
        UserRepository users,
        PasswordEncoder encoder,
        JwtService jwt,
        AuthRateLimitService rateLimit,
        AuthTokenService authTokens,
        AuthEmailService emailService,
        @Value("${app.auth.public-base-url:}") String configuredPublicBaseUrl
    ) {
        this.accounts = accounts;
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.rateLimit = rateLimit;
        this.authTokens = authTokens;
        this.emailService = emailService;
        this.configuredPublicBaseUrl = configuredPublicBaseUrl;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req, HttpServletRequest request) {
        rateLimit.check(req.email(), clientIp(request));
        if (!Boolean.TRUE.equals(req.termsAccepted()) || !Boolean.TRUE.equals(req.privacyAccepted())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Для регистрации нужно принять оферту и политику персональных данных");
        }
        if (users.existsByEmail(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email уже зарегистрирован");
        }

        Account account = new Account();
        account.name = req.accountName();
        account.tariff = Tariff.FREE;
        account.objectsLimit = 3;
        accounts.save(account);

        AppUser user = new AppUser();
        user.accountId = account.id;
        user.email = req.email().toLowerCase(Locale.ROOT);
        user.fullName = req.fullName();
        user.passwordHash = encoder.encode(req.password());
        user.emailVerified = false;
        Instant now = Instant.now();
        user.termsAcceptedAt = now;
        user.privacyAcceptedAt = now;
        user.notificationConsentAt = Boolean.TRUE.equals(req.notificationConsent()) ? now : null;
        user.lastPasswordChangeAt = now;
        users.save(user);
        rateLimit.reset(req.email(), clientIp(request));
        String verifyToken = authTokens.issue(user, AuthTokenType.EMAIL_VERIFICATION, Duration.ofDays(3));
        String verifyLink = verificationLink(request, verifyToken);
        emailService.sendVerification(user.email, verifyLink);

        return response(user, emailService.devLinksEnabled() ? verifyLink : null);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        rateLimit.check(req.email(), clientIp(request));
        AppUser user = users.findByEmail(req.email().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль"));
        if (!encoder.matches(req.password(), user.passwordHash)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
        }
        rateLimit.reset(req.email(), clientIp(request));
        return response(user, null);
    }

    @GetMapping("/verify-email")
    public Map<String, Object> verifyEmail(@RequestParam String token) {
        AuthToken authToken = authTokens.consume(token, AuthTokenType.EMAIL_VERIFICATION);
        AppUser user = users.findById(authToken.userId)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Пользователь не найден"));
        user.emailVerified = true;
        user.emailVerifiedAt = Instant.now();
        users.save(user);
        return Map.of("ok", true, "message", "Email подтвержден");
    }

    @PostMapping("/forgot-password")
    public Map<String, Object> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req, HttpServletRequest request) {
        rateLimit.check(req.email(), clientIp(request));
        return users.findByEmail(req.email().toLowerCase(Locale.ROOT))
            .map(user -> {
                String token = authTokens.issue(user, AuthTokenType.PASSWORD_RESET, Duration.ofHours(2));
                String link = resetLink(request, token);
                emailService.sendPasswordReset(user.email, link);
                rateLimit.reset(req.email(), clientIp(request));
                return Map.<String, Object>of(
                    "ok", true,
                    "message", "Если email зарегистрирован, ссылка для сброса пароля создана",
                    "resetLink", emailService.devLinksEnabled() ? link : ""
                );
            })
            .orElseGet(() -> Map.of("ok", true, "message", "Если email зарегистрирован, ссылка для сброса пароля создана"));
    }

    @PostMapping("/reset-password")
    public Map<String, Object> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        AuthToken authToken = authTokens.consume(req.token(), AuthTokenType.PASSWORD_RESET);
        AppUser user = users.findById(authToken.userId)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Пользователь не найден"));
        user.passwordHash = encoder.encode(req.newPassword());
        users.save(user);
        return Map.of("ok", true, "message", "Пароль изменен");
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private AuthResponse response(AppUser user, String verificationLink) {
        return new AuthResponse(jwt.issue(user), user.accountId, user.fullName, user.role.name(), user.emailVerified, verificationLink);
    }

    private String verificationLink(HttpServletRequest request, String token) {
        return publicBaseUrl(request) + "/api/auth/verify-email?token=" + token;
    }

    private String resetLink(HttpServletRequest request, String token) {
        return publicBaseUrl(request) + "/reset-password?token=" + token;
    }

    private String publicBaseUrl(HttpServletRequest request) {
        if (configuredPublicBaseUrl != null && !configuredPublicBaseUrl.isBlank()) {
            return configuredPublicBaseUrl.replaceAll("/+$", "");
        }
        String proto = headerOrDefault(request, "X-Forwarded-Proto", request.getScheme());
        String host = headerOrDefault(request, "X-Forwarded-Host", headerOrDefault(request, "Host", "localhost"));
        return proto + "://" + host;
    }

    private String headerOrDefault(HttpServletRequest request, String name, String fallback) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
