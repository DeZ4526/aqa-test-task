package ru.nordcodes.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ru.nordcodes.dto.request.ActionRequest;

/**
 * Абстрактный базовый класс для API-клиентов.
 * Определяет контракт для отправки запросов.
 */
public abstract class ApiClient {

    protected final RequestSpecification baseSpec;

    protected ApiClient(RequestSpecification baseSpec) {
        this.baseSpec = baseSpec;
    }

    /**
     * Отправляет запрос с действием к приложению.
     * @param request DTO запроса
     * @return Response от сервера
     */
    public abstract Response sendAction(ActionRequest request);

    /**
     * Отправляет POST-запрос с form-urlencoded телом.
     */
    protected Response postForm(String endpoint, String body) {
        return baseSpec
                .body(body)
                .post(endpoint);
    }

    /**
     * Добавляет стандартные заголовки к спеке.
     */
    protected RequestSpecification withDefaultHeaders(RequestSpecification spec) {
        return spec
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json");
    }
}