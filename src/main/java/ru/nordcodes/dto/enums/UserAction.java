package ru.nordcodes.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserAction {

    LOGIN("LOGIN", "Аутентификация: отправляет запрос /auth во внешний сервис"),
    ACTION("ACTION", "Выполнение действия: доступно только после успешного LOGIN"),
    LOGOUT("LOGOUT", "Завершение сессии: удаляет токен из внутреннего хранилища");

    private final String value;
    private final String description;

    /**
     * Преобразует строку в enum (case-insensitive).
     * @param value строковое значение действия
     * @return соответствующий enum или null если не найдено
     */
    public static UserAction fromString(String value) {
        if (value == null) return null;
        for (UserAction action : values()) {
            if (action.value.equalsIgnoreCase(value.trim())) {
                return action;
            }
        }
        return null;
    }

    /**
     * Проверяет, является ли строка валидным действием.
     */
    public static boolean isValid(String value) {
        return fromString(value) != null;
    }
}
