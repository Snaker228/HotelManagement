package com.hotel.services;

import com.hotel.models.Room;
import com.hotel.models.enums.RoomStatus;

import java.sql.*;

public class ExtendedRoomService extends RoomService {
    
    public Room addRoom(Room room) {
        String sql = "INSERT INTO rooms (number, price_per_night, capacity) VALUES (?, ?, ?) RETURNING id";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, room.getNumber());
            pstmt.setDouble(2, room.getPricePerNight());
            pstmt.setInt(3, room.getCapacity());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    room.setId(rs.getInt("id"));
                    // Статус будет определяться динамически
                    room.setStatus(RoomStatus.AVAILABLE);
                    return room;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка добавления номера: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean updateRoom(Room room) {
        if (room.getId() == 0) {
            throw new IllegalArgumentException("ID номера не может быть 0");
        }
        
        String sql = "UPDATE rooms SET number = ?, price_per_night = ?, capacity = ? WHERE id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, room.getNumber());
            pstmt.setDouble(2, room.getPricePerNight());
            pstmt.setInt(3, room.getCapacity());
            pstmt.setInt(4, room.getId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Ошибка обновления номера: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteRoom(int roomId) {
        // Сначала проверяем, нет ли активных бронирований для этого номера
        String checkBookingsSql = """
            SELECT COUNT(*) 
            FROM bookings 
            WHERE room_id = ? 
            AND check_out_date >= CURRENT_DATE
            """;
        String deleteSql = "DELETE FROM rooms WHERE id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkBookingsSql);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            
            checkStmt.setInt(1, roomId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalArgumentException("Нельзя удалить номер с активными бронированиями");
                }
            }
            
            deleteStmt.setInt(1, roomId);
            return deleteStmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Ошибка удаления номера: " + e.getMessage());
            return false;
        }
    }
    
    public boolean isRoomNumberExists(String roomNumber) {
        String sql = "SELECT COUNT(*) FROM rooms WHERE number = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, roomNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка проверки номера комнаты: " + e.getMessage());
        }
        
        return false;
    }
}