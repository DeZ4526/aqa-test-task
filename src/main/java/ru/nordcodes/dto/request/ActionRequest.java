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
 * DTO (Data Transfer Object) для формирования запросов к эндпоинту {@code /endpoint} тестируемого приложения.
 * <p>
 * Класс инкапсулирует параметры запроса и обеспечивает их преобразование в формат
 * {@code application/x-www-form-urlencoded} для отправки через HTTP POST.
 * <p>
 * <b>Формат HTTP-запроса:</b>
 * <pre>
 * POST /endpoint
 * Content-Type: application/x-www-form-urlencoded
 * Body: token=${token}&action=${action}
 * </pre>
 * <p>
 * <b>Требования к полям:</b>
 * <ul>
 *   <li>{@code token} — строка длиной ровно 32 символа, содержащая только символы {@code A-Z} и {@code 0-9}</li>
 *   <li>{@code action} — одно из значений перечисления {@link UserAction}: {@code LOGIN}, {@code ACTION}, {@code LOGOUT}</li>
 * </ul>
 * <p>
 * <b>Примеры использования:</b>
 * <pre>{@code
 * // Создание через Lombok Builder
 * ActionRequest request = ActionRequest.builder()
 *     .token("ABCDEFGHIJKLMNOP1234567890ABCDEF")
 *     .action(UserAction.ACTION)
 *     .build();
 *
 * // Создание через конструктор со строковым действием
 * ActionRequest request = new ActionRequest("ABCDEFGHIJKLMNOP1234567890ABCDEF", "LOGIN");
 *
 * // Быстрый builder с токеном (действие по умолчанию: LOGIN)
 * ActionRequest request = ActionRequest.builderWithToken("ABCDEFGHIJKLMNOP1234567890ABCDEF");
 *
 * // Преобразование в form-urlencoded строку
 * String body = request.toFormUrlEncoded();
 * // Результат: "token=ABCDEFGHIJKLMNOP1234567890ABCDEF&action=LOGIN"
 *
 * // Валидация токена перед отправкой
 * if (request.isValidToken()) {
 *     client.sendAction(request);
 * }
 * }</pre>
 * <p>
 * <b>Безопасность:</b>
 * <ul>
 *   <li>Метод {@link #toFormUrlEncoded()} автоматически выполняет URL-кодирование параметров</li>
 *   <li>Метод {@link #isValidToken()} проверяет соответствие токена регламенту перед отправкой</li>
 *   <li>При отсутствии обязательных полей выбрасывается {@link IllegalStateException}</li>
 * </ul>
 *
 * @author Карпов Даниил
 * @email karpov.k-r@yandex.ru
 * @telegram https://t.me/Dez4526
 * @see UserAction
 * @see ru.nordcodes.client.AppApiClient#sendAction(ActionRequest)
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionRequest {

    /**
     * Токен авторизации пользователя.
     * <p>
     * <b>Требования к формату:</b>
     * <ul>
     *   <li>Длина: ровно 32 символа</li>
     *   <li>Допустимые символы: заглавные латинские буквы {@code A-Z} и цифры {@code 0-9}</li>
     *   <li>Регулярное выражение: {@code ^[A-Z0-9]{32}$}</li>
     * </ul>
     * <p>
     * Токен передаётся в теле запроса после URL-кодирования.
     * Для проверки соответствия формату используйте метод {@link #isValidToken()}.
     *
     * @see #isValidToken()
     * @see #toFormUrlEncoded()
     */
    private String token;

    /**
     * Действие пользователя, определяющее бизнес-логику обработки запроса.
     * <p>
     * Значение по умолчанию: {@link UserAction#LOGIN} — обеспечивает безопасное поведение
     * при создании объекта без явного указания действия.
     * <p>
     * Аннотация {@code @Builder.Default} гарантирует, что при использовании Lombok Builder
     * поле получит значение по умолчанию, если не было явно указано.
     *
     * @see UserAction
     * @see UserAction#LOGIN
     * @see UserAction#ACTION
     * @see UserAction#LOGOUT
     */
    @Builder.Default
    private UserAction action = UserAction.LOGIN;

    /**
     * Конструктор для создания запроса с токеном и строковым значением действия.
     * <p>
     * Удобный конструктор для случаев, когда действие поступает в виде строки
     * (например, из конфигурации, внешнего API или пользовательского ввода).
     * <p>
     * Выполняет преобразование строки в {@link UserAction} через {@link UserAction#fromString(String)}.
     * Если строка не распознана, поле {@code action} получит значение {@code null}.
     *
     * @param token  токен авторизации пользователя
     * @param action строковое представление действия (регистронезависимое)
     * @see UserAction#fromString(String)
     * @see #ActionRequest(String, UserAction)
     */
    public ActionRequest(String token, String action) {
        this.token = token;
        this.action = UserAction.fromString(action);
    }

    /**
     * Преобразует объект запроса в строку формата {@code application/x-www-form-urlencoded}.
     * <p>
     * <b>Формат результата:</b>
     * <pre>token={urlEncodedToken}&action={urlEncodedActionValue}</pre>
     * <p>
     * <b>Особенности реализации:</b>
     * <ul>
     *   <li>Оба параметра проходят URL-кодирование через {@link URLEncoder#encode(String, String)}</li>
     *   <li>Используется кодировка {@link StandardCharsets#UTF_8} (гарантированно поддерживается в Java)</li>
     *   <li>При отсутствии {@code token} или {@code action} выбрасывается {@link IllegalStateException}</li>
     * </ul>
     * <p>
     * <b>Пример:</b>
     * <pre>{@code
     * ActionRequest req = ActionRequest.builder()
     *     .token("ABC DEF 123")  // содержит пробелы
     *     .action(UserAction.LOGIN)
     *     .build();
     * String body = req.toFormUrlEncoded();
     * // Результат: "token=ABC+DEF+123&action=LOGIN"
     * }</pre>
     *
     * @return строка тела запроса, готовая к отправке через HTTP POST
     * @throws IllegalStateException если {@code token} или {@code action} равны {@code null}
     * @see #urlEncode(String)
     * @see UserAction#getValue()
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
     * Выполняет валидацию токена согласно техническим требованиям.
     * <p>
     * <b>Критерии валидности:</b>
     * <ol>
     *   <li>Токен не должен быть {@code null}</li>
     *   <li>Длина токена должна быть ровно 32 символа</li>
     *   <li>Токен должен соответствовать регулярному выражению {@code ^[A-Z0-9]{32}$}</li>
     * </ol>
     * <p>
     * <b>Примеры:</b>
     * <pre>{@code
     * new ActionRequest("ABCDEFGHIJKLMNOP1234567890ABCDEF", "LOGIN").isValidToken(); // true
     * new ActionRequest("abc123", "LOGIN").isValidToken();                           // false (короткий)
     * new ActionRequest("ABCDEFGHIJKLMNOP1234567890ABCDE!", "LOGIN").isValidToken(); // false (спецсимвол)
     * new ActionRequest(null, "LOGIN").isValidToken();                               // false (null)
     * }</pre>
     *
     * @return {@code true} если токен соответствует всем требованиям, иначе {@code false}
     * @apiNote Рекомендуется вызывать перед отправкой запроса для раннего обнаружения ошибок
     * @see #token
     */
    public boolean isValidToken() {
        if (token == null || token.length() != 32) {
            return false;
        }
        return token.matches("^[A-Z0-9]{32}$");
    }

    /**
     * Выполняет URL-кодирование строки для безопасной передачи в HTTP-запросах.
     * <p>
     * Использует {@link URLEncoder#encode(String, String)} с кодировкой {@link StandardCharsets#UTF_8}.
     * <p>
     * <b>Обработка исключений:</b>
     * Метод перехватывает {@link UnsupportedEncodingException}, однако в современных JVM
     * кодировка UTF-8 поддерживается всегда, поэтому исключение практически недостижимо.
     * В случае гипотетической ошибки возвращается исходное значение без кодирования.
     *
     * @param value строка для кодирования
     * @return URL-кодированная строка или исходное значение при ошибке
     * @see URLEncoder#encode(String, String)
     * @see StandardCharsets#UTF_8
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
     * Фабричный метод для быстрого создания запроса с указанным токеном.
     * <p>
     * <b>Особенности:</b>
     * <ul>
     *   <li>Устанавливает переданный токен</li>
     *   <li>Автоматически задаёт действие {@link UserAction#LOGIN} по умолчанию</li>
     *   <li>Немедленно вызывает {@code build()}, возвращая готовый объект</li>
     * </ul>
     * <p>
     * <b>Пример использования:</b>
     * <pre>{@code
     * // Вместо многословного builder:
     * ActionRequest req1 = ActionRequest.builder()
     *     .token("ABCDEFGHIJKLMNOP1234567890ABCDEF")
     *     .action(UserAction.LOGIN)
     *     .build();
     *
     * // Можно использовать компактный метод:
     * ActionRequest req2 = ActionRequest.builderWithToken("ABCDEFGHIJKLMNOP1234567890ABCDEF");
     * }</pre>
     *
     * @param token токен авторизации для включения в запрос
     * @return новый экземпляр {@link ActionRequest} с заданным токеном и действием {@code LOGIN}
     * @see #builder()
     * @see UserAction#LOGIN
     */
    public static ActionRequest builderWithToken(String token) {
        return ActionRequest.builder()
                .token(token)
                .action(UserAction.LOGIN)  // Значение по умолчанию
                .build();
    }
}