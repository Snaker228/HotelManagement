package com.hotel.services;

import com.hotel.models.Guest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuestService {
    protected final DatabaseService databaseService;
    
    public GuestService() {
        this.databaseService = DatabaseService.getInstance();
    }
    
    public List<Guest> getAllGuests() {
        List<Guest> guests = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, phone FROM guests ORDER BY last_name, first_name";
        
        try (Connection conn = databaseService.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Guest guest = new Guest(
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("phone")
                );
                guest.setId(rs.getInt("id"));
                guests.add(guest);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения гостей: " + e.getMessage());
        }
        
        return guests;
    }
    
    public boolean isPhoneExists(String phone) {
        String sql = "SELECT COUNT(*) FROM guests WHERE phone = ?";
        
        try (Connection conn = databaseService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка проверки телефона: " + e.getMessage());
        }
        
        return false;
    }
    
    public Guest addGuest(Guest guest) {
        // Проверяем уникальность телефона
        if (isPhoneExists(guest.getPhone())) {
            throw new IllegalArgumentException("Гость с таким номером телефона уже существует");
        }
        
        String sql = "INSERT INTO guests (first_name, last_name, phone) VALUES (?, ?, ?) RETURNING id";
        
        try (Connection conn = databaseService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, guest.getFirstName());
            pstmt.setString(2, guest.getLastName());
            pstmt.setString(3, guest.getPhone());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    guest.setId(rs.getInt("id"));
                    return guest;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка добавления гостя: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean deleteGuest(int guestId) {
        String sql = "DELETE FROM guests WHERE id = ?";
        
        try (Connection conn = databaseService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, guestId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Ошибка удаления гостя: " + e.getMessage());
            return false;
        }
    }
}