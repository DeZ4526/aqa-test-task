package ru.nordcodes.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nordcodes.dto.response.AppResponse;

/**
 * Хранит состояние между шагами одного теста.
 * Помогает передавать данные (токен, ответ) без полей в тест-классе.
 */
@Data
@NoArgsConstructor
public class TestContext {

    // Токен пользователя
    private String token;

    // Последний ответ от приложения
    private AppResponse lastResponse;

    // Флаг: был ли выполнен LOGIN
    private boolean loggedIn;

    // Произвольные данные для тестов
    private String testData;

    /**
     * Сохраняет токен и отмечает, что пользователь авторизован.
     */
    public void markLoggedIn(String token) {
        this.token = token;
        this.loggedIn = true;
    }

    /**
     * Сбрасывает состояние после LOGOUT.
     */
    public void markLoggedOut() {
        this.token = null;
        this.loggedIn = false;
    }

    /**
     * Проверяет, можно ли выполнять ACTION (требуется предварительный LOGIN).
     */
    public boolean canPerformAction() {
        return loggedIn && token != null && !token.isEmpty();
    }

    /**
     * Очищает все поля.
     */
    public void clear() {
        this.token = null;
        this.lastResponse = null;
        this.loggedIn = false;
        this.testData = null;
    }
}