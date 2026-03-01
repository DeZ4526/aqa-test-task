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

@Log4j
public abstract class BaseTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .port(TestConfig.MOCK_PORT)
                    .bindAddress("localhost")
                    .proxyPassThrough(false))
            .build();

    protected static MockSteps mockSteps;
    protected static AppApiClient appClient;
    protected static RequestSpecification defaultSpec;

    protected TestContext testContext;

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


    @AfterEach
    void resetAfterTest() {

        if (testContext != null) {
            testContext.clear();
        }

        log.debug("Тест завершён, контекст очищен");
    }

    protected void initTestContext() {
        this.testContext = new TestContext();
    }

    protected void addParameter(String name, String value, boolean sensitive) {
        Allure.parameter(name, sensitive ? "***" : value);
    }

    protected String maskToken(String token) {
        if (token == null || token.length() < 8) return "****";
        return token.substring(0, 8) + "..." + token.substring(token.length() - 4);
    }
}