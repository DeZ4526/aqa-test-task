package ru.nordcodes.steps;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.qameta.allure.Step;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import ru.nordcodes.config.TestConfig;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Бизнес-шаги для настройки и верификации WireMock.
 * Все методы аннотированы @Step для отображения в Allure-отчёте.
 */
@Log4j
@RequiredArgsConstructor
public class MockSteps {
    private final WireMockExtension wireMock;
    private final String mockBaseUrl;

    public MockSteps(WireMockExtension wireMock) {
        this(wireMock, TestConfig.MOCK_BASE_URL);
    }


    @Step("Мокаем успешный ответ от /auth для токена {token}")
    public void stubAuthSuccess(String token) {
        log.debug("Mock: /auth → 200 OK для токена " + maskToken(token));

        wireMock.stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
                .withRequestBody(equalTo("token=" + token))
                .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"authenticated\"}")));
    }

    @Step("Мокаем ошибку {statusCode} от /auth для токена {token}")
    public void stubAuthError(String token, int statusCode) {
        log.debug("Mock: /auth → "+statusCode+" для токена " +maskToken(token) );

        wireMock.stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
                .withRequestBody(equalTo("token=" + token))
                .willReturn(status(statusCode)
                        .withBody("{\"error\":\"external_failure\"}")));
    }

    @Step("Мокаем успешный ответ от /doAction для токена {token}")
    public void stubDoActionSuccess(String token) {
        log.debug("Mock: /doAction → 200 OK для токена " + maskToken(token));

        wireMock.stubFor(post(urlEqualTo(TestConfig.MOCK_ACTION_ENDPOINT))
                .withRequestBody(equalTo("token=" + token))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\":\"done\"}")));
    }

    @Step("Мокаем задержку {delayMs}мс для /doAction")
    public void stubDoActionWithDelay(String token, int delayMs) {
        log.debug("Mock: /doAction → задержка " + delayMs + " мс.");

        wireMock.stubFor(post(urlEqualTo(TestConfig.MOCK_ACTION_ENDPOINT))
                .withRequestBody(equalTo("token=" + token))
                .willReturn(ok()
                        .withFixedDelay(delayMs)
                        .withBody("{\"result\":\"delayed\"}")));
    }

    @Step("Сбрасываем все моки WireMock")
    public void resetStubs() {
        wireMock.resetAll();
        log.debug("WireMock сброшен");
    }


    @Step("Проверяем, что /auth был вызван с токеном {expectedToken}")
    public void verifyAuthCalled(String expectedToken) {
        wireMock.verify(postRequestedFor(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
                .withRequestBody(equalTo("token=" + expectedToken)));
        log.debug("Верификация: /auth вызван с ожидаемым токеном");
    }

    @Step("Проверяем, что /auth НЕ был вызван")
    public void verifyAuthNotCalled() {
        wireMock.verify(0, postRequestedFor(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT)));
        log.debug("Верификация: /auth не вызван");
    }

    @Step("Проверяем, что /doAction был вызван с токеном {expectedToken}")
    public void verifyDoActionCalled(String expectedToken) {
        wireMock.verify(postRequestedFor(urlEqualTo(TestConfig.MOCK_ACTION_ENDPOINT))
                .withRequestBody(equalTo("token=" + expectedToken)));
        log.debug("Верификация: /doAction вызван с ожидаемым токеном");
    }

    @Step("Проверяем, что /doAction НЕ был вызван")
    public void verifyDoActionNotCalled() {
        wireMock.verify(0, postRequestedFor(urlEqualTo(TestConfig.MOCK_ACTION_ENDPOINT)));
        log.debug("Верификация: /doAction не вызван");
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) return "****";
        return token.substring(0, 8) + "...";
    }

    public String getMockUrl(String endpoint) {
        return mockBaseUrl + endpoint;
    }
}