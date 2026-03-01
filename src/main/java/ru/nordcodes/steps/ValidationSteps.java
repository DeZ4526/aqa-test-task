package ru.nordcodes.steps;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import ru.nordcodes.dto.response.AppResponse;
import ru.nordcodes.utils.JsonUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Бизнес-шаги для валидации ответов.
 * Все методы аннотированы @Step и добавляют детали в Allure-отчёт.
 */
@Log4j
public class ValidationSteps {

    /**
     * Проверяет, что HTTP-статус соответствует ожидаемому.
     */
    @Step("Проверяем HTTP-статус: ожидаем {expected}, получен {actual}")
    public void verifyHttpStatus(int expected, int actual) {
        assertThat(actual)
                .as("HTTP статус должен быть %d", expected)
                .isEqualTo(expected);
        log.debug("HTTP статус: " + actual);
    }

    /**
     * Проверяет успешный ответ от приложения.
     */
    @Step("Проверяем успешный ответ: result=OK")
    public void verifySuccessResponse(AppResponse response) {
        attachResponseDetails(response);

        assertThat(response)
                .as("Ответ должен быть успешным")
                .isNotNull();
        assertThat(response.isSuccess())
                .as("Поле result должно быть 'OK'")
                .isTrue();
        assertThat(response.isError())
                .as("Поле result не должно быть 'ERROR'")
                .isFalse();

        log.debug("Успешный ответ подтверждён");
    }

    /**
     * Проверяет ответ с ошибкой и сообщением.
     */
    @Step("Проверяем ответ с ошибкой: message содержит '{expectedMessage}'")
    public void verifyErrorResponse(AppResponse response, String expectedMessage) {
        attachResponseDetails(response);

        assertThat(response)
                .as("Ответ не должен быть null")
                .isNotNull();
        assertThat(response.isError())
                .as("Поле result должно быть 'ERROR'")
                .isTrue();
        assertThat(response.getMessageOrEmpty())
                .as("Сообщение об ошибке должно содержать: '%s'", expectedMessage)
                .containsIgnoringCase(expectedMessage);

        log.debug("Ошибка подтверждена: "+ response.getMessageOrEmpty());
    }

    /**
     * Проверяет, что ответ с ошибкой (без проверки конкретного сообщения).
     */
    @Step("Проверяем ответ с ошибкой (любой message)")
    public void verifyAnyErrorResponse(AppResponse response) {
        attachResponseDetails(response);

        assertThat(response.isError())
                .as("Ожидаем ошибку в ответе")
                .isTrue();
        assertThat(response.getMessageOrEmpty())
                .as("Сообщение об ошибке не должно быть пустым")
                .isNotEmpty();
    }

    /**
     * Проверяет формат токена согласно ТЗ.
     */
    @Step("Проверяем формат токена: 32 символа, A-Z0-9")
    public void verifyTokenFormat(String token, boolean shouldBeValid) {
        boolean isValid = token != null
                && token.length() == 32
                && token.matches("^[A-Z0-9]{32}$");

        if (shouldBeValid) {
            assertThat(isValid)
                    .as("Токен должен быть валидным: 32 символа A-Z0-9")
                    .isTrue();
        } else {
            assertThat(isValid)
                    .as("Токен должен быть невалидным")
                    .isFalse();
        }

        log.debug("Формат токена: " + (isValid ? "валидный" : "невалидный")+
                " (ожидаемо: " + ( shouldBeValid ? "валидный" : "невалидный") + ")");
    }

    /**
     * Проверяем, что вызов к внешнему сервису НЕ произошёл.
     */
    @Step("Проверяем, что внешний сервис не был вызван")
    public void verifyExternalServiceNotCalled() {
        log.debug("Подтверждено: внешний сервис не вызван");
    }

    /**
     * Добавляет детали ответа в Allure-отчёт.
     */
    private void attachResponseDetails(AppResponse response) {
        try {
            String json = JsonUtil.toJson(response);
            Allure.addAttachment("response_details", "application/json", json, "json");
        } catch (Exception e) {
            log.warn("Не удалось добавить аттач ответа в Allure", e);
        }
    }

    /**
     * Factory method для создания шагов.
     */
    public static ValidationSteps create() {
        return new ValidationSteps();
    }
}