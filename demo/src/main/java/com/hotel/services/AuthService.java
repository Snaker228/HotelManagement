package com.hotel.services;

import com.hotel.models.Users;
import com.hotel.views.*;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AuthService {
    private final UserService userService;
    private Stage primaryStage;
    
    public AuthService(Stage primaryStage) {
        this.userService = new UserService();
        this.primaryStage = primaryStage;
    }
    
    public boolean authenticate(String username, String password) {
        Users user = userService.authenticate(username, password);
        if (user != null) {
            navigateToUserForm(user);
            return true;
        }
        return false;
    }
    
    private void navigateToUserForm(Users user) {
        switch (user.getType()) {
            case ADMIN:
                openAdminForm(user);
                break;
            case DEFAULT:
                openReceptionForm(user);
                break;
            default:
                openReceptionForm(user);
        }
    }
    
    private void openAdminForm(Users user) {
        AdminForm adminForm = new AdminForm(user);
        
        adminForm.setLogoutListener(() -> {
            showAuthForm();
        });
        
        Scene scene = new Scene(adminForm, 800, 600);
        primaryStage.setTitle("Отель - Панель администратора");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
    }
    
    private void openReceptionForm(Users user) {
        ReceptionForm receptionForm = new ReceptionForm(user);
        
        receptionForm.setLogoutListener(() -> {
            showAuthForm();
        });
        
        Scene scene = new Scene(receptionForm, 1000, 700);
        primaryStage.setTitle("Отель - Панель ресепшн");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
    }
    
    public void showAuthForm() {
        // Этот метод будет вызван из AuthForm
        AnimatedAuthForm authForm = new AnimatedAuthForm(this);
        Scene scene = new Scene(authForm, 400, 350);
        primaryStage.setTitle("Отель - Вход в систему");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        authForm.focusUserSelection();
    }
}