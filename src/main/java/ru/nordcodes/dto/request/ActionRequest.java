package ru.nordcodes.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nordcodes.dto.enums.UserAction;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * DTO для запроса к эндпоинту /endpoint тестируемого приложения.
 *
 * Формат запроса:
 * POST /endpoint
 * Content-Type: application/x-www-form-urlencoded
 * Body: token=${token}&action=${action}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionRequest {

    /**
     * Токен: строка длиной 32 символа, символы A-Z0-9
     */
    private String token;

    /**
     * Действие пользователя: LOGIN | ACTION | LOGOUT
     */
    @Builder.Default
    private UserAction action = UserAction.LOGIN;

    /**
     * Конструктор с строковым значением действия (для удобства).
     */
    public ActionRequest(String token, String action) {
        this.token = token;
        this.action = UserAction.fromString(action);
    }

    /**
     * Преобразует запрос в формат application/x-www-form-urlencoded.
     * @return строка вида "token=ABC123&action=LOGIN"
     */
    public String toFormUrlEncoded() {
        if (token == null || action == null) {
            throw new IllegalStateException("Token и action должны быть определены");
        }
        return String.format("token=%s&action=%s",
                urlEncode(token),
                urlEncode(action.getValue()));
    }

    /**
     * Валидирует токен согласно требованиям ТЗ.
     * @return true если токен валиден
     */
    public boolean isValidToken() {
        if (token == null || token.length() != 32) {
            return false;
        }
        return token.matches("^[A-Z0-9]{32}$");
    }

    /**
     * URL-encoding для безопасной передачи параметров.
     */
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // UTF-8 всегда поддерживается в Java
            return value;
        }
    }

    /**
     * Быстрый builder для тестов.
     */
    public static ActionRequest builderWithToken(String token) {
        return ActionRequest.builder()
                .token(token)
                .action(UserAction.LOGIN)  // Значение по умолчанию
                .build();  // ← Сразу вызываем build()
    }
}