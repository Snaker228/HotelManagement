package com.hotel.services;

import com.hotel.models.Booking;
import com.hotel.models.Guest;
import com.hotel.models.Room;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingService {
    protected final DatabaseService databaseService;
    private final GuestService guestService;
    private final RoomService roomService;
    
    public BookingService() {
        this.databaseService = DatabaseService.getInstance();
        this.guestService = new GuestService();
        this.roomService = new RoomService();
    }
    
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.id, b.guest_id, b.room_id, b.check_in_date, b.check_out_date,
                   g.first_name, g.last_name, g.phone,
                   r.number, r.price_per_night, r.capacity
            FROM bookings b
            JOIN guests g ON b.guest_id = g.id
            JOIN rooms r ON b.room_id = r.id
            ORDER BY b.check_in_date DESC
            """;
        
        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Guest guest = new Guest(
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("phone")
                );
                guest.setId(rs.getInt("guest_id"));
                
                Room room = new Room(
                    rs.getString("number"),
                    rs.getDouble("price_per_night"),
                    rs.getInt("capacity")
                );
                room.setId(rs.getInt("room_id"));
                
                LocalDate checkIn = rs.getDate("check_in_date").toLocalDate();
                LocalDate checkOut = rs.getDate("check_out_date").toLocalDate();
                
                Booking booking = new Booking(guest, room, checkIn, checkOut);
                booking.setId(rs.getInt("id"));
                bookings.add(booking);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения бронирований: " + e.getMessage());
        }
        
        return bookings;
    }
    
    public Booking addBooking(Booking booking) {
        // Проверяем доступность номера на выбранные даты
        if (!roomService.isRoomAvailableForPeriod(
            booking.getRoom().getId(), 
            booking.getCheckInDate(), 
            booking.getCheckOutDate())) {
            throw new IllegalArgumentException("Номер уже забронирован на выбранные даты");
        }
        
        String sql = "INSERT INTO bookings (guest_id, room_id, check_in_date, check_out_date) VALUES (?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, booking.getGuest().getId());
            pstmt.setInt(2, booking.getRoom().getId());
            pstmt.setDate(3, Date.valueOf(booking.getCheckInDate()));
            pstmt.setDate(4, Date.valueOf(booking.getCheckOutDate()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    booking.setId(rs.getInt("id"));
                    return booking;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка добавления бронирования: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean deleteBooking(int bookingId) {
        String sql = "DELETE FROM bookings WHERE id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookingId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Ошибка удаления бронирования: " + e.getMessage());
            return false;
        }
    }
    
    public List<Booking> getCurrentBookings() {
        LocalDate today = LocalDate.now();
        String sql = """
            SELECT b.id, b.guest_id, b.room_id, b.check_in_date, b.check_out_date,
                   g.first_name, g.last_name, g.phone,
                   r.number, r.price_per_night, r.capacity
            FROM bookings b
            JOIN guests g ON b.guest_id = g.id
            JOIN rooms r ON b.room_id = r.id
            WHERE b.check_in_date <= ? AND b.check_out_date >= ?
            ORDER BY b.check_in_date
            """;
        
        List<Booking> bookings = new ArrayList<>();
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(today));
            pstmt.setDate(2, Date.valueOf(today));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Guest guest = new Guest(
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone")
                    );
                    guest.setId(rs.getInt("guest_id"));
                    
                    Room room = new Room(
                        rs.getString("number"),
                        rs.getDouble("price_per_night"),
                        rs.getInt("capacity")
                    );
                    room.setId(rs.getInt("room_id"));
                    
                    LocalDate checkIn = rs.getDate("check_in_date").toLocalDate();
                    LocalDate checkOut = rs.getDate("check_out_date").toLocalDate();
                    
                    Booking booking = new Booking(guest, room, checkIn, checkOut);
                    booking.setId(rs.getInt("id"));
                    bookings.add(booking);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения текущих бронирований: " + e.getMessage());
        }
        
        return bookings;
    }
    
    // Получение бронирований для конкретного номера
    public List<Booking> getBookingsForRoom(int roomId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.id, b.guest_id, b.room_id, b.check_in_date, b.check_out_date,
                   g.first_name, g.last_name, g.phone,
                   r.number, r.price_per_night, r.capacity
            FROM bookings b
            JOIN guests g ON b.guest_id = g.id
            JOIN rooms r ON b.room_id = r.id
            WHERE b.room_id = ?
            ORDER BY b.check_in_date
            """;
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roomId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Guest guest = new Guest(
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone")
                    );
                    guest.setId(rs.getInt("guest_id"));
                    
                    Room room = new Room(
                        rs.getString("number"),
                        rs.getDouble("price_per_night"),
                        rs.getInt("capacity")
                    );
                    room.setId(rs.getInt("room_id"));
                    
                    LocalDate checkIn = rs.getDate("check_in_date").toLocalDate();
                    LocalDate checkOut = rs.getDate("check_out_date").toLocalDate();
                    
                    Booking booking = new Booking(guest, room, checkIn, checkOut);
                    booking.setId(rs.getInt("id"));
                    bookings.add(booking);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения бронирований для номера: " + e.getMessage());
        }
        
        return bookings;
    }
}