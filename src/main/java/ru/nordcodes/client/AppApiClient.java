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
 * Типобезопасный клиент для вызовов к тестируемому приложению.
 *
 * Эндпоинт: POST {app.base.url}{app.endpoint}
 * Headers: X-Api-Key, Content-Type, Accept
 * Body: token=${token}&action=${action}
 */
@Log4j
public class AppApiClient extends ApiClient {

    private final String baseUrl;
    private final String endpoint;
    private final String apiKey;

    public AppApiClient(RequestSpecification baseSpec) {
        super(baseSpec);
        this.baseUrl = TestConfig.APP_BASE_URL;
        this.endpoint = TestConfig.APP_ENDPOINT;
        this.apiKey = TestConfig.APP_API_KEY;
    }

    /**
     * Отправляет запрос с действием к приложению.
     * Логирует запрос/ответ в Allure.
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
     * Удобный метод для LOGIN-запроса.
     */
    public Response sendLogin(String token) {
        ActionRequest request = ActionRequest.builder()
                .token(token)
                .action(UserAction.LOGIN)
                .build();
        return sendAction(request);
    }

    /**
     * Удобный метод для ACTION-запроса.
     */
    public Response sendDoAction(String token) {
        ActionRequest request = ActionRequest.builder()
                .token(token)
                .action(UserAction.ACTION)
                .build();
        return sendAction(request);
    }

    /**
     * Удобный метод для LOGOUT-запроса.
     */
    public Response sendLogout(String token) {
        ActionRequest request = ActionRequest.builder()
                .token(token)
                .action(UserAction.LOGOUT)
                .build();
        return sendAction(request);
    }

    /**
     * Добавляет текст в Allure-отчёт как вложение.
     */
    private void attachToAllure(String name, String content, String type) {
        try {
            Allure.addAttachment(name, type, content, getExtension(type));
        } catch (Exception e) {
            log.warn("Не удалось добавить аттач в Allure: "+ name, e);
        }
    }

    private String getExtension(String mimeType) {
        return switch (mimeType) {
            case "application/json" -> "json";
            case "text/plain" -> "txt";
            case "text/html" -> "html";
            default -> "txt";
        };
    }

    /**
     * Factory method для создания клиента с дефолтной спецификацией.
     */
    public static AppApiClient createDefault() {
        RequestSpecification spec = io.restassured.RestAssured.given()
                .baseUri(TestConfig.APP_BASE_URL)
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json");
        return new AppApiClient(spec);
    }
}