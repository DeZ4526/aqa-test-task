package ru.nordcodes.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) для десериализации JSON-ответов от тестируемого приложения.
 * <p>
 * Класс представляет структуру ответа API и предоставляет удобные методы
 * для проверки статуса операции и извлечения данных.
 * <p>
 * <b>Форматы ответов:</b>
 * <pre>{@code
 * // Успешный ответ:
 * { "result": "OK" }
 *
 * // Ответ с ошибкой:
 * { "result": "ERROR", "message": "Invalid token" }
 *
 * // Ответ с дополнительными полями (игнорируются благодаря @JsonIgnoreProperties):
 * { "result": "OK", "timestamp": 1234567890, "version": "1.0" }
 * }</pre>
 * <p>
 * <b>Основные возможности:</b>
 * <ul>
 *   <li>Автоматическая десериализация JSON через Jackson с маппингом полей {@code result} и {@code message}</li>
 *   <li>Игнорирование неизвестных полей через {@code @JsonIgnoreProperties} для совместимости с будущими версиями API</li>
 *   <li>Типобезопасные методы проверки: {@link #isSuccess()}, {@link #isError()}</li>
 *   <li>Безопасный доступ к сообщению через {@link #getMessageOrEmpty()} (гарантирует non-null возврат)</li>
 *   <li>Фабричные методы {@link #success()} и {@link #error(String)} для создания моков в тестах</li>
 * </ul>
 * <p>
 * <b>Пример использования:</b>
 * <pre>{@code
 * // Десериализация ответа из RestAssured
 * AppResponse response = RestAssured.given()
 *     .spec(defaultSpec)
 *     .body(request)
 *     .post("/endpoint")
 *     .then()
 *     .extract()
 *     .as(AppResponse.class);
 *
 * // Проверка результата
 * if (response.isSuccess()) {
 *     // Обработка успешного сценария
 *     log.info("Операция выполнена успешно");
 * } else if (response.isError()) {
 *     // Обработка ошибки с безопасным получением сообщения
 *     log.error("Ошибка: " + response.getMessageOrEmpty());
 * }
 *
 * // Создание тестовых ответов
 * AppResponse mockSuccess = AppResponse.success();
 * AppResponse mockError = AppResponse.error("Token expired");
 * }</pre>
 * <p>
 * <b>Интеграция с Jackson:</b>
 * <ul>
 *   <li>{@code @JsonProperty} обеспечивает корректный маппинг JSON-полей на Java-поля</li>
 *   <li>{@code @JsonIgnoreProperties(ignoreUnknown = true)} предотвращает ошибки при появлении новых полей в API</li>
 *   <li>Lombok-аннотации генерируют getter/setter, конструкторы и builder для уменьшения boilerplate-кода</li>
 * </ul>
 *
 * @author Карпов Даниил
 * @email karpov.k-r@yandex.ru
 * @telegram https://t.me/Dez4526
 * @see ru.nordcodes.client.AppApiClient#sendAction(ru.nordcodes.dto.request.ActionRequest)
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // Игнорируем лишние поля (future-proof)
public class AppResponse {

    /**
     * Результат выполнения операции.
     * <p>
     * <b>Возможные значения:</b>
     * <ul>
     *   <li>{@code "OK"} — операция выполнена успешно</li>
     *   <li>{@code "ERROR"} — произошла ошибка при обработке запроса</li>
     * </ul>
     * <p>
     * Поле маппится из JSON-ключа {@code "result"} через {@link JsonProperty}.
     * Сравнение значений выполняется без учёта регистра в методах {@link #isSuccess()} и {@link #isError()}.
     *
     * @see #isSuccess()
     * @see #isError()
     * @see ru.nordcodes.config.TestConfig#RESPONSE_OK
     * @see ru.nordcodes.config.TestConfig#RESPONSE_ERROR
     */
    @JsonProperty("result")
    private String result;

    /**
     * Детальное сообщение об ошибке.
     * <p>
     * Присутствует в ответе только при {@code result = "ERROR"}.
     * Содержит человеко-читаемое описание причины неудачи для отладки и логирования.
     * <p>
     * <b>Примеры значений:</b>
     * <ul>
     *   <li>{@code "Invalid token format"}</li>
     *   <li>{@code "Session expired"}</li>
     *   <li>{@code "Action not allowed"}</li>
     * </ul>
     * <p>
     * Поле может быть {@code null}, если сервер не вернул сообщение.
     * Для безопасного получения значения используйте {@link #getMessageOrEmpty()}.
     *
     * @see #getMessageOrEmpty()
     * @see #isError()
     */
    @JsonProperty("message")
    private String message;

    /**
     * Проверяет, является ли ответ успешным.
     * <p>
     * Возвращает {@code true}, если поле {@link #result} равно {@code "OK"}
     * (сравнение выполняется без учёта регистра через {@link String#equalsIgnoreCase(String)}).
     *
     * @return {@code true} если операция выполнена успешно, иначе {@code false}
     * @apiNote Метод безопасен к {@code null} значению поля {@code result}
     * @see #isError()
     * @see #getResult()
     */
    public boolean isSuccess() {
        return "OK".equalsIgnoreCase(result);
    }

    /**
     * Проверяет, содержит ли ответ ошибку.
     * <p>
     * Возвращает {@code true}, если поле {@link #result} равно {@code "ERROR"}
     * (сравнение выполняется без учёта регистра).
     *
     * @return {@code true} если операция завершилась с ошибкой, иначе {@code false}
     * @apiNote Метод безопасен к {@code null} значению поля {@code result}
     * @see #isSuccess()
     * @see #getMessageOrEmpty()
     */
    public boolean isError() {
        return "ERROR".equalsIgnoreCase(result);
    }

    /**
     * Возвращает сообщение об ошибке или пустую строку, если сообщение отсутствует.
     * <p>
     * Метод гарантирует возврат non-null значения, что упрощает использование
     * в строковых операциях и логировании без дополнительных проверок на {@code null}.
     *
     * @return содержимое поля {@link #message} или пустая строка {@code ""}, если message равен {@code null}
     * @apiNote Рекомендуется использовать вместо прямого обращения к {@code getMessage()}
     * @see #getMessage()
     * @see #isError()
     */
    public String getMessageOrEmpty() {
        return message != null ? message : "";
    }

    /**
     * Фабричный метод для создания экземпляра успешного ответа.
     * <p>
     * <b>Эквивалентно:</b>
     * <pre>{@code
     * AppResponse.builder().result("OK").build();
     * }</pre>
     * <p>
     * Удобно для использования в тестах при мокировании ответов или создании
     * ожидаемых значений для ассертов.
     *
     * @return новый экземпляр {@link AppResponse} с {@code result = "OK"} и {@code message = null}
     * @see #error(String)
     * @see ru.nordcodes.config.TestConfig#RESPONSE_OK
     */
    public static AppResponse success() {
        return AppResponse.builder().result("OK").build();
    }

    /**
     * Фабричный метод для создания экземпляра ответа с ошибкой.
     * <p>
     * <b>Эквивалентно:</b>
     * <pre>{@code
     * AppResponse.builder().result("ERROR").message(reason).build();
     * }</pre>
     * <p>
     * Удобно для использования в тестах при мокировании ошибок или проверке
     * обработки неудачных сценариев.
     *
     * @param reason текстовое описание причины ошибки
     * @return новый экземпляр {@link AppResponse} с {@code result = "ERROR"} и заданным сообщением
     * @see #success()
     * @see ru.nordcodes.config.TestConfig#RESPONSE_ERROR
     */
    public static AppResponse error(String reason) {
        return AppResponse.builder()
                .result("ERROR")
                .message(reason)
                .build();
    }
}