package com.hotel.services;

import com.hotel.models.Room;
import com.hotel.models.enums.RoomStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomService {
    protected final DatabaseService databaseService;
    
    public RoomService() {
        this.databaseService = DatabaseService.getInstance();
    }
    
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT id, number, price_per_night, capacity FROM rooms ORDER BY number";
        
        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Room room = new Room(
                    rs.getString("number"),
                    rs.getDouble("price_per_night"),
                    rs.getInt("capacity")
                );
                room.setId(rs.getInt("id"));
                
                // Определяем статус динамически
                RoomStatus status = getRoomStatus(room.getId());
                room.setStatus(status);
                
                rooms.add(room);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения номеров: " + e.getMessage());
        }
        
        return rooms;
    }
    
    public List<Room> getAvailableRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT id, number, price_per_night, capacity FROM rooms ORDER BY number";
        
        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Room room = new Room(
                    rs.getString("number"),
                    rs.getDouble("price_per_night"),
                    rs.getInt("capacity")
                );
                room.setId(rs.getInt("id"));
                
                // Определяем статус динамически
                RoomStatus status = getRoomStatus(room.getId());
                room.setStatus(status);
                
                // Добавляем только свободные номера
                if (status == RoomStatus.AVAILABLE) {
                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения свободных номеров: " + e.getMessage());
        }
        
        return rooms;
    }
    
    public List<Room> getAvailableRoomsForPeriod(LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = new ArrayList<>();
        String sql = """
            SELECT r.id, r.number, r.price_per_night, r.capacity 
            FROM rooms r
            WHERE r.id NOT IN (
                SELECT b.room_id 
                FROM bookings b 
                WHERE (b.check_in_date <= ? AND b.check_out_date >= ?) 
                   OR (b.check_in_date <= ? AND b.check_out_date >= ?)
                   OR (b.check_in_date >= ? AND b.check_out_date <= ?)
            )
            ORDER BY r.number
            """;
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(checkOut));
            pstmt.setDate(2, Date.valueOf(checkIn));
            pstmt.setDate(3, Date.valueOf(checkOut));
            pstmt.setDate(4, Date.valueOf(checkIn));
            pstmt.setDate(5, Date.valueOf(checkIn));
            pstmt.setDate(6, Date.valueOf(checkOut));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Room room = new Room(
                        rs.getString("number"),
                        rs.getDouble("price_per_night"),
                        rs.getInt("capacity")
                    );
                    room.setId(rs.getInt("id"));
                    room.setStatus(RoomStatus.AVAILABLE);
                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения доступных номеров на период: " + e.getMessage());
        }
        
        return rooms;
    }
    
    public List<Room> getOccupiedRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT id, number, price_per_night, capacity FROM rooms ORDER BY number";
        
        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Room room = new Room(
                    rs.getString("number"),
                    rs.getDouble("price_per_night"),
                    rs.getInt("capacity")
                );
                room.setId(rs.getInt("id"));
                
                // Определяем статус динамически
                RoomStatus status = getRoomStatus(room.getId());
                room.setStatus(status);
                
                // Добавляем только занятые номера
                if (status == RoomStatus.OCCUPIED) {
                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения занятых номеров: " + e.getMessage());
        }
        
        return rooms;
    }
    
    private RoomStatus getRoomStatus(int roomId) {
        String sql = """
            SELECT COUNT(*) as active_bookings 
            FROM bookings 
            WHERE room_id = ? 
            AND check_in_date <= CURRENT_DATE 
            AND check_out_date >= CURRENT_DATE
            """;
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("active_bookings") > 0 ? RoomStatus.OCCUPIED : RoomStatus.AVAILABLE;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка определения статуса номера: " + e.getMessage());
        }
        
        return RoomStatus.AVAILABLE;
    }
    
    public boolean isRoomAvailableForPeriod(int roomId, LocalDate checkIn, LocalDate checkOut) {
        String sql = """
            SELECT COUNT(*) as conflicting_bookings 
            FROM bookings 
            WHERE room_id = ? 
            AND ((check_in_date <= ? AND check_out_date >= ?) 
                OR (check_in_date <= ? AND check_out_date >= ?)
                OR (check_in_date >= ? AND check_out_date <= ?))
            """;
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roomId);
            pstmt.setDate(2, Date.valueOf(checkOut));
            pstmt.setDate(3, Date.valueOf(checkIn));
            pstmt.setDate(4, Date.valueOf(checkOut));
            pstmt.setDate(5, Date.valueOf(checkIn));
            pstmt.setDate(6, Date.valueOf(checkIn));
            pstmt.setDate(7, Date.valueOf(checkOut));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("conflicting_bookings") == 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка проверки доступности номера: " + e.getMessage());
        }
        
        return false;
    }
    
    // Удаляем метод updateRoomStatus, так как статус теперь определяется динамически
    
    // Статистика по номерам
    public int getTotalRoomsCount() {
        String sql = "SELECT COUNT(*) FROM rooms";
        return getCountFromQuery(sql);
    }
    
    public int getAvailableRoomsCount() {
        // Считаем номера без активных бронирований на сегодня
        String sql = """
            SELECT COUNT(*) 
            FROM rooms r
            WHERE r.id NOT IN (
                SELECT room_id 
                FROM bookings 
                WHERE check_in_date <= CURRENT_DATE 
                AND check_out_date >= CURRENT_DATE
            )
            """;
        return getCountFromQuery(sql);
    }
    
    public int getOccupiedRoomsCount() {
        // Считаем номера с активными бронированиями на сегодня
        String sql = """
            SELECT COUNT(DISTINCT room_id) 
            FROM bookings 
            WHERE check_in_date <= CURRENT_DATE 
            AND check_out_date >= CURRENT_DATE
            """;
        return getCountFromQuery(sql);
    }
    
    private int getCountFromQuery(String sql) {
        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения количества: " + e.getMessage());
        }
        return 0;
    }
}