package com.hotel.services;

import com.hotel.models.Users;
import com.hotel.models.enums.UsersType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final DatabaseService databaseService;
    
    public UserService() {
        this.databaseService = DatabaseService.getInstance();
    }
    
    public List<Users> getAllUsers() {
        List<Users> users = new ArrayList<>();
        String sql = "SELECT id, name, password, type FROM users ORDER BY name";
        
        try (Connection conn = databaseService.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Users user = new Users(
                    rs.getString("name"),
                    rs.getString("password")
                );
                user.setId(rs.getInt("id"));
                
                // Устанавливаем тип пользователя
                String typeStr = rs.getString("type");
                try {
                    UsersType type = UsersType.valueOf(typeStr);
                    user.setType(type);
                } catch (IllegalArgumentException e) {
                    user.setType(UsersType.DEFAULT);
                }
                
                users.add(user);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка получения пользователей: " + e.getMessage());
            e.printStackTrace();
        }
        
        return users;
    }
    
    public Users authenticate(String username, String password) {
        String sql = "SELECT id, name, password, type FROM users WHERE name = ? AND password = ?";
        
        try (Connection conn = databaseService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Users user = new Users(
                        rs.getString("name"),
                        rs.getString("password")
                    );
                    user.setId(rs.getInt("id"));
                    
                    String typeStr = rs.getString("type");
                    try {
                        UsersType type = UsersType.valueOf(typeStr);
                        user.setType(type);
                    } catch (IllegalArgumentException e) {
                        user.setType(UsersType.DEFAULT);
                    }
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка аутентификации: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public Users getUserByUsername(String username) {
        String sql = "SELECT id, name, password, type FROM users WHERE name = ?";
        
        try (Connection conn = databaseService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Users user = new Users(
                        rs.getString("name"),
                        rs.getString("password")
                    );
                    user.setId(rs.getInt("id"));
                    
                    String typeStr = rs.getString("type");
                    try {
                        UsersType type = UsersType.valueOf(typeStr);
                        user.setType(type);
                    } catch (IllegalArgumentException e) {
                        user.setType(UsersType.DEFAULT);
                    }
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка получения пользователя: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}