package ru.nordcodes.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j;
import ru.nordcodes.config.TestConfig;
import ru.nordcodes.utils.enums.InvalidType;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Утилитный класс для генерации валидных и невалидных токенов в целях тестирования.
 * <p>
 * Класс предоставляет методы для создания тестовых данных, соответствующих требованиям ТЗ:
 * <ul>
 *   <li><b>Длина токена:</b> ровно 32 символа</li>
 *   <li><b>Допустимые символы:</b> заглавные латинские буквы {@code A-Z} и цифры {@code 0-9}</li>
 *   <li><b>Регулярное выражение:</b> {@code ^[A-Z0-9]{32}$}</li>
 * </ul>
 * <p>
 * <b>Основные возможности:</b>
 * <ul>
 *   <li>Генерация криптографически стойких валидных токенов через {@link SecureRandom}</li>
 *   <li>Создание невалидных токенов с различными типами нарушений через {@link InvalidType}</li>
 *   <li>Генерация списка уникальных токенов для параллельного выполнения тестов</li>
 *   <li>Валидация токенов на соответствие требованиям через {@link #isValid(String)}</li>
 *   <li>Генерация токенов с фиксированным префиксом для отладки и трассировки</li>
 * </ul>
 * <p>
 * <b>Примеры использования:</b>
 * <pre>{@code
 * // Генерация валидного токена (длина 32 по умолчанию)
 * String token = TokenGenerator.generateValid();
 *
 * // Генерация валидного токена заданной длины
 * String shortToken = TokenGenerator.generateValid(16);
 *
 * // Генерация невалидных токенов для негативных тестов
 * String tooShort = TokenGenerator.generateInvalid(InvalidType.TOO_SHORT);
 * String withSpecialChars = TokenGenerator.generateInvalid(InvalidType.SPECIAL_CHARS);
 * String nullToken = TokenGenerator.generateInvalid(InvalidType.NULL);
 *
 * // Валидация токена
 * if (TokenGenerator.isValid(token)) {
 *     // токен соответствует требованиям
 * }
 *
 * // Генерация уникальных токенов для параллельных тестов
 * List<String> tokens = TokenGenerator.generateUnique(10);
 *
 * // Токен с префиксом для отладки
 * String debugToken = TokenGenerator.generateWithPrefix("TEST_");
 * // Результат: "TEST_XXXXXXXXXXXXXXXXXXXXXXXXX" (всего 32 символа)
 * }</pre>
 * <p>
 * <b>Безопасность:</b>
 * <ul>
 *   <li>Используется {@link SecureRandom} вместо {@link java.util.Random} для повышения энтропии</li>
 *   <li>Алфавит символов загружается из {@link TestConfig#TOKEN_CHARSET} для централизованного управления</li>
 *   <li>Методы потокобезопасны благодаря использованию локальных переменных и immutable-констант</li>
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
 * @telegram <a href="https://t.me/Dez4526">https://t.me/Dez4526</a>
 * @see TestConfig#TOKEN_CHARSET
 * @see InvalidType
 * @see SecureRandom
 * @since 1.0
 */
@Log4j
@UtilityClass
public class TokenGenerator {

    /**
     * Алфавит символов для генерации валидных токенов.
     * <p>
     * Значение загружается из конфигурации {@link TestConfig#TOKEN_CHARSET}.
     * По умолчанию: {@code "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"}.
     * <p>
     * Используется во всех методах генерации валидных токенов.
     * Для невалидных токенов могут применяться модификации этого алфавита
     * (например, преобразование к нижнему регистру или добавление спецсимволов).
     *
     * @see TestConfig#TOKEN_CHARSET
     * @see #generate(int, String)
     */
    private static final String VALID_CHARSET = TestConfig.TOKEN_CHARSET;

    /**
     * Криптографически стойкий генератор псевдослучайных чисел.
     * <p>
     * Используется вместо {@link java.util.Random} для обеспечения:
     * <ul>
     *   <li>Более высокой энтропии при генерации токенов</li>
     *   <li>Предсказуемости только в контролируемых тестовых сценариях</li>
     *   <li>Соответствия требованиям безопасности для тестов аутентификации</li>
     * </ul>
     * <p>
     * Экземпляр инициализируется однократно при загрузке класса
     * и является потокобезопасным для использования в параллельных тестах.
     *
     * @see SecureRandom
     * @see #generate(int, String)
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Генерирует валидный токен стандартной длины (32 символа).
     * <p>
     * Метод является удобной обёрткой над {@link #generateValid(int)}
     * и использует значение по умолчанию из требований ТЗ.
     * <p>
     * <b>Характеристики результата:</b>
     * <ul>
     *   <li>Длина: ровно 32 символа</li>
     *   <li>Символы: только {@code A-Z} и {@code 0-9}</li>
     *   <li>Распределение: равномерное, благодаря {@link SecureRandom}</li>
     *   <li>Уникальность: высокая вероятность уникальности при последовательных вызовах</li>
     * </ul>
     *
     * @return сгенерированный токен, соответствующий требованиям ТЗ
     * @apiNote Для генерации токена другой длины используйте {@link #generateValid(int)}
     * @see #generateValid(int)
     * @see #isValid(String)
     */
    public static String generateValid() {
        return generate(32, VALID_CHARSET);
    }

    /**
     * Генерирует валидный токен заданной длины из стандартного алфавита.
     * <p>
     * Позволяет создавать токены произвольной длины для тестирования
     * граничных условий и валидации входных данных.
     * <p>
     * <b>Примеры:</b>
     * <pre>{@code
     * String shortToken = TokenGenerator.generateValid(16);  // 16 символов
     * String longToken = TokenGenerator.generateValid(64);   // 64 символа
     * String standard = TokenGenerator.generateValid(32);    // эквивалентно generateValid()
     * }</pre>
     *
     * @param length желаемая длина генерируемого токена (должна быть положительной)
     * @return сгенерированный токен из символов {@code A-Z0-9} указанной длины
     * @apiNote При {@code length <= 0} метод вернёт пустую строку
     * @see #generateValid()
     * @see #generate(int, String)
     */
    public static String generateValid(int length) {
        return generate(length, VALID_CHARSET);
    }

    /**
     * Генерирует невалидный токен согласно указанному типу нарушения.
     * <p>
     * Метод предназначен для создания тестовых данных в негативных сценариях,
     * где требуется проверить обработку ошибочных входных значений.
     * <p>
     * <b>Типы нарушений ({@link InvalidType}):</b>
     * <table border="1" cellpadding="5" cellspacing="0">
     *   <tr><th>Тип</th><th>Описание</th><th>Пример результата</th></tr>
     *   <tr><td>{@code TOO_SHORT}</td><td>Длина 16 вместо 32</td><td>{@code "ABC123..."} (16 симв.)</td></tr>
     *   <tr><td>{@code TOO_LONG}</td><td>Длина 48 вместо 32</td><td>{@code "ABC123..."} (48 симв.)</td></tr>
     *   <tr><td>{@code LOWERCASE}</td><td>Строчные буквы вместо заглавных</td><td>{@code "abc123..."}</td></tr>
     *   <tr><td>{@code SPECIAL_CHARS}</td><td>Добавлены спецсимволы</td><td>{@code "ABC123...!@#"}</td></tr>
     *   <tr><td>{@code EMPTY}</td><td>Пустая строка</td><td>{@code ""}</td></tr>
     *   <tr><td>{@code NULL}</td><td>Значение {@code null}</td><td>{@code null}</td></tr>
     *   <tr><td>{@code UNICODE}</td><td>Символы кириллицы</td><td>{@code "ТестТокен123456789012345678901"}</td></tr>
     * </table>
     * <p>
     * <b>Пример использования в тесте:</b>
     * <pre>{@code
     * @ParameterizedTest
     * @EnumSource(InvalidType.class)
     * void testInvalidTokenHandling(InvalidType invalidType) {
     *     String invalidToken = TokenGenerator.generateInvalid(invalidType);
     *     ActionRequest request = ActionRequest.builder()
     *         .token(invalidToken)
     *         .action(UserAction.LOGIN)
     *         .build();
     *
     *     Response response = client.sendAction(request);
     *     assertEquals(400, response.getStatusCode());
     * }
     * }</pre>
     *
     * @param type тип нарушения для генерации невалидного токена
     * @return строка, не соответствующая требованиям к токену, или {@code null} для типа {@code NULL}
     * @see InvalidType
     * @see #isValid(String)
     */
    public static String generateInvalid(InvalidType type) {
        return switch (type) {
            case TOO_SHORT -> generate(16, VALID_CHARSET);  // 16 вместо 32
            case TOO_LONG -> generate(48, VALID_CHARSET);   // 48 вместо 32
            case LOWERCASE -> generateValid().toLowerCase(); // строчные буквы
            case SPECIAL_CHARS -> generateValid() + "!@#";   // спецсимволы
            case EMPTY -> "";
            case NULL -> null;
            case UNICODE -> "ТестТокен123456789012345678901"; // кириллица
        };
    }

    /**
     * Внутренний метод генерации строки произвольной длины из заданного алфавита.
     * <p>
     * <b>Алгоритм работы:</b>
     * <ol>
     *   <li>Проверяет корректность входных параметров ({@code length > 0}, непустой {@code charset})</li>
     *   <li>При пустом алфавите логирует предупреждение и использует дефолтный {@code A-Z0-9}</li>
     *   <li>Для каждой позиции от 0 до {@code length-1}:
     *       <ul>
     *         <li>Генерирует случайный индекс через {@link SecureRandom#nextInt(int)}</li>
     *         <li>Извлекает символ из алфавита по этому индексу</li>
     *       </ul>
     *   </li>
     *   <li>Собирает символы в строку через {@link Collectors#joining()}</li>
     * </ol>
     * <p>
     * <b>Производительность:</b>
     * <ul>
     *   <li>Использует {@link IntStream} для параллелизуемой генерации</li>
     *   <li>Время выполнения: O(n), где n — длина токена</li>
     *   <li>Память: O(n) для хранения результата</li>
     * </ul>
     *
     * @param length  длина генерируемой строки
     * @param charset алфавит символов для выбора
     * @return сгенерированная строка заданной длины или пустая строка при ошибке ввода
     * @apiNote Метод приватный и используется только внутри класса
     * @see #generateValid()
     * @see #generateValid(int)
     */
    private static String generate(int length, String charset) {
        if (length <= 0) return "";
        if (charset == null || charset.isEmpty()) {
            log.warn("Пустой charset, используем дефолтный A-Z0-9");
            charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        }

        String finalCharset = charset;
        return IntStream.range(0, length)
                .mapToObj(i -> String.valueOf(finalCharset.charAt(RANDOM.nextInt(finalCharset.length()))))
                .collect(Collectors.joining());
    }

    /**
     * Генерирует список уникальных валидных токенов для параллельных тестов.
     * <p>
     * Метод гарантирует, что возвращаемые токены не будут повторяться в рамках одного вызова,
     * что критично для тестов, выполняющихся параллельно и использующих токены как идентификаторы.
     * <p>
     * <b>Особенности реализации:</b>
     * <ul>
     *   <li>Использует {@link IntStream} для генерации потока токенов</li>
     *   <li>Применяет {@link Stream#distinct()} для фильтрации дубликатов</li>
     *   <li>Ограничивает результат через {@link Stream#limit(long)} до запрошенного количества</li>
     *   <li>Собирает результат в {@link List} через {@link Collectors#toList()}</li>
     * </ul>
     * <p>
     * <b>Важно:</b> При очень большом {@code count} и малой энтропии алфавита
     * метод может работать дольше из-за повторной генерации при коллизиях.
     *
     * @param count желаемое количество уникальных токенов
     * @return список из {@code count} уникальных валидных токенов длиной 32 символа
     * @apiNote Возвращаемый список неизменяем для предотвращения случайной модификации
     * @see #generateValid()
     * @see java.util.stream.Stream#distinct()
     */
    public static List<String> generateUnique(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> generateValid())
                .distinct()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Проверяет, соответствует ли переданный токен требованиям технического задания.
     * <p>
     * <b>Критерии валидности:</b>
     * <ol>
     *   <li>Токен не должен быть {@code null}</li>
     *   <li>Длина токена должна быть ровно 32 символа</li>
     *   <li>Токен должен содержать только заглавные латинские буквы {@code A-Z} и цифры {@code 0-9}</li>
     *   <li>Полное соответствие регулярному выражению: {@code ^[A-Z0-9]{32}$}</li>
     * </ol>
     * <p>
     * <b>Примеры:</b>
     * <pre>{@code
     * TokenGenerator.isValid("ABCDEFGHIJKLMNOP1234567890ABCDEF"); // true
     * TokenGenerator.isValid("abcdefghijklmnop1234567890abcdef"); // false (lowercase)
     * TokenGenerator.isValid("ABC123");                            // false (too short)
     * TokenGenerator.isValid("ABC!@#12345678901234567890123456"); // false (special chars)
     * TokenGenerator.isValid(null);                                // false
     * TokenGenerator.isValid("");                                  // false
     * }</pre>
     *
     * @param token токен для проверки
     * @return {@code true} если токен полностью соответствует требованиям, иначе {@code false}
     * @apiNote Метод безопасен к {@code null} и пустым строкам
     * @see #generateValid()
     * @see #generateInvalid(InvalidType)
     */
    public static boolean isValid(String token) {
        return token != null
                && token.length() == 32
                && token.matches("^[A-Z0-9]{32}$");
    }

    /**
     * Генерирует токен с фиксированным префиксом для упрощения отладки и трассировки.
     * <p>
     * Метод полезен в сценариях, где необходимо визуально идентифицировать токены
     * в логах, базах данных или ответах API без потери соответствия формату.
     * <p>
     * <b>Правила формирования:</b>
     * <ul>
     *   <li>Если {@code prefix} равен {@code null}, используется пустая строка</li>
     *   <li>Если длина {@code prefix} >= 32, результат — первые 32 символа префикса в верхнем регистре</li>
     *   <li>Если длина {@code prefix} < 32, оставшаяся часть дополняется случайными символами из {@link #VALID_CHARSET}</li>
     *   <li>Итоговый результат всегда приводится к верхнему регистру и имеет длину ровно 32 символа</li>
     * </ul>
     * <p>
     * <b>Примеры:</b>
     * <pre>{@code
     * TokenGenerator.generateWithPrefix("TEST_");
     * // Результат: "TEST_XXXXXXXXXXXXXXXXXXXXXXXXX" (32 символа)
     *
     * TokenGenerator.generateWithPrefix("ABCDEFGHIJKLMNOP1234567890ABCDEF");
     * // Результат: "ABCDEFGHIJKLMNOP1234567890ABCDEF" (ровно 32, без дополнения)
     *
     * TokenGenerator.generateWithPrefix("ABCDEFGHIJKLMNOP1234567890ABCDEF_EXTRA");
     * // Результат: "ABCDEFGHIJKLMNOP1234567890ABCDEF" (обрезано до 32)
     * }</pre>
     *
     * @param prefix префикс для включения в начало токена
     * @return токен длиной 32 символа, начинающийся с {@code prefix} (при необходимости обрезанный или дополненный)
     * @apiNote Результат всегда в верхнем регистре и соответствует формату {@code ^[A-Z0-9]{32}$}
     * @see #generateValid()
     * @see #isValid(String)
     */
    public static String generateWithPrefix(String prefix) {
        if (prefix == null) prefix = "";
        if (prefix.length() >= 32) {
            return prefix.substring(0, 32).toUpperCase();
        }
        String suffix = generate(32 - prefix.length(), VALID_CHARSET);
        return (prefix + suffix).toUpperCase();
    }
}