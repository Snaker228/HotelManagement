package com.hotel.services;

import com.hotel.models.Guest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExtendedGuestService extends GuestService {
    
    public boolean updateGuest(Guest guest) {
        if (guest.getId() == 0) {
            throw new IllegalArgumentException("ID гостя не может быть 0");
        }
        
        // Проверяем уникальность телефона (исключая текущего гостя)
        String checkPhoneSql = "SELECT COUNT(*) FROM guests WHERE phone = ? AND id != ?";
        try (Connection conn = databaseService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(checkPhoneSql)) {
            
            pstmt.setString(1, guest.getPhone());
            pstmt.setInt(2, guest.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalArgumentException("Гость с таким номером телефона уже существует");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка проверки телефона: " + e.getMessage());
            return false;
        }
        
        String sql = "UPDATE guests SET first_name = ?, last_name = ?, phone = ? WHERE id = ?";
        
        try (Connection conn = databaseService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, guest.getFirstName());
            pstmt.setString(2, guest.getLastName());
            pstmt.setString(3, guest.getPhone());
            pstmt.setInt(4, guest.getId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Ошибка обновления гостя: " + e.getMessage());
            return false;
        }
    }
}