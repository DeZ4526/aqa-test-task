package ru.nordcodes.utils.enums;

/**
 * Типы невалидных токенов для параметризованных тестов.
 */
public enum InvalidType {
    TOO_SHORT("Слишком короткий (< 32 символов)"),
    TOO_LONG("Слишком длинный (> 32 символа)"),
    LOWERCASE("Содержит строчные буквы (a-z)"),
    SPECIAL_CHARS("Содержит спецсимволы (!@#$)"),
    EMPTY("Пустая строка"),
    NULL("null значение"),
    UNICODE("Содержит не-ASCII символы");

    private final String description;

    InvalidType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
