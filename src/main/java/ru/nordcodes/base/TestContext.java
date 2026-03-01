package ru.nordcodes.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nordcodes.dto.response.AppResponse;

/**
 * Контекст выполнения теста для хранения состояния между шагами одного тестового сценария.
 * <p>
 * Класс предназначен для передачи данных (токен авторизации, ответы API, флаги состояния)
 * между методами тестов без необходимости объявления полей в самом тест-классе.
 * Обеспечивает изоляцию состояния каждого теста и упрощает управление жизненным циклом тестовых данных.
 * <p>
 * <b>Основные возможности:</b>
 * <ul>
 *   <li>Хранение токена авторизации текущего пользователя</li>
 *   <li>Сохранение последнего ответа от приложения типа {@link AppResponse}</li>
 *   <li>Отслеживание состояния авторизации через флаг {@code loggedIn}</li>
 *   <li>Поддержка произвольных тестовых данных через поле {@code testData}</li>
 *   <li>Методы для безопасного управления сессией: {@link #markLoggedIn(String)}, {@link #markLoggedOut()}</li>
 *   <li>Проверка готовности к выполнению действий через {@link #canPerformAction()}</li>
 *   <li>Полная очистка состояния через {@link #clear()} для изоляции тестов</li>
 * </ul>
 * <p>
 * <b>Пример использования:</b>
 * <pre>{@code
 * @Test
 * void testUserFlow() {
 *     initTestContext(); // инициализация нового контекста
 *
 *     // Шаг 1: авторизация
 *     testContext.markLoggedIn("abc123token");
 *
 *     // Шаг 2: выполнение действия с проверкой
 *     if (testContext.canPerformAction()) {
 *         AppResponse response = appClient.doAction();
 *         testContext.setLastResponse(response);
 *     }
 *
 *     // Шаг 3: завершение сессии
 *     testContext.markLoggedOut();
 * }
 * }</pre>
 *
 * @author Карпов Даниил
 * @email karpov.k-r@yandex.ru
 * @telegram https://t.me/Dez4526
 * @see AppResponse
 * @see BaseTest#initTestContext()
 * @see BaseTest#resetAfterTest()
 * @since 1.0
 */
@Data
@NoArgsConstructor
public class TestContext {

    /**
     * Токен авторизации текущего пользователя.
     * <p>
     * Устанавливается при успешном входе через {@link #markLoggedIn(String)}
     * и сбрасывается при выходе через {@link #markLoggedOut()}.
     */
    private String token;

    /**
     * Последний полученный ответ от тестируемого приложения.
     * <p>
     * Позволяет передавать данные между шагами теста без дополнительных параметров.
     */
    private AppResponse lastResponse;

    /**
     * Флаг состояния авторизации пользователя.
     * <p>
     * {@code true} — пользователь авторизован, токен валиден.<br>
     * {@code false} — сессия не активна или завершена.
     *
     * @see #markLoggedIn(String)
     * @see #markLoggedOut()
     */
    private boolean loggedIn;

    /**
     * Поле для хранения произвольных тестовых данных.
     * <p>
     * Может использоваться для передачи строк между шагами теста,
     * например: ID сущностей, ожидаемые значения, временные метки.
     */
    private String testData;

    /**
     * Устанавливает токен авторизации и активирует флаг входа в систему.
     * <p>
     * Вызывается после успешной аутентификации для подготовки контекста
     * к выполнению действий, требующих авторизации.
     *
     * @param token валидный токен доступа, полученный от сервера
     * @apiNote После вызова этого метода {@link #canPerformAction()} вернёт {@code true}
     * @see #canPerformAction()
     * @see #markLoggedOut()
     */
    public void markLoggedIn(String token) {
        this.token = token;
        this.loggedIn = true;
    }

    /**
     * Завершает пользовательскую сессию: сбрасывает токен и флаг авторизации.
     * <p>
     * Вызывается после выполнения операций LOGOUT или при необходимости
     * принудительного завершения сессии в рамках теста.
     *
     * @apiNote После вызова этого метода {@link #canPerformAction()} вернёт {@code false}
     * @see #markLoggedIn(String)
     */
    public void markLoggedOut() {
        this.token = null;
        this.loggedIn = false;
    }

    /**
     * Проверяет готовность контекста к выполнению действий, требующих авторизации.
     * <p>
     * Возвращает {@code true} только если выполнены все условия:
     * <ul>
     *   <li>Флаг {@link #loggedIn} установлен в {@code true}</li>
     *   <li>Поле {@link #token} не равно {@code null}</li>
     *   <li>Поле {@link #token} не является пустой строкой</li>
     * </ul>
     *
     * @return {@code true}, если пользователь авторизован и токен валиден; иначе {@code false}
     * @apiNote Рекомендуется использовать перед вызовами API, требующими аутентификации
     */
    public boolean canPerformAction() {
        return loggedIn && token != null && !token.isEmpty();
    }

    /**
     * Полностью очищает состояние контекста, сбрасывая все поля в значения по умолчанию.
     * <p>
     * Вызывается автоматически после каждого теста через {@link BaseTest#resetAfterTest()}
     * для обеспечения изоляции тестовых сценариев и предотвращения утечек состояния.
     *
     * @apiNote Метод гарантирует, что следующий тест начнёт с чистого контекста
     */
    public void clear() {
        this.token = null;
        this.lastResponse = null;
        this.loggedIn = false;
        this.testData = null;
    }
}