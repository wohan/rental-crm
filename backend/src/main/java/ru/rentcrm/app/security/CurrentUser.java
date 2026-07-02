package ru.rentcrm.app.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.rentcrm.app.exception.ApiException;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.repository.UserRepository;

import java.util.UUID;

@Component
public class CurrentUser {
    private final UserRepository users;

    public CurrentUser(UserRepository users) {
        this.users = users;
    }

    public AppUser get() {
        String email = String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return users.findByEmail(email)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
    }

    public UUID accountId() {
        return get().accountId;
    }
}
