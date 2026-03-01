package ru.nordcodes.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Перечисление доступных действий пользователя в тестируемом приложении.
 * <p>
 * Каждый константный элемент содержит:
 * <ul>
 *   <li>{@code value} — строковое представление действия для передачи в API-запросах</li>
 *   <li>{@code description} — человеко-читаемое описание бизнес-логики действия</li>
 * </ul>
 * <p>
 * <b>Жизненный цикл сессии:</b>
 * <ol>
 *   <li>{@link #LOGIN} — начальное действие для аутентификации пользователя</li>
 *   <li>{@link #ACTION} — основное действие, доступное только после успешного {@code LOGIN}</li>
 *   <li>{@link #LOGOUT} — завершающее действие для очистки сессии и токена</li>
 * </ol>
 * <p>
 * <b>Пример использования:</b>
 * <pre>{@code
 * // Прямое использование константы
 * UserAction action = UserAction.LOGIN;
 * String apiValue = action.getValue(); // "LOGIN"
 *
 * // Парсинг из строкового значения (case-insensitive)
 * UserAction parsed = UserAction.fromString("login"); // вернёт UserAction.LOGIN
 *
 * // Валидация пользовательского ввода
 * if (UserAction.isValid(input)) {
 *     // безопасное использование значения
 * }
 * }</pre>
 * <p>
 * <b>Интеграция с API:</b>
 * <ul>
 *   <li>Значение {@link #getValue()} используется при формировании тела запроса: {@code action=${value}}</li>
 *   <li>Описание {@link #getDescription()} может использоваться для генерации документации или логирования</li>
 * </ul>
 *
 * @author Карпов Даниил
 * @email karpov.k-r@yandex.ru
 * @telegram https://t.me/Dez4526
 * @see ru.nordcodes.dto.request.ActionRequest
 * @see ru.nordcodes.client.AppApiClient
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum UserAction {

    /**
     * Аутентификация пользователя.
     * <p>
     * Отправляет запрос на эндпоинт {@code /auth} во внешний сервис для проверки токена
     * и создания сессии. Является обязательным первым шагом перед выполнением других действий.
     * <p>
     * <b>Особенности:</b>
     * <ul>
     *   <li>Не требует предварительной авторизации</li>
     *   <li>При успехе возвращает подтверждение сессии</li>
     *   <li>При ошибке токен помечается как невалидный</li>
     * </ul>
     */
    LOGIN("LOGIN", "Аутентификация: отправляет запрос /auth во внешний сервис"),

    /**
     * Выполнение основного действия пользователя.
     * <p>
     * Доступно только после успешной аутентификации через {@link #LOGIN}.
     * Представляет собой бизнес-операцию, ради которой проектируется тестируемый сценарий.
     * <p>
     * <b>Требования:</b>
     * <ul>
     *   <li>Обязательное наличие валидного токена в запросе</li>
     *   <li>Сессия должна быть активна (не истекла, не отозвана)</li>
     *   <li>Предварительный вызов {@code LOGIN} с тем же токеном</li>
     * </ul>
     *
     * @see #LOGIN
     * @see #LOGOUT
     */
    ACTION("ACTION", "Выполнение действия: доступно только после успешного LOGIN"),

    /**
     * Завершение пользовательской сессии.
     * <p>
     * Удаляет токен из внутреннего хранилища сессий, делая невозможным
     * дальнейшее выполнение действий {@link #ACTION} без повторной аутентификации.
     * <p>
     * <b>Поведение:</b>
     * <ul>
     *   <li>Токен удаляется из хранилища независимо от его валидности</li>
     *   <li>Повторный вызов {@code LOGOUT} с тем же токеном вернёт ошибку</li>
     *   <li>После успеха требуется новый {@code LOGIN} для продолжения работы</li>
     * </ul>
     */
    LOGOUT("LOGOUT", "Завершение сессии: удаляет токен из внутреннего хранилища");

    /**
     * Строковое представление действия для использования в API-запросах.
     * <p>
     * Значение передаётся в теле запроса в формате {@code application/x-www-form-urlencoded}:
     * <pre>action={@link #value}</pre>
     * <p>
     * <b>Важно:</b> Значение чувствительно к регистру на стороне сервера,
     * поэтому используйте константы enum вместо хардкода строк.
     *
     * @see #getDescription()
     * @see #fromString(String)
     */
    private final String value;

    /**
     * Человеко-читаемое описание бизнес-логики действия.
     * <p>
     * Используется для:
     * <ul>
     *   <li>Генерации документации к API</li>
     *   <li>Детального логирования тестовых сценариев</li>
     *   <li>Формирования понятных сообщений об ошибках</li>
     * </ul>
     *
     * @see #getValue()
     */
    private final String description;

    /**
     * Преобразует строковое значение в соответствующий элемент перечисления.
     * <p>
     * <b>Алгоритм сравнения:</b>
     * <ol>
     *   <li>Если входное значение {@code null} — немедленно возвращает {@code null}</li>
     *   <li>Выполняет {@code trim()} для удаления ведущих/завершающих пробелов</li>
     *   <li>Сравнение выполняется без учёта регистра через {@link String#equalsIgnoreCase(String)}</li>
     *   <li>Возвращает первый совпавший элемент enum или {@code null} при отсутствии совпадений</li>
     * </ol>
     * <p>
     * <b>Примеры:</b>
     * <pre>{@code
     * UserAction.fromString("LOGIN")     // вернёт UserAction.LOGIN
     * UserAction.fromString("  action ") // вернёт UserAction.ACTION
     * UserAction.fromString("invalid")   // вернёт null
     * UserAction.fromString(null)        // вернёт null
     * }</pre>
     *
     * @param value строковое представление действия (например, из запроса или конфигурации)
     * @return соответствующий элемент {@link UserAction} или {@code null}, если значение не распознано
     * @apiNote Метод безопасен к {@code null} и лишним пробелам во входных данных
     * @see #isValid(String)
     * @see #getValue()
     */
    public static UserAction fromString(String value) {
        if (value == null) return null;
        for (UserAction action : values()) {
            if (action.value.equalsIgnoreCase(value.trim())) {
                return action;
            }
        }
        return null;
    }

    /**
     * Проверяет, является ли переданная строка валидным действием.
     * <p>
     * Метод является удобной обёрткой над {@link #fromString(String)}
     * и возвращает {@code true}, если строка соответствует одному из значений enum.
     * <p>
     * <b>Использует те же правила сравнения:</b>
     * <ul>
     *   <li>Регистронезависимое сравнение</li>
     *   <li>Игнорирование ведущих и завершающих пробелов</li>
     *   <li>Безопасная обработка {@code null} (возвращает {@code false})</li>
     * </ul>
     *
     * @param value проверяемое строковое значение
     * @return {@code true} если строка соответствует одному из действий enum, иначе {@code false}
     * @apiNote Рекомендуется использовать для валидации пользовательского ввода перед парсингом
     * @see #fromString(String)
     * @see #values()
     */
    public static boolean isValid(String value) {
        return fromString(value) != null;
    }
}