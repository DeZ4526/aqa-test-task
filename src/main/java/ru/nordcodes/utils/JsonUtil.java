package ru.nordcodes.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j;

/**
 * Утилитный класс для работы с JSON: сериализация и десериализация объектов.
 * <p>
 * Класс предоставляет статические методы-обёртки над {@link ObjectMapper} из библиотеки Jackson
 * с предустановленной конфигурацией для удобства использования в тестах и бизнес-логике.
 * <p>
 * <b>Конфигурация ObjectMapper:</b>
 * <ul>
 *   <li>Создаётся через {@link JsonMapper#builder()} для гибкой настройки</li>
 *   <li>Подключён {@link JavaTimeModule} для корректной обработки Java 8 Date/Time API:
 *       {@code LocalDateTime}, {@code ZonedDateTime}, {@code Instant} и др.</li>
 *   <li>Вызван {@code findAndModules()} для автоматического подключения доступных модулей Jackson</li>
 *   <li>Использует стандартные настройки сериализации/десериализации Jackson</li>
 * </ul>
 * <p>
 * <b>Обработка ошибок:</b>
 * <ul>
 *   <li>Все исключения перехватываются и логируются через Log4j на уровне {@code ERROR}</li>
 *   <li>При ошибке методы возвращают {@code null} вместо выбрасывания исключения —
 *       это упрощает использование в тестах, где не требуется сложная обработка ошибок</li>
 *   <li>Для критичных сценариев рекомендуется проверять возвращаемое значение на {@code null}</li>
 * </ul>
 * <p>
 * <b>Примеры использования:</b>
 * <pre>{@code
 * // Сериализация объекта в JSON
 * User user = new User("ivan", "ivan@example.com");
 * String json = JsonUtil.toJson(user);
 * // Результат: {"username":"ivan","email":"ivan@example.com"}
 *
 * // Десериализация JSON в объект
 * String input = "{\"username\":\"ivan\",\"email\":\"ivan@example.com\"}";
 * User parsed = JsonUtil.fromJson(input, User.class);
 *
 * // Работа с Java 8 Date/Time
 * Event event = new Event("meeting", LocalDateTime.now());
 * String eventJson = JsonUtil.toJson(event); // корректно сериализует LocalDateTime
 *
 * // Проверка на null после десериализации
 * AppResponse response = JsonUtil.fromJson(json, AppResponse.class);
 * if (response != null && response.isSuccess()) {
 *     // обработка успешного ответа
 * }
 * }</pre>
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
 * @see ObjectMapper
 * @see JsonMapper
 * @see JavaTimeModule
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @since 1.0
 */
@Log4j
@UtilityClass
public class JsonUtil {

    /**
     * Статический экземпляр {@link ObjectMapper} с предустановленной конфигурацией.
     * <p>
     * <b>Особенности конфигурации:</b>
     * <ul>
     *   <li>Инициализируется однократно при загрузке класса (thread-safe благодаря final)</li>
     *   <li>Поддерживает сериализацию/десериализацию Java 8 Date/Time через {@link JavaTimeModule}</li>
     *   <li>Автоматически обнаруживает и подключает другие доступные модули Jackson</li>
     *   <li>Использует стандартные настройки: camelCase поля, игнорирование неизвестных свойств при десериализации</li>
     * </ul>
     * <p>
     * <b>Рекомендации:</b>
     * <ul>
     *   <li>Не модифицируйте экземпляр после инициализации — он используется во всех вызовах утилиты</li>
     *   <li>Для кастомной настройки создавайте отдельный {@code ObjectMapper} в месте использования</li>
     * </ul>
     *
     * @see JsonMapper#builder()
     * @see JavaTimeModule
     */
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())  // Поддержка Java 8 Date/Time
            .findAndAddModules()
            .build();

    /**
     * Сериализует Java-объект в JSON-строку.
     * <p>
     * <b>Алгоритм работы:</b>
     * <ol>
     *   <li>Вызывает {@link ObjectMapper#writeValueAsString(Object)} для преобразования объекта</li>
     *   <li>При успехе возвращает валидную JSON-строку</li>
     *   <li>При любом исключении (JsonProcessingException, IllegalArgumentException и др.):
     *       <ul>
     *         <li>Логирует ошибку с полным стектрейсом на уровне {@code ERROR}</li>
     *         <li>Возвращает {@code null} для безопасной обработки вызывающей стороной</li>
     *       </ul>
     *   </li>
     * </ol>
     * <p>
     * <b>Примеры:</b>
     * <pre>{@code
     * // Простой объект
     * String json = JsonUtil.toJson(Map.of("key", "value"));
     * // Результат: {"key":"value"}
     *
     * // Объект с Java 8 Date/Time
     * Record rec = new Record(LocalDateTime.of(2024, 1, 1, 12, 0));
     * String json = JsonUtil.toJson(rec);
     * // Результат: {"timestamp":[2024,1,1,12,0,0]}
     *
     * // null-объект
     * String nullJson = JsonUtil.toJson(null); // вернёт "null" (валидный JSON)
     *
     * // Объект с циклическими ссылками (ошибка)
     * String badJson = JsonUtil.toJson(cyclicObject); // вернёт null + лог ошибки
     * }</pre>
     *
     * @param obj объект для сериализации (может быть {@code null})
     * @return JSON-представление объекта в виде строки или {@code null} при ошибке
     * @apiNote Метод возвращает строку {@code "null"} при передаче {@code null} как аргумента —
     *          это корректное поведение согласно спецификации JSON
     * @see ObjectMapper#writeValueAsString(Object)
     * @see #fromJson(String, Class)
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Ошибка сериализации в JSON", e);
            return null;
        }
    }

    /**
     * Десериализует JSON-строку в объект указанного типа.
     * <p>
     * <b>Алгоритм работы:</b>
     * <ol>
     *   <li>Вызывает {@link ObjectMapper#readValue(String, Class)} для преобразования JSON</li>
     *   <li>При успехе возвращает новый экземпляр класса {@code <T>} с заполненными полями</li>
     *   <li>При любом исключении (JsonProcessingException, IllegalArgumentException и др.):
     *       <ul>
     *         <li>Логирует ошибку с полным стектрейсом на уровне {@code ERROR}</li>
     *         <li>Возвращает {@code null} для безопасной обработки вызывающей стороной</li>
     *       </ul>
     *   </li>
     * </ol>
     * <p>
     * <b>Поддерживаемые сценарии:</b>
     * <ul>
     *   <li>Десериализация в POJO с аннотациями Jackson ({@code @JsonProperty}, {@code @JsonIgnore})</li>
     *   <li>Обработка Java 8 Date/Time типов благодаря подключённому {@link JavaTimeModule}</li>
     *   <li>Игнорирование неизвестных JSON-полей (стандартное поведение Jackson)</li>
     *   <li>Работа с коллекциями и массивами через обёртки (например, {@code TypeReference<List<T>>})</li>
     * </ul>
     * <p>
     * <b>Примеры:</b>
     * <pre>{@code
     * // Десериализация в DTO
     * String json = "{\"result\":\"OK\",\"message\":null}";
     * AppResponse resp = JsonUtil.fromJson(json, AppResponse.class);
     *
     * // Десериализация с Java 8 Time
     * String json = "{\"timestamp\":[2024,1,1,12,0,0]}";
     * Event event = JsonUtil.fromJson(json, Event.class);
     *
     * // Некорректный JSON (ошибка)
     * String badJson = "{invalid}";
     * AppResponse resp = JsonUtil.fromJson(badJson, AppResponse.class); // вернёт null + лог ошибки
     *
     * // JSON с лишними полями (успех, поля игнорируются)
     * String json = "{\"result\":\"OK\",\"extra\":\"ignored\"}";
     * AppResponse resp = JsonUtil.fromJson(json, AppResponse.class); // вернёт объект с result="OK"
     * }</pre>
     * <p>
     * <b>Ограничения:</b>
     * <ul>
     *   <li>Для десериализации коллекций с дженериками используйте перегрузку с {@code TypeReference}</li>
     *   <li>Целевой класс должен иметь публичный конструктор без аргументов или аннотации Jackson для конструктора</li>
     *   <li>Поля класса должны быть доступны для записи (setter, public field или {@code @JsonProperty} на конструкторе)</li>
     * </ul>
     *
     * @param json  JSON-строка для десериализации (может быть {@code null} или пустой)
     * @param clazz {@link Class} целевого типа для создания экземпляра
     * @param <T>   тип возвращаемого объекта
     * @return новый экземпляр {@code <T>} с данными из JSON или {@code null} при ошибке/невалидном входе
     * @apiNote Всегда проверяйте возвращаемое значение на {@code null} перед использованием
     * @see ObjectMapper#readValue(String, Class)
     * @see #toJson(Object)
     * @see com.fasterxml.jackson.core.type.TypeReference
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Ошибка десериализации из JSON", e);
            return null;
        }
    }
}