package ru.nordcodes.tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.nordcodes.base.BaseTest;
import ru.nordcodes.dto.response.AppResponse;
import ru.nordcodes.steps.ValidationSteps;
import ru.nordcodes.utils.TokenGenerator;
import ru.nordcodes.utils.enums.InvalidType;

/**
 * Тесты авторизации (LOGIN action).
 *
 * Epic: Пользовательские сессии
 * Feature: Аутентификация через внешний сервис
 */
@Epic("Пользовательские сессии")
@Feature("Аутентификация")
@DisplayName("Авторизация пользователя: LOGIN")
class LoginTest extends BaseTest {

    private ValidationSteps validation;

    @BeforeEach
    void setUp() {
        initTestContext();
        validation = ValidationSteps.create();
    }

    @Test
    @Story("Успешная авторизация с валидным токеном")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("TC-AUTH-001")
    @Owner("Даниил Карпов")
    @Description("Пользователь отправляет LOGIN с валидным токеном → " +
            "внешний сервис возвращает 200 → токен сохраняется → ответ OK")
    @DisplayName("LOGIN: валидный токен → успешная авторизация")
    void login_withValidToken_success() {
        String token = TokenGenerator.generateValid();
        addParameter("test_token", token, true);

        mockSteps.stubAuthSuccess(token);

        Response response = appClient.sendLogin(token);

        validation.verifyHttpStatus(200, response.getStatusCode());

        AppResponse appResponse = response.as(AppResponse.class);
        testContext.setLastResponse(appResponse);

        validation.verifySuccessResponse(appResponse);
        mockSteps.verifyAuthCalled(token);

        testContext.markLoggedIn(token);
    }

    @Test
    @Story("Авторизация с ошибкой внешнего сервиса")
    @Severity(SeverityLevel.NORMAL)
    @TmsLink("TC-AUTH-005")
    @Description("Внешний сервис возвращает 500 → приложение возвращает ERROR")
    @DisplayName("LOGIN: внешний сервис вернул 500 → ошибка авторизации")
    void login_externalServiceError_returnsError() {
        String token = TokenGenerator.generateValid();

        mockSteps.stubAuthError(token, 500);

        Response response = appClient.sendLogin(token);

        validation.verifyHttpStatus(200, response.getStatusCode());

        AppResponse appResponse = response.as(AppResponse.class);
        validation.verifyErrorResponse(appResponse, "external");

        mockSteps.verifyAuthCalled(token);
    }

    @Test
    @Story("Авторизация с невалидным токеном")
    @Severity(SeverityLevel.NORMAL)
    @TmsLink("TC-AUTH-010")
    @Description("Токен не соответствует формату → приложение возвращает ERROR без вызова внешнего сервиса")
    @DisplayName("LOGIN: невалидный токен → валидация не прошла")
    void login_withInvalidToken_validationError() {

        String invalidToken = TokenGenerator.generateInvalid(InvalidType.TOO_SHORT);


        Response response = appClient.sendLogin(invalidToken);


        validation.verifyHttpStatus(200, response.getStatusCode());

        AppResponse appResponse = response.as(AppResponse.class);
        validation.verifyAnyErrorResponse(appResponse);

        mockSteps.verifyAuthNotCalled();
        validation.verifyExternalServiceNotCalled();
    }

    @Test
    @Story("Параметризованная валидация формата токена")
    @Severity(SeverityLevel.MINOR)
    @TmsLink("TC-AUTH-011")
    @DisplayName("LOGIN: проверка разных типов невалидных токенов")
    void login_withVariousInvalidTokens() {
        for (InvalidType invalidType : InvalidType.values()) {
            if (invalidType == InvalidType.NULL) continue;

            String invalidToken = TokenGenerator.generateInvalid(invalidType);

            Response response = appClient.sendLogin(invalidToken);
            AppResponse appResponse = response.as(AppResponse.class);

            io.qameta.allure.Allure.step(
                    "Токен типа: " + invalidType.getDescription(),
                    () -> {
                        validation.verifyAnyErrorResponse(appResponse);
                        mockSteps.verifyAuthNotCalled();
                    }
            );
        }
    }
}
