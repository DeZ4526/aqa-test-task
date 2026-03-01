package ru.nordcodes.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа от тестируемого приложения.
 *
 * Успешный ответ:
 * { "result": "OK" }
 *
 * Неуспешный ответ:
 * { "result": "ERROR", "message": "reason" }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // Игнорируем лишние поля (future-proof)
public class AppResponse {

    /**
     * Результат операции: "OK" или "ERROR"
     */
    @JsonProperty("result")
    private String result;

    /**
     * Сообщение об ошибке (присутствует только при result=ERROR)
     */
    @JsonProperty("message")
    private String message;

    /**
     * Проверка: успешный ли ответ.
     */
    public boolean isSuccess() {
        return "OK".equalsIgnoreCase(result);
    }

    /**
     * Проверка: ошибка ли.
     */
    public boolean isError() {
        return "ERROR".equalsIgnoreCase(result);
    }

    /**
     * Безопасное получение сообщения (не null).
     */
    public String getMessageOrEmpty() {
        return message != null ? message : "";
    }

    /**
     * Factory method для успешного ответа (удобно в тестах).
     */
    public static AppResponse success() {
        return AppResponse.builder().result("OK").build();
    }

    /**
     * Factory method для ответа с ошибкой.
     */
    public static AppResponse error(String reason) {
        return AppResponse.builder()
                .result("ERROR")
                .message(reason)
                .build();
    }
}