package com.hotel.controllers;

import com.hotel.models.Users;
import com.hotel.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class AuthController {
    private final UserService userService;
    private ObservableList<Users> usersList;
    
    public AuthController() {
        this.userService = new UserService();
        loadUsers();
    }
    
    private void loadUsers() {
        List<Users> users = userService.getAllUsers();
        this.usersList = FXCollections.observableArrayList(users);
    }
    
    public ObservableList<Users> getUsersList() {
        return usersList;
    }
    
    public Users authenticate(Users selectedUser, String password) {
        if (selectedUser == null) {
            throw new IllegalArgumentException("Пользователь не выбран");
        }
        
        // if (password == null || password.trim().isEmpty()) {
        //     throw new IllegalArgumentException("Введите пароль");
        // }
        
        Users authenticatedUser = userService.authenticate(
            selectedUser.getName(), 
            password
        );
        
        if (authenticatedUser == null) {
            throw new SecurityException("Неверный пароль");
        }
        
        return authenticatedUser;
    }
    
    public void refreshUsers() {
        loadUsers();
    }
}