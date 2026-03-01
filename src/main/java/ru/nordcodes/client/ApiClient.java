package ru.nordcodes.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ru.nordcodes.dto.request.ActionRequest;

/**
 * Абстрактный базовый класс для реализации API-клиентов.
 * <p>
 * Предоставляет общую инфраструктуру для отправки HTTP-запросов к тестируемому приложению
 * и определяет контракт для конкретных реализаций клиентов.
 * <p>
 * <b>Основные возможности:</b>
 * <ul>
 *   <li>Хранение базовой {@link RequestSpecification} с предустановленными настройками</li>
 *   <li>Защита от повторной инициализации через {@code final} поле и конструктор</li>
 *   <li>Утилитные методы для отправки POST-запросов с {@code application/x-www-form-urlencoded}</li>
 *   <li>Метод для добавления стандартных заголовков к спецификации запроса</li>
 *   <li>Абстрактный метод {@link #sendAction(ActionRequest)} для реализации конкретного поведения</li>
 * </ul>
 * <p>
 * <b>Пример реализации:</b>
 * <pre>{@code
 * public class AppApiClient extends ApiClient {
 *
 *     public AppApiClient(RequestSpecification baseSpec) {
 *         super(baseSpec);
 *     }
 *
 *     @Override
 *     public Response sendAction(ActionRequest request) {
 *         String body = "action=" + request.getAction() + "&value=" + request.getValue();
 *         return postForm("/api/action", body);
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Пример использования:</b>
 * <pre>{@code
 * RequestSpecification spec = new RequestSpecBuilder()
 *     .setBaseUri("https://api.example.com")
 *     .build();
 *
 * ApiClient client = new AppApiClient(spec);
 * ActionRequest request = new ActionRequest("login", "user123");
 * Response response = client.sendAction(request);
 * }</pre>
 *
 * @author Карпов Даниил
 * @email karpov.k-r@yandex.ru
 * @telegram https://t.me/Dez4526
 * @see RequestSpecification
 * @see Response
 * @see ActionRequest
 * @since 1.0
 */
public abstract class ApiClient {

    /**
     * Базовая спецификация запросов, содержащая общие настройки:
     * базовый URI, заголовки по умолчанию, параметры аутентификации и т.д.
     * <p>
     * Объявлено как {@code final} для гарантии неизменности после инициализации
     * и предотвращения случайной модификации в подклассах.
     *
     * @see RequestSpecification
     */
    protected final RequestSpecification baseSpec;

    /**
     * Конструктор для инициализации API-клиента с базовой спецификацией.
     * <p>
     * Защищённый модификатор доступа ({@code protected}) гарантирует,
     * что класс может быть использован только через наследование.
     *
     * @param baseSpec предустановленная спецификация запросов с базовыми настройками
     * @throws IllegalArgumentException если {@code baseSpec} равен {@code null}
     * @apiNote Рекомендуется валидировать входные параметры в конструкторах подклассов
     */
    protected ApiClient(RequestSpecification baseSpec) {
        this.baseSpec = baseSpec;
    }

    /**
     * Отправляет запрос с действием к тестируемому приложению.
     * <p>
     * Абстрактный метод, который должен быть реализован в конкретном клиенте
     * для определения логики формирования и отправки запроса.
     *
     * @param request DTO, содержащее параметры действия для отправки на сервер
     * @return {@link Response} с данными ответа от сервера для последующей валидации
     * @apiNote Реализация должна обрабатывать возможные исключения и логировать ошибки
     * @see ActionRequest
     * @see Response
     */
    public abstract Response sendAction(ActionRequest request);

    /**
     * Отправляет POST-запрос с телом в формате {@code application/x-www-form-urlencoded}.
     * <p>
     * Утилитный метод для упрощения отправки стандартных form-запросов.
     * Использует базовую спецификацию {@link #baseSpec} для применения общих настроек.
     *
     * @param endpoint относительный путь эндпоинта (например, {@code "/api/login"})
     * @param body     строка тела запроса в формате {@code key1=value1&key2=value2}
     * @return {@link Response} с результатом выполнения запроса
     * @apiNote Метод не добавляет заголовки автоматически — убедитесь, что они заданы в {@code baseSpec}
     * @see #withDefaultHeaders(RequestSpecification)
     */
    protected Response postForm(String endpoint, String body) {
        return baseSpec
                .body(body)
                .post(endpoint);
    }

    /**
     * Добавляет стандартные заголовки к переданной спецификации запроса.
     * <p>
     * Устанавливает следующие заголовки по умолчанию:
     * <ul>
     *   <li>{@code Content-Type: application/x-www-form-urlencoded} — для отправки form-данных</li>
     *   <li>{@code Accept: application/json} — для ожидания JSON-ответа от сервера</li>
     * </ul>
     * <p>
     * Метод возвращает ту же спецификацию (fluid interface),
     * что позволяет использовать его в цепочке вызовов.
     *
     * @param spec спецификация запроса, к которой необходимо добавить заголовки
     * @return та же {@link RequestSpecification} с добавленными заголовками
     * @apiNote Если заголовки уже установлены, они будут перезаписаны
     * @see RequestSpecification(String, String)
     */
    protected RequestSpecification withDefaultHeaders(RequestSpecification spec) {
        return spec
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json");
    }
}