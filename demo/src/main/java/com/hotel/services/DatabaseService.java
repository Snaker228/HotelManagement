package com.hotel.services;

import java.sql.*;

public class DatabaseService {
    // Настройки для PostgreSQL
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/hotel_db";
    private static final String DB_USER = "hotel_user";
    private static final String DB_PASSWORD = "hotel123";
    
    private static DatabaseService instance;
    private Connection connection;

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private DatabaseService() {
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try {
            // Явно загружаем драйвер
            Class.forName("org.postgresql.Driver");
            
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            createTables();
            insertDefaultUsers();
            System.out.println("PostgreSQL база данных инициализирована успешно!");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL драйвер не найден: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Ошибка инициализации БД: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createTables() throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL UNIQUE,
                password VARCHAR(100),
                type VARCHAR(20) DEFAULT 'DEFAULT'
            );
            """;

        String createGuestsTable = """
            CREATE TABLE IF NOT EXISTS guests (
                id SERIAL PRIMARY KEY,
                first_name VARCHAR(100) NOT NULL,
                last_name VARCHAR(100) NOT NULL,
                phone VARCHAR(20) NOT NULL UNIQUE
            );
            """;
            
        String createRoomsTable = """
            CREATE TABLE IF NOT EXISTS rooms (
                id SERIAL PRIMARY KEY,
                number VARCHAR(10) NOT NULL UNIQUE,
                price_per_night DECIMAL(10,2) NOT NULL,
                capacity INTEGER NOT NULL
            );
            """;

        String createBookingsTable = """
            CREATE TABLE IF NOT EXISTS bookings (
                id SERIAL PRIMARY KEY,
                guest_id INTEGER REFERENCES guests(id),
                room_id INTEGER REFERENCES rooms(id),
                check_in_date DATE,
                check_out_date DATE
            );
            """;
            
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createGuestsTable);
            stmt.execute(createRoomsTable);
            stmt.execute(createBookingsTable);
        }
    }
    
    private void insertDefaultUsers() {
        String checkUsers = "SELECT COUNT(*) FROM users";
        String insertAdmin = """
            INSERT INTO users (name, password, type) VALUES (?, ?, ?) 
            ON CONFLICT (name) DO NOTHING
            """;
        String insertReception = """
            INSERT INTO users (name, password, type) VALUES (?, ?, ?) 
            ON CONFLICT (name) DO NOTHING
            """;
        
        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(checkUsers)) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                // Добавляем пользователей только если таблица пуста
                try (PreparedStatement pstmt = connection.prepareStatement(insertAdmin)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, "admin123");
                    pstmt.setString(3, "ADMIN");
                    pstmt.executeUpdate();
                }
                
                try (PreparedStatement pstmt = connection.prepareStatement(insertReception)) {
                    pstmt.setString(1, "reception");
                    pstmt.setString(2, "");
                    pstmt.setString(3, "DEFAULT");
                    pstmt.executeUpdate();
                }
                
                System.out.println("Добавлены пользователи по умолчанию: admin/admin123, reception/reception123");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении пользователей: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к БД: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Ошибка закрытия соединения: " + e.getMessage());
        }
    }
}