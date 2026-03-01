package ru.nordcodes.base;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import lombok.extern.log4j.Log4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.nordcodes.client.AppApiClient;
import ru.nordcodes.config.TestConfig;
import ru.nordcodes.steps.MockSteps;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Абстрактный базовый класс для тестов, предоставляющий общую инфраструктуру инициализации.
 * <p>
 * Класс отвечает за:
 * <ul>
 *   <li>Настройку и регистрацию {@link WireMockExtension} для мокирования HTTP-запросов</li>
 *   <li>Инициализацию {@link RestAssured} с логированием при ошибках валидации</li>
 *   <li>Создание базовой {@link RequestSpecification} с заголовками по умолчанию</li>
 *   <li>Настройку параметров для отчётов Allure</li>
 *   <li>Управление жизненным циклом тестового контекста</li>
 *   <li>Предоставление утилитных методов для работы с чувствительными данными</li>
 * </ul>
 * <p>
 * Наследуемые тестовые классы автоматически получают доступ к:
 * <ul>
 *   <li>{@link #mockSteps} — шаги для работы с WireMock</li>
 *   <li>{@link #appClient} — клиент для взаимодействия с тестируемым приложением</li>
 *   <li>{@link #defaultSpec} — спецификация запросов с предустановленными заголовками</li>
 * </ul>
 *
 * @author Карпов Даниил
 * @email karpov.k-r@yandex.ru
 * @telegram <a href="https://t.me/Dez4526">https://t.me/Dez4526</a>
 * @see WireMockExtension
 * @see RestAssured
 * @see Allure
 * @since 1.0
 */
@Log4j
public abstract class BaseTest {

    /**
     * Экземпляр WireMock для мокирования внешних зависимостей.
     * <p>
     * Настроен на запуск на localhost с портом из {@link TestConfig#MOCK_PORT}.
     * Опция {@code proxyPassThrough(false)} обеспечивает полную изоляцию моков.
     */
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .port(TestConfig.MOCK_PORT)
                    .bindAddress("localhost")
                    .proxyPassThrough(false))
            .build();

    /** Шаг для настройки и верификации моков через WireMock. */
    protected static MockSteps mockSteps;

    /** API-клиент для взаимодействия с тестируемым приложением. */
    protected static AppApiClient appClient;

    /** Базовая спецификация запросов с предустановленными заголовками и URI. */
    protected static RequestSpecification defaultSpec;

    /** Контекст текущего теста для хранения временных данных. */
    protected TestContext testContext;

    /**
     * Инициализирует тестовую инфраструктуру перед запуском всех тестов.
     * <p>
     * Выполняет следующие действия:
     * <ol>
     *   <li>Создаёт экземпляр {@link MockSteps} для работы с WireMock</li>
     *   <li>Включает логирование запросов/ответов RestAssured при ошибках валидации</li>
     *   <li>Отключает URL-кодирование в RestAssured для корректной обработки параметров</li>
     *   <li>Создаёт {@link RequestSpecification} с базовым URI и заголовками:
     *       <ul>
     *         <li>{@code Content-Type: application/x-www-form-urlencoded}</li>
     *         <li>{@code Accept: application/json}</li>
     *       </ul>
     *   </li>
     *   <li>Инициализирует {@link AppApiClient} с созданной спецификацией</li>
     *   <li>Добавляет параметры конфигурации в отчёт Allure</li>
     * </ol>
     *
     * @apiNote Метод выполняется один раз перед всеми тестами в классе
     * @see TestConfig#APP_BASE_URL
     * @see TestConfig#MOCK_PORT
     */
    @BeforeAll
    static void setUpInfrastructure() {
        log.info("Инициализация тестовой инфраструктуры...");

        mockSteps = new MockSteps(wireMock);

        // RestAssured
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.urlEncodingEnabled = false;

        // Базовая спецификация
        defaultSpec = new RequestSpecBuilder()
                .setBaseUri(TestConfig.APP_BASE_URL)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();

        appClient = new AppApiClient(defaultSpec);

        // Allure параметры
        Allure.parameter("app.url", TestConfig.APP_BASE_URL);
        Allure.parameter("mock.port", String.valueOf(TestConfig.MOCK_PORT));

        log.info("Инфраструктура готова");
    }

    /**
     * Сбрасывает состояние после выполнения каждого теста.
     * <p>
     * Выполняет очистку {@link TestContext}, если он был инициализирован,
     * для обеспечения изоляции тестов друг от друга.
     *
     * @apiNote Вызывается автоматически после каждого @Test метода
     */
    @AfterEach
    void resetAfterTest() {

        if (testContext != null) {
            testContext.clear();
        }

        log.debug("Тест завершён, контекст очищен");
    }

    /**
     * Инициализирует новый экземпляр {@link TestContext} для текущего теста.
     * <p>
     * Рекомендуется вызывать в начале каждого теста для подготовки чистого контекста.
     *
     * @see TestContext
     */
    protected void initTestContext() {
        this.testContext = new TestContext();
    }

    /**
     * Добавляет параметр в отчёт Allure с опцией маскировки чувствительных данных.
     *
     * @param name      имя параметра для отображения в отчёте
     * @param value     значение параметра
     * @param sensitive флаг маскировки: если {@code true}, значение заменяется на "***"
     * @apiNote Используйте для логирования токенов, паролей и других конфиденциальных данных
     * @see Allure(String, String)
     */
    protected void addParameter(String name, String value, boolean sensitive) {
        Allure.parameter(name, sensitive ? "***" : value);
    }

    /**
     * Маскирует токен для безопасного отображения в логах и отчётах.
     *
     * @param token исходное значение токена
     * @return замаскированная строка:
     *         <ul>
     *           <li>{@code "****"} — если токен {@code null} или короче 8 символов</li>
     *           <li>{@code первые 8 символов + "..." + последние 4 символа} — для валидных токенов</li>
     *         </ul>
     * @apiNote Формат маскировки: {@code abcdefgh...xyz1}
     */
    protected String maskToken(String token) {
        if (token == null || token.length() < 8) return "****";
        return token.substring(0, 8) + "..." + token.substring(token.length() - 4);
    }
}