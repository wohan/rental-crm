package ru.rentcrm.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.rentcrm.app.model.ErrorEvent;
import ru.rentcrm.app.service.ErrorTrackingService;

import java.util.Map;

@RestControllerAdvice
public class ApiErrors {
    private static final Logger log = LoggerFactory.getLogger(ApiErrors.class);

    private final ErrorTrackingService errorTracking;

    public ApiErrors(ErrorTrackingService errorTracking) {
        this.errorTracking = errorTracking;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> api(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Ошибка валидации",
            "details", ex.getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList()
        ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> uploadTooLarge() {
        return ResponseEntity.badRequest().body(Map.of("error", "Файл больше 30 МБ"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> unexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled backend error path={}", request.getRequestURI(), ex);
        ErrorEvent event = errorTracking.capture(ex, request);
        return ResponseEntity.internalServerError().body(Map.of(
            "error", "Внутренняя ошибка сервера",
            "errorId", event.id
        ));
    }
}
