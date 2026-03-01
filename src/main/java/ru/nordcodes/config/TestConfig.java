package ru.nordcodes.config;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Централизованное хранилище конфигурационных параметров для тестовой инфраструктуры.
 * <p>
 * Класс предоставляет типобезопасный доступ к настройкам тестов, загружаемым из файла
 * {@code application-test.properties} с поддержкой переопределения значений через:
 * <ul>
 *   <li>Системные свойства JVM ({@code -Dkey=value} при запуске)</li>
 *   <li>Maven properties ({@code <properties>} в pom.xml)</li>
 *   <li>Значения по умолчанию, указанные в коде</li>
 * </ul>
 * <p>
 * <b>Приоритет разрешения параметров:</b>
 * <ol>
 *   <li>{@code System.getProperty(key)} — наивысший приоритет</li>
 *   <li>{@code PROPERTIES.getProperty(key)} — значение из файла конфигурации</li>
 *   <li>{@code defaultValue} — значение по умолчанию, указанное в коде</li>
 * </ol>
 * <p>
 * <b>Пример использования:</b>
 * <pre>{@code
 * // Получение строкового значения с дефолтом
 * String baseUrl = TestConfig.get("app.base.url", "http://localhost:8080");
 *
 * // Получение целочисленного значения
 * int timeout = TestConfig.getInt("request.timeout.seconds", 30);
 *
 * // Использование предопределённых констант
 * String apiKey = TestConfig.APP_API_KEY;
 * int mockPort = TestConfig.MOCK_PORT;
 *
 * // Переопределение параметра при запуске тестов:
 * // mvn test -Dapp.base.url=https://staging.example.com
 * }</pre>
 * <p>
 * <b>Структура конфигурационных групп:</b>
 * <ul>
 *   <li><b>Приложение:</b> {@code app.base.url}, {@code app.api.key}, {@code app.endpoint}</li>
 *   <li><b>Mock-сервис:</b> {@code mock.base.url}, {@code mock.port}, эндпоинты аутентификации и действий</li>
 *   <li><b>Токены:</b> длина, набор символов, валидный токен для тестов</li>
 *   <li><b>Действия:</b> строковые значения для {@code LOGIN}, {@code ACTION}, {@code LOGOUT}</li>
 *   <li><b>Ответы:</b> ожидаемые значения результатов {@code OK}/{@code ERROR}</li>
 *   <li><b>Таймауты:</b> время ожидания запросов и ассертов</li>
 *   <li><b>Allure:</b> настройки отчётности и директория результатов</li>
 * </ul>
 * <p>
 * <b>Важно:</b> Класс аннотирован {@code @UtilityClass} (Lombok), что автоматически:
 * <ul>
 *   <li>Делает все члены {@code static}</li>
 *   <li>Делает конструктор приватным и бросающим {@code UnsupportedOperationException}</li>
 *   <li>Предотвращает создание экземпляров класса</li>
 * </ul>
 *
 * @author Карпов Даниил
 * @email karpov.k-r@yandex.ru
 * @telegram https://t.me/Dez4526
 * @see Properties
 * @see System#getProperty(String)
 * @since 1.0
 */
@Log4j
@UtilityClass
public class TestConfig {

    /**
     * Кэш загруженных свойств из файла конфигурации.
     * <p>
     * Инициализируется однократно при загрузке класса через {@link #loadProperties()}.
     * Не является {@code final}, так как может быть расширен в будущем для поддержки hot-reload.
     */
    private static final Properties PROPERTIES = loadProperties();

    /**
     * Загружает свойства из файла {@code application-test.properties} в classpath.
     * <p>
     * Метод выполняет следующие действия:
     * <ol>
     *   <li>Пытается найти ресурс через {@link ClassLoader#getResourceAsStream(String)}</li>
     *   <li>При успешном нахождении — загружает свойства через {@link Properties#load(InputStream)}</li>
     *   <li>При ошибке или отсутствии файла — логирует предупреждение и возвращает пустой объект</li>
     * </ol>
     * <p>
     * <b>Обработка ошибок:</b>
     * <ul>
     *   <li>Если файл не найден — логируется ошибка уровня {@code ERROR}, возвращается пустой {@code Properties}</li>
     *   <li>При {@code IOException} — исключение перехватывается, логируется стектрейс, возвращается частично загруженный объект</li>
     * </ul>
     *
     * @return экземпляр {@link Properties} с загруженными параметрами или пустой объект при ошибке
     * @apiNote Метод вызывается автоматически при инициализации класса, не требует явного вызова
     * @see #PROPERTIES
     */
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
     * Получает строковое значение конфигурационного параметра с поддержкой fallback-значения.
     * <p>
     * <b>Алгоритм разрешения значения:</b>
     * <ol>
     *   <li>Проверка {@code System.getProperty(key)} — позволяет переопределять параметры при запуске JVM</li>
     *   <li>Если не найдено — поиск в загруженных свойствах {@code PROPERTIES.getProperty(key, defaultValue)}</li>
     *   <li>Если не найдено — возврат переданного {@code defaultValue}</li>
     * </ol>
     * <p>
     * <b>Логирование:</b>
     * <ul>
     *   <li>{@code DEBUG} — при переопределении параметра через System property</li>
     *   <li>{@code TRACE} — при успешном получении значения из файла</li>
     * </ul>
     *
     * @param key          имя конфигурационного параметра (например, {@code "app.base.url"})
     * @param defaultValue значение по умолчанию, возвращаемое если параметр не найден
     * @return строковое значение параметра или {@code defaultValue}, если ключ отсутствует во всех источниках
     * @apiNote Используйте этот метод для параметров, где допустимо отсутствие значения
     * @see #get(String)
     * @see System#getProperty(String)
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
     * Получает строковое значение параметра без значения по умолчанию.
     * <p>
     * Удобный перегруженный метод для случаев, когда отсутствие параметра
     * должно интерпретироваться как {@code null}, а не как строка-заглушка.
     *
     * @param key имя конфигурационного параметра
     * @return значение параметра или {@code null}, если ключ не найден ни в System properties, ни в файле
     * @see #get(String, String)
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * Получает целочисленное значение конфигурационного параметра с обработкой ошибок парсинга.
     * <p>
     * Метод пытается преобразовать строковое значение в {@code int} через {@link Integer#parseInt(String)}.
     * При возникновении {@link NumberFormatException}:
     * <ul>
     *   <li>Логируется предупреждение с указанием проблемного значения</li>
     *   <li>Возвращается переданное {@code defaultValue}</li>
     * </ul>
     *
     * @param key          имя конфигурационного параметра
     * @param defaultValue значение по умолчанию, возвращаемое при ошибке парсинга или отсутствии ключа
     * @return целочисленное значение параметра или {@code defaultValue} при ошибке
     * @apiNote Используйте для таймаутов, портов, счётчиков и других числовых настроек
     * @see #get(String, String)
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
     * Получает логическое значение конфигурационного параметра.
     * <p>
     * Использует {@link Boolean#parseBoolean(String)}, который возвращает {@code true}
     * только если строковое значение (без учёта регистра) равно {@code "true"}.
     * Все остальные значения, включая {@code null}, интерпретируются как {@code false}.
     *
     * @param key          имя конфигурационного параметра
     * @param defaultValue значение по умолчанию, используемое если параметр не найден
     * @return {@code true} если значение параметра равно "true" (игнорируя регистр), иначе {@code false}
     * @apiNote Для явного контроля над false-значениями передавайте строковые "false"/"true" в файле конфигурации
     * @see Boolean#parseBoolean(String)
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    // ==================== Convenience getters ====================

    /**
     * Базовый URL тестируемого приложения.
     * <p>
     * Используется как {@code baseUri} для RestAssured и других HTTP-клиентов.
     * Значение по умолчанию: {@code "http://localhost:8080"}
     *
     * @see #get(String, String)
     */
    public static final String APP_BASE_URL = get("app.base.url", "http://localhost:8080");

    /**
     * API-ключ для аутентификации запросов к приложению.
     * <p>
     * Передаётся в заголовке {@code X-Api-Key}.
     * Значение по умолчанию: {@code "qazWSXedc"}
     *
     * @apiNote Не храните реальные ключи в коде — используйте переопределение через System properties
     */
    public static final String APP_API_KEY = get("app.api.key", "qazWSXedc");

    /**
     * Относительный путь основного эндпоинта приложения.
     * <p>
     * Значение по умолчанию: {@code "/endpoint"}
     *
     * @see #APP_BASE_URL
     */
    public static final String APP_ENDPOINT = get("app.endpoint", "/endpoint");

    /**
     * Базовый URL mock-сервиса для изоляции внешних зависимостей.
     * <p>
     * Значение по умолчанию: {@code "http://localhost:8888"}
     */
    public static final String MOCK_BASE_URL = get("mock.base.url", "http://localhost:8888");

    /**
     * Порт, на котором запускается WireMock-сервер для мокирования.
     * <p>
     * Значение по умолчанию: {@code 8888}
     *
     * @see #getInt(String, int)
     */
    public static final int MOCK_PORT = getInt("mock.port", 8888);

    /**
     * Эндпоинт mock-сервиса для аутентификации.
     * <p>
     * Значение по умолчанию: {@code "/auth"}
     */
    public static final String MOCK_AUTH_ENDPOINT = get("mock.auth.endpoint", "/auth");

    /**
     * Эндпоинт mock-сервиса для выполнения действий.
     * <p>
     * Значение по умолчанию: {@code "/doAction"}
     */
    public static final String MOCK_ACTION_ENDPOINT = get("mock.action.endpoint", "/doAction");

    /**
     * Ожидаемая длина генерируемых тестовых токенов.
     * <p>
     * Значение по умолчанию: {@code 32}
     *
     * @see #TOKEN_CHARSET
     */
    public static final int TOKEN_LENGTH = getInt("token.length", 32);

    /**
     * Набор символов для генерации тестовых токенов.
     * <p>
     * По умолчанию: заглавные латинские буквы и цифры {@code A-Z0-9}
     *
     * @see #TOKEN_LENGTH
     */
    public static final String TOKEN_CHARSET = get("token.charset", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

    /**
     * Предопределённый валидный токен для использования в позитивных тест-кейсах.
     * <p>
     * Значение по умолчанию: {@code "ABCDEFGHIJKLMNOP1234567890ABCDEF"}
     *
     * @apiNote Используйте для тестов, где не требуется реальная валидация токена
     */
    public static final String VALID_TOKEN = get("token.valid", "ABCDEFGHIJKLMNOP1234567890ABCDEF");

    /**
     * Строковое значение действия "авторизация" для формирования запросов.
     * <p>
     * Значение по умолчанию: {@code "LOGIN"}
     *
     * @see ru.nordcodes.dto.enums.UserAction
     */
    public static final String ACTION_LOGIN = get("action.login", "LOGIN");

    /**
     * Строковое значение действия "выполнить операцию" для формирования запросов.
     * <p>
     * Значение по умолчанию: {@code "ACTION"}
     */
    public static final String ACTION_DO = get("action.do", "ACTION");

    /**
     * Строковое значение действия "завершение сессии" для формирования запросов.
     * <p>
     * Значение по умолчанию: {@code "LOGOUT"}
     */
    public static final String ACTION_LOGOUT = get("action.logout", "LOGOUT");

    /**
     * Ожидаемое значение поля {@code result} в ответе при успешном выполнении запроса.
     * <p>
     * Значение по умолчанию: {@code "OK"}
     *
     * @see ru.nordcodes.dto.response.AppResponse
     */
    public static final String RESPONSE_OK = get("response.result.ok", "OK");

    /**
     * Ожидаемое значение поля {@code result} в ответе при ошибке выполнения запроса.
     * <p>
     * Значение по умолчанию: {@code "ERROR"}
     */
    public static final String RESPONSE_ERROR = get("response.result.error", "ERROR");

    /**
     * Таймаут ожидания ответа от сервера в секундах.
     * <p>
     * Используется при настройке HTTP-клиентов (RestAssured, WireMock).
     * Значение по умолчанию: {@code 30}
     */
    public static final int REQUEST_TIMEOUT_SECONDS = getInt("request.timeout.seconds", 30);

    /**
     * Таймаут ожидания выполнения ассертов в миллисекундах.
     * <p>
     * Может использоваться для retry-логики или ожидания асинхронных операций.
     * Значение по умолчанию: {@code 5000} (5 секунд)
     */
    public static final int ASSERTION_TIMEOUT_MILLIS = getInt("assertion.timeout.millis", 5000);

    /**
     * Владелец тестов для отображения в отчётах Allure.
     * <p>
     * Значение по умолчанию: {@code "AQA Engineer"}
     *
     * @see io.qameta.allure.Epic
     * @see io.qameta.allure.Owner
     */
    public static final String ALLURE_OWNER = get("allure.owner", "AQA Engineer");

    /**
     * Директория для сохранения результатов Allure-отчётности.
     * <p>
     * Используется при настройке Maven/Gradle плагинов для генерации отчётов.
     * Значение по умолчанию: {@code "target/allure-results"}
     *
     * @see <a href="https://docs.qameta.io/allure/">Allure Documentation</a>
     */
    public static final String ALLURE_RESULTS_DIR = get("allure.results.directory", "target/allure-results");
}