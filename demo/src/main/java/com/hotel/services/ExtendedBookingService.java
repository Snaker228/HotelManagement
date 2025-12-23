package com.hotel.services;

import com.hotel.models.Booking;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ExtendedBookingService extends BookingService {
    
    public boolean updateBooking(Booking booking) {
        if (booking.getId() == 0) {
            throw new IllegalArgumentException("ID бронирования не может быть 0");
        }
        
        String sql = "UPDATE bookings SET guest_id = ?, room_id = ?, check_in_date = ?, check_out_date = ? WHERE id = ?";
        
        try (Connection conn = databaseService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, booking.getGuest().getId());
            pstmt.setInt(2, booking.getRoom().getId());
            pstmt.setDate(3, Date.valueOf(booking.getCheckInDate()));
            pstmt.setDate(4, Date.valueOf(booking.getCheckOutDate()));
            pstmt.setInt(5, booking.getId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Ошибка обновления бронирования: " + e.getMessage());
            return false;
        }
    }
    
    // Статистика доходов
    public Map<String, Object> getRevenueStatistics() {
        Map<String, Object> stats = new HashMap<>();
        String sql = """
            SELECT 
                COUNT(*) as total_bookings,
                SUM((b.check_out_date - b.check_in_date) * r.price_per_night) as total_revenue,
                AVG((b.check_out_date - b.check_in_date) * r.price_per_night) as avg_booking_value,
                MAX(r.price_per_night) as max_room_price,
                MIN(r.price_per_night) as min_room_price
            FROM bookings b
            JOIN rooms r ON b.room_id = r.id
            """;
        
        try (Connection conn = databaseService.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                stats.put("totalBookings", rs.getInt("total_bookings"));
                stats.put("totalRevenue", rs.getDouble("total_revenue"));
                stats.put("avgBookingValue", rs.getDouble("avg_booking_value"));
                stats.put("maxRoomPrice", rs.getDouble("max_room_price"));
                stats.put("minRoomPrice", rs.getDouble("min_room_price"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения статистики доходов: " + e.getMessage());
        }
        
        return stats;
    }
    
    // Статистика по месяцам
    public Map<String, Double> getMonthlyRevenue() {
        Map<String, Double> monthlyRevenue = new HashMap<>();
        String sql = """
            SELECT 
                TO_CHAR(b.check_in_date, 'YYYY-MM') as month,
                SUM((b.check_out_date - b.check_in_date) * r.price_per_night) as monthly_revenue
            FROM bookings b
            JOIN rooms r ON b.room_id = r.id
            GROUP BY TO_CHAR(b.check_in_date, 'YYYY-MM')
            ORDER BY month
            """;
        
        try (Connection conn = databaseService.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                monthlyRevenue.put(rs.getString("month"), rs.getDouble("monthly_revenue"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения месячной статистики: " + e.getMessage());
        }
        
        return monthlyRevenue;
    }
    
    // Статистика загрузки номеров
    public Map<String, Object> getOccupancyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        String sql = """
            SELECT 
                COUNT(*) as total_rooms,
                SUM(CASE WHEN status = 'OCCUPIED' THEN 1 ELSE 0 END) as occupied_rooms,
                SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) as available_rooms,
                ROUND((SUM(CASE WHEN status = 'OCCUPIED' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)), 2) as occupancy_rate
            FROM rooms
            """;
        
        try (Connection conn = databaseService.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                stats.put("totalRooms", rs.getInt("total_rooms"));
                stats.put("occupiedRooms", rs.getInt("occupied_rooms"));
                stats.put("availableRooms", rs.getInt("available_rooms"));
                stats.put("occupancyRate", rs.getDouble("occupancy_rate"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения статистики загрузки: " + e.getMessage());
        }
        
        return stats;
    }
}