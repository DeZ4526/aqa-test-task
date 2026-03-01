package ru.nordcodes.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j;

@Log4j
@UtilityClass
public class JsonUtil {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())  // Поддержка Java 8 Date/Time
            .findAndAddModules()
            .build();

    /**
     * Сериализует объект в JSON-строку.
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
     * Десериализует JSON-строку в объект.
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