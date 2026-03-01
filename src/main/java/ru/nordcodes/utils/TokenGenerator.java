package ru.nordcodes.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j;
import ru.nordcodes.config.TestConfig;
import ru.nordcodes.utils.enums.InvalidType;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Утилиты для генерации валидных и невалидных токенов для тестов.
 *
 * Требования к токену (из ТЗ):
 * - Длина: ровно 32 символа
 * - Символы: только A-Z (заглавные) и 0-9
 */
@Log4j
@UtilityClass
public class TokenGenerator {

    /**
     * Алфавит для валидных токенов.
     */
    private static final String VALID_CHARSET = TestConfig.TOKEN_CHARSET;

    /**
     * Генератор случайных чисел для криптографически стойких токенов.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Генерирует валидный токен длиной 32 символа.
     * @return строка вида "ABCD1234...XYZ9" (32 символа, A-Z0-9)
     */
    public static String generateValid() {
        return generate(32, VALID_CHARSET);
    }

    /**
     * Генерирует валидный токен заданной длины.
     * @param length желаемая длина токена
     * @return сгенерированный токен
     */
    public static String generateValid(int length) {
        return generate(length, VALID_CHARSET);
    }

    /**
     * Генерирует невалидный токен по заданному типу нарушения.
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
     * Внутренний метод генерации строки из заданного алфавита.
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
     * Генерирует список уникальных токенов (для параллельных тестов).
     * @param count количество токенов
     * @return список уникальных токенов
     */
    public static List<String> generateUnique(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> generateValid())
                .distinct()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Проверяет, соответствует ли токен требованиям ТЗ.
     * @param token токен для проверки
     * @return true если токен валиден
     */
    public static boolean isValid(String token) {
        return token != null
                && token.length() == 32
                && token.matches("^[A-Z0-9]{32}$");
    }

    /**
     * Генерирует токен с фиксированным префиксом (для отладки).
     * @param prefix префикс (будет обрезан/дополнен до 32 символов)
     * @return токен длиной 32 символа, начинающийся с prefix
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
