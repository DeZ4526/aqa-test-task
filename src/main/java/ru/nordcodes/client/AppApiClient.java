package ru.nordcodes.client;

import io.qameta.allure.Allure;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import ru.nordcodes.config.TestConfig;
import ru.nordcodes.dto.enums.UserAction;
import ru.nordcodes.dto.request.ActionRequest;

/**
 * Типобезопасный API-клиент для взаимодействия с тестируемым приложением.
 * <p>
 * Класс реализует отправку запросов к эндпоинту приложения с поддержкой:
 * <ul>
 *   <li>Автоматического формирования тела запроса в формате {@code application/x-www-form-urlencoded}</li>
 *   <li>Добавления обязательных заголовков: {@code X-Api-Key}, {@code Content-Type}, {@code Accept}</li>
 *   <li>Логирования запросов и ответов с интеграцией в отчёты Allure</li>
 *   <li>Типобезопасных методов для основных сценариев: {@code LOGIN}, {@code ACTION}, {@code LOGOUT}</li>
 * </ul>
 * <p>
 * <b>Конфигурация подключения:</b>
 * <pre>
 * Эндпоинт: POST {app.base.url}{app.endpoint}
 * Заголовки:
 *   - X-Api-Key: {@value ru.nordcodes.config.TestConfig#APP_API_KEY}
 *   - Content-Type: application/x-www-form-urlencoded
 *   - Accept: application/json
 * Тело запроса: token=${token}&action=${action}
 * </pre>
 * <p>
 * <b>Пример использования:</b>
 * <pre>{@code
 * // Создание клиента с дефолтной конфигурацией
 * AppApiClient client = AppApiClient.createDefault();
 *
 * // Отправка LOGIN-запроса
 * Response loginResponse = client.sendLogin("user_token_123");
 *
 * // Отправка произвольного действия через DTO
 * ActionRequest request = ActionRequest.builder()
 *     .token("user_token_123")
 *     .action(UserAction.ACTION)
 *     .build();
 * Response actionResponse = client.sendAction(request);
 *
 * // Валидация ответа
 * assertEquals(200, actionResponse.getStatusCode());
 * }</pre>
 * <p>
 * <b>Интеграция с Allure:</b>
 * <ul>
 *   <li>Тело запроса автоматически добавляется как вложение с именем {@code request_body}</li>
 *   <li>Тело ответа добавляется как вложение с именем {@code response_body} и MIME-типом {@code application/json}</li>
 *   <li>Все операции логируются через Log4j с уровнем {@code DEBUG}</li>
 * </ul>
 *
 * @author Карпов Даниил
 * @email karpov.k-r@yandex.ru
 * @telegram <a href="https://t.me/Dez4526">https://t.me/Dez4526</a>
 * @see ApiClient
 * @see ActionRequest
 * @see UserAction
 * @see TestConfig
 * @since 1.0
 */
@Log4j
public class AppApiClient extends ApiClient {

    /** Базовый URL тестируемого приложения из конфигурации {@link TestConfig#APP_BASE_URL}. */
    private final String baseUrl;

    /** Относительный путь эндпоинта API из конфигурации {@link TestConfig#APP_ENDPOINT}. */
    private final String endpoint;

    /** API-ключ для аутентификации запросов из конфигурации {@link TestConfig#APP_API_KEY}. */
    private final String apiKey;

    /**
     * Конструктор для инициализации клиента с предустановленной спецификацией запросов.
     * <p>
     * Автоматически загружает конфигурационные параметры из {@link TestConfig}:
     * <ul>
     *   <li>{@code baseUrl} — базовый адрес приложения</li>
     *   <li>{@code endpoint} — путь к API-методу</li>
     *   <li>{@code apiKey} — ключ авторизации для заголовка {@code X-Api-Key}</li>
     * </ul>
     *
     * @param baseSpec базовая спецификация RestAssured с общими настройками запросов
     * @see TestConfig#APP_BASE_URL
     * @see TestConfig#APP_ENDPOINT
     * @see TestConfig#APP_API_KEY
     */
    public AppApiClient(RequestSpecification baseSpec) {
        super(baseSpec);
        this.baseUrl = TestConfig.APP_BASE_URL;
        this.endpoint = TestConfig.APP_ENDPOINT;
        this.apiKey = TestConfig.APP_API_KEY;
    }

    /**
     * Отправляет запрос с действием к тестируемому приложению.
     * <p>
     * Метод выполняет следующие операции:
     * <ol>
     *   <li>Логирует параметры запроса в DEBUG-режиме (токен маскируется до первых 8 символов)</li>
     *   <li>Преобразует {@link ActionRequest} в строку формата {@code application/x-www-form-urlencoded}</li>
     *   <li>Добавляет тело запроса и заголовок {@code X-Api-Key} к спецификации</li>
     *   <li>Отправляет POST-запрос к эндпоинту, указанному в конфигурации</li>
     *   <li>Добавляет тела запроса и ответа как вложения в отчёт Allure</li>
     *   <li>Логирует статус код и результат ответа</li>
     * </ol>
     * <p>
     * <b>Важно:</b> Метод не обрабатывает исключения на уровне HTTP-статусов —
     * валидация ответа должна выполняться вызывающей стороной.
     *
     * @param request DTO запроса, содержащее токен и тип действия {@link UserAction}
     * @return {@link Response} с данными ответа сервера для последующей проверки
     * @apiNote Тело запроса и ответа автоматически добавляются в Allure-отчёт
     * @see ActionRequest#toFormUrlEncoded()
     * @see #attachToAllure(String, String, String)
     */
    @Override
    public Response sendAction(ActionRequest request) {
        log.debug("Отправка запроса: action="+request.getAction()+", token="
                + (request.getToken() != null ? request.getToken().substring(0, 8) : "null") +"...");

        String body = request.toFormUrlEncoded();

        // Аттач запроса в Allure (только если отчёт генерируется)
        attachToAllure("request_body", body, "text/plain");

        Response response = baseSpec
                .header("X-Api-Key", apiKey)
                .body(body)
                .post(endpoint);

        // Аттач ответа в Allure
        attachToAllure("response_body", response.getBody().asString(), "application/json");

        log.debug("Получен ответ: status=" + response.getStatusCode() + ", result=" + response.jsonPath().getString("result"));

        return response;
    }

    /**
     * Удобный метод для отправки запроса авторизации ({@code LOGIN}).
     * <p>
     * Создаёт и отправляет {@link ActionRequest} с действием {@link UserAction#LOGIN}
     * и переданным токеном пользователя.
     *
     * @param token токен пользователя для аутентификации
     * @return {@link Response} с результатом выполнения запроса входа
     * @see UserAction#LOGIN
     * @see #sendAction(ActionRequest)
     */
    public Response sendLogin(String token) {
        ActionRequest request = ActionRequest.builder()
                .token(token)
                .action(UserAction.LOGIN)
                .build();
        return sendAction(request);
    }

    /**
     * Удобный метод для отправки запроса выполнения действия ({@code ACTION}).
     * <p>
     * Создаёт и отправляет {@link ActionRequest} с действием {@link UserAction#ACTION}
     * и переданным токеном пользователя.
     *
     * @param token токен авторизованного пользователя
     * @return {@link Response} с результатом выполнения запрошенного действия
     * @see UserAction#ACTION
     * @see #sendAction(ActionRequest)
     */
    public Response sendDoAction(String token) {
        ActionRequest request = ActionRequest.builder()
                .token(token)
                .action(UserAction.ACTION)
                .build();
        return sendAction(request);
    }

    /**
     * Удобный метод для отправки запроса завершения сессии ({@code LOGOUT}).
     * <p>
     * Создаёт и отправляет {@link ActionRequest} с действием {@link UserAction#LOGOUT}
     * и переданным токеном пользователя.
     *
     * @param token токен пользователя для завершения сессии
     * @return {@link Response} с результатом выполнения запроса выхода
     * @see UserAction#LOGOUT
     * @see #sendAction(ActionRequest)
     */
    public Response sendLogout(String token) {
        ActionRequest request = ActionRequest.builder()
                .token(token)
                .action(UserAction.LOGOUT)
                .build();
        return sendAction(request);
    }

    /**
     * Добавляет текстовое содержимое как вложение в отчёт Allure.
     * <p>
     * Метод безопасно обрабатывает исключения: при ошибке добавления вложения
     * записывает предупреждение в лог, но не прерывает выполнение теста.
     *
     * @param name    имя вложения для отображения в отчёте
     * @param content текстовое содержимое для добавления
     * @param type    MIME-тип содержимого (например, {@code "application/json"})
     * @apiNote Использует {@link #getExtension(String)} для определения расширения файла
     * @see Allure#addAttachment(String, String, String, String)
     */
    private void attachToAllure(String name, String content, String type) {
        try {
            Allure.addAttachment(name, type, content, getExtension(type));
        } catch (Exception e) {
            log.warn("Не удалось добавить аттач в Allure: "+ name, e);
        }
    }

    /**
     * Определяет расширение файла по MIME-типу для корректного отображения в Allure.
     *
     * @param mimeType MIME-тип содержимого
     * @return строковое расширение файла:
     *         <ul>
     *           <li>{@code "json"} — для {@code application/json}</li>
     *           <li>{@code "txt"} — для {@code text/plain} и типов по умолчанию</li>
     *           <li>{@code "html"} — для {@code text/html}</li>
     *         </ul>
     * @apiNote Метод использует switch-выражение для эффективного сопоставления
     */
    private String getExtension(String mimeType) {
        return switch (mimeType) {
            case "application/json" -> "json";
            case "text/plain" -> "txt";
            case "text/html" -> "html";
            default -> "txt";
        };
    }

    /**
     * Фабричный метод для создания экземпляра клиента с конфигурацией по умолчанию.
     * <p>
     * Автоматически настраивает {@link RequestSpecification} с параметрами:
     * <ul>
     *   <li>{@code baseUri} из {@link TestConfig#APP_BASE_URL}</li>
     *   <li>{@code Content-Type: application/x-www-form-urlencoded}</li>
     *   <li>{@code Accept: application/json}</li>
     * </ul>
     * <p>
     * Удобен для быстрого создания клиента в тестах без ручной настройки спецификации.
     *
     * @return новый экземпляр {@link AppApiClient} с готовой к использованию конфигурацией
     * @see io.restassured.RestAssured#given()
     * @see TestConfig#APP_BASE_URL
     */
    public static AppApiClient createDefault() {
        RequestSpecification spec = io.restassured.RestAssured.given()
                .baseUri(TestConfig.APP_BASE_URL)
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json");
        return new AppApiClient(spec);
    }
}