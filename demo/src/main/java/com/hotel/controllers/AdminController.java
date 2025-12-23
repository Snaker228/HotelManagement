package com.hotel.controllers;

import com.hotel.models.Booking;
import com.hotel.models.Guest;
import com.hotel.models.Room;
import com.hotel.models.enums.RoomStatus;
import com.hotel.services.ExtendedBookingService;
import com.hotel.services.ExtendedGuestService;
import com.hotel.services.ExtendedRoomService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AdminController extends BookingController {
    private final ExtendedGuestService extendedGuestService;
    private final ExtendedRoomService extendedRoomService;
    private final ExtendedBookingService extendedBookingService;
    
    public AdminController() {
        super();
        this.extendedGuestService = new ExtendedGuestService();
        this.extendedRoomService = new ExtendedRoomService();
        this.extendedBookingService = new ExtendedBookingService();
    }
    
    // Расширенные методы для гостей
    public boolean updateGuest(Guest guest) {
        return extendedGuestService.updateGuest(guest);
    }
    
    public boolean deleteGuest(int guestId) {
        return extendedGuestService.deleteGuest(guestId);
    }
    
    // Расширенные методы для номеров
    public Room addRoom(Room room) {
        if (extendedRoomService.isRoomNumberExists(room.getNumber())) {
            throw new IllegalArgumentException("Номер с таким названием уже существует");
        }
        return extendedRoomService.addRoom(room);
    }
    
    public boolean updateRoom(Room room) {
        return extendedRoomService.updateRoom(room);
    }
    
    public boolean deleteRoom(int roomId) {
        return extendedRoomService.deleteRoom(roomId);
    }
    
    public boolean isRoomNumberExists(String roomNumber) {
        return extendedRoomService.isRoomNumberExists(roomNumber);
    }
    
    // Расширенные методы для бронирований
    public boolean updateBooking(Booking booking) {
        return extendedBookingService.updateBooking(booking);
    }
    
    // Статистика
    public Map<String, Object> getRevenueStatistics() {
        return extendedBookingService.getRevenueStatistics();
    }
    
    public Map<String, Double> getMonthlyRevenue() {
        return extendedBookingService.getMonthlyRevenue();
    }
    
    public Map<String, Object> getOccupancyStatistics() {
        return extendedBookingService.getOccupancyStatistics();
    }
    
    // Получение всех бронирований (включая архивные)
    @Override
    public List<Booking> getAllBookings() {
        return extendedBookingService.getAllBookings();
    }
}