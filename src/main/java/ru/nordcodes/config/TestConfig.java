package ru.nordcodes.config;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Централизованное хранилище тестовых параметров.
 * Читает значения из application-test.properties с возможностью переопределения через System properties.
 */
@Log4j
@UtilityClass
public class TestConfig {

    private static final Properties PROPERTIES = loadProperties();

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = TestConfig.class.getClassLoader()
                .getResourceAsStream("application-test.properties")) {

            if (is == null) {
                log.error("Файл application-test.properties не найден в classpath!");
                return props;
            }

            props.load(is);
            log.debug("Загружено "+props.size()+" параметров из application-test.properties");

        } catch (IOException e) {
            log.error("Ошибка при чтении application-test.properties", e);
        }
        return props;
    }

    /**
     * Получает строковое значение параметра.
     * Приоритет: System property > Maven property > значение из файла
     */
    public static String get(String key, String defaultValue) {
        // 1. Проверяем System property (переопределение через -D)
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            log.debug("Переопределён параметр " + key + " через System property: " + systemValue);
            return systemValue;
        }

        // 2. Возвращаем из файла (с дефолтом)
        String fileValue = PROPERTIES.getProperty(key, defaultValue);
        log.trace("Параметр " + key + " = " + fileValue);
        return fileValue;
    }

    /**
     * Получает значение без дефолта (вернёт null если не найдено)
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * Получает целочисленное значение
     */
    public static int getInt(String key, int defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Не удалось распарсить int для ключа " + key + ": '"+value+"', используем default: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Получает boolean значение
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    // ==================== Convenience getters ====================

    // Приложение
    public static final String APP_BASE_URL = get("app.base.url", "http://localhost:8080");
    public static final String APP_API_KEY = get("app.api.key", "qazWSXedc");
    public static final String APP_ENDPOINT = get("app.endpoint", "/endpoint");

    // Mock сервис
    public static final String MOCK_BASE_URL = get("mock.base.url", "http://localhost:8888");
    public static final int MOCK_PORT = getInt("mock.port", 8888);
    public static final String MOCK_AUTH_ENDPOINT = get("mock.auth.endpoint", "/auth");
    public static final String MOCK_ACTION_ENDPOINT = get("mock.action.endpoint", "/doAction");

    // Токены
    public static final int TOKEN_LENGTH = getInt("token.length", 32);
    public static final String TOKEN_CHARSET = get("token.charset", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
    public static final String VALID_TOKEN = get("token.valid", "ABCDEFGHIJKLMNOP1234567890ABCDEF");

    // Действия
    public static final String ACTION_LOGIN = get("action.login", "LOGIN");
    public static final String ACTION_DO = get("action.do", "ACTION");
    public static final String ACTION_LOGOUT = get("action.logout", "LOGOUT");

    // Ответы
    public static final String RESPONSE_OK = get("response.result.ok", "OK");
    public static final String RESPONSE_ERROR = get("response.result.error", "ERROR");

    // Таймауты
    public static final int REQUEST_TIMEOUT_SECONDS = getInt("request.timeout.seconds", 30);
    public static final int ASSERTION_TIMEOUT_MILLIS = getInt("assertion.timeout.millis", 5000);

    // Allure
    public static final String ALLURE_OWNER = get("allure.owner", "AQA Engineer");
    public static final String ALLURE_RESULTS_DIR = get("allure.results.directory", "target/allure-results");
}
