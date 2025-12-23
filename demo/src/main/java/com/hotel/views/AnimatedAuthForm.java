package com.hotel.views;

import com.hotel.controllers.AuthController;
import com.hotel.models.Users;
import com.hotel.services.AuthService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class AnimatedAuthForm extends VBox {
    private Label titleLabel;
    private ComboBox<Users> usersBox;
    private PasswordField passwordField;
    private Button loginButton;
    private Label messageLabel;
    
    private AuthController controller;
    private AuthService authService;
    
    public AnimatedAuthForm(AuthService authService) {
        this.authService = authService;
        this.controller = new AuthController();
        initializeUI();
        setupEventHandlers();
        playEntranceAnimation();
    }
    
    private void initializeUI() {
        setAlignment(Pos.CENTER);
        setSpacing(15);
        setPadding(new Insets(25));
        setStyle("-fx-background-color: #f8f9fa;");
        
        // Заголовок
        titleLabel = new Label("Вход в систему отеля");
        titleLabel.setFont(Font.font(20));
        titleLabel.setTextFill(Color.DARKBLUE);
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        // ComboBox с пользователями
        Label userLabel = new Label("Выберите пользователя:");
        userLabel.setStyle("-fx-font-weight: bold;");
        
        usersBox = new ComboBox<>();
        usersBox.setItems(controller.getUsersList());
        usersBox.setPrefWidth(250);
        usersBox.setPromptText("Выберите пользователя...");
        
        // Поле пароля
        Label passwordLabel = new Label("Пароль:");
        passwordLabel.setStyle("-fx-font-weight: bold;");
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Введите пароль");
        passwordField.setPrefWidth(250);
        
        // Кнопка входа
        loginButton = new Button("Войти");
        loginButton.setStyle("-fx-font-size: 14; -fx-background-color: #007bff; -fx-text-fill: white;");
        loginButton.setPrefWidth(120);
        loginButton.setPrefHeight(35);
        
        // Сообщение
        messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 12;");
        
        // Добавляем элементы
        getChildren().addAll(
            titleLabel,
            userLabel,
            usersBox,
            passwordLabel,
            passwordField,
            loginButton,
            messageLabel
        );
        
        // Обработчик выбора пользователя
        usersBox.setOnAction(event -> {
            if (usersBox.getValue() != null) {
                passwordField.requestFocus();
            }
        });
    }
    
    private void playEntranceAnimation() {
        // Сначала делаем все элементы невидимыми
        for (var node : getChildren()) {
            node.setOpacity(0);
            node.setTranslateY(20);
        }
        
        // Анимация появления элементов с задержкой
        for (int i = 0; i < getChildren().size(); i++) {
            var node = getChildren().get(i);
            
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), node);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), node);
            translateTransition.setFromY(20);
            translateTransition.setToY(0);
            
            ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, translateTransition);
            parallelTransition.setDelay(Duration.millis(i * 100)); // Задержка для каждого элемента
            parallelTransition.play();
        }
    }
    
    private void playSuccessAnimation() {
        // Анимация успешного входа
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), loginButton);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);
        
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(600), messageLabel);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        
        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();
    }
    
    private void playErrorAnimation() {
        // Анимация ошибки
        TranslateTransition shakeTransition = new TranslateTransition(Duration.millis(100), passwordField);
        shakeTransition.setFromX(0);
        shakeTransition.setToX(10);
        shakeTransition.setAutoReverse(true);
        shakeTransition.setCycleCount(6);
        
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), messageLabel);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        
        ParallelTransition parallelTransition = new ParallelTransition(shakeTransition, fadeTransition);
        parallelTransition.play();
    }
    
    private void setupEventHandlers() {
        loginButton.setOnAction(event -> handleLogin());
        passwordField.setOnAction(event -> handleLogin());
    }
    
    private void handleLogin() {
        try {
            Users selectedUser = usersBox.getValue();
            String password = passwordField.getText();
            
            // Валидация
            if (selectedUser == null) {
                showMessage("Выберите пользователя из списка", true);
                usersBox.requestFocus();
                playErrorAnimation();
                return;
            }
            
            // Аутентификация через контроллер
            Users authenticatedUser = controller.authenticate(selectedUser, password);
            
            // Успешный вход - навигация через AuthService
            showMessage("Успешный вход! Перенаправление...", false);
            playSuccessAnimation();
            
            // Очистка формы
            clearForm();
            
            // Навигация через AuthService
            boolean loginSuccess = authService.authenticate(
                authenticatedUser.getName(), 
                authenticatedUser.getPassword()
            );
            
            if (!loginSuccess) {
                showMessage("Ошибка навигации", true);
            }
            
        } catch (IllegalArgumentException e) {
            showMessage(e.getMessage(), true);
            playErrorAnimation();
        } catch (SecurityException e) {
            showMessage(e.getMessage(), true);
            passwordField.clear();
            passwordField.requestFocus();
            playErrorAnimation();
        } catch (Exception e) {
            showMessage("Ошибка системы: " + e.getMessage(), true);
            playErrorAnimation();
        }
    }
    
    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setTextFill(isError ? Color.RED : Color.GREEN);
    }
    
    public void clearForm() {
        usersBox.setValue(null);
        passwordField.clear();
        messageLabel.setText("");
    }
    
    public void refreshUsers() {
        controller.refreshUsers();
        usersBox.setItems(controller.getUsersList());
    }
    
    public void focusUserSelection() {
        usersBox.requestFocus();
    }
}