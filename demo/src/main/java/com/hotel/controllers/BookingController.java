package com.hotel.controllers;

import com.hotel.models.Booking;
import com.hotel.models.Guest;
import com.hotel.models.Room;
import com.hotel.services.BookingService;
import com.hotel.services.GuestService;
import com.hotel.services.RoomService;

import java.time.LocalDate;
import java.util.List;

public class BookingController {
    private final BookingService bookingService;
    private final GuestService guestService;
    private final RoomService roomService;
    
    public BookingController() {
        this.bookingService = new BookingService();
        this.guestService = new GuestService();
        this.roomService = new RoomService();
    }
    
    public List<Guest> getAllGuests() {
        return guestService.getAllGuests();
    }
    
    public List<Room> getAvailableRooms() {
        return roomService.getAvailableRooms();
    }
    
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }
    
    public List<Room> getOccupiedRooms() {
        return roomService.getOccupiedRooms();
    }
    
    public List<Room> getAvailableRoomsForPeriod(LocalDate checkIn, LocalDate checkOut) {
        return roomService.getAvailableRoomsForPeriod(checkIn, checkOut);
    }
    
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }
    
    public List<Booking> getCurrentBookings() {
        return bookingService.getCurrentBookings();
    }
    
    public List<Booking> getBookingsForRoom(int roomId) {
        return bookingService.getBookingsForRoom(roomId);
    }
    
    public Guest addGuest(Guest guest) {
        return guestService.addGuest(guest);
    }
    
    public boolean isPhoneExists(String phone) {
        return guestService.isPhoneExists(phone);
    }
    
    public Booking createBooking(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
        if (guest == null || room == null || checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Все поля должны быть заполнены");
        }
        
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            throw new IllegalArgumentException("Дата выезда должна быть после даты заезда");
        }
        
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Дата заезда не может быть в прошлом");
        }
        
        // Проверяем доступность номера
        if (!roomService.isRoomAvailableForPeriod(room.getId(), checkIn, checkOut)) {
            throw new IllegalArgumentException("Номер " + room.getNumber() + " уже забронирован на выбранные даты");
        }
        
        Booking booking = new Booking(guest, room, checkIn, checkOut);
        return bookingService.addBooking(booking);
    }
    
    public boolean cancelBooking(int bookingId) {
        return bookingService.deleteBooking(bookingId);
    }
    
    // Статистика
    public int getTotalRoomsCount() {
        return roomService.getTotalRoomsCount();
    }
    
    public int getAvailableRoomsCount() {
        return roomService.getAvailableRoomsCount();
    }
    
    public int getOccupiedRoomsCount() {
        return roomService.getOccupiedRoomsCount();
    }
    
    public boolean isRoomAvailableForPeriod(int roomId, LocalDate checkIn, LocalDate checkOut) {
        return roomService.isRoomAvailableForPeriod(roomId, checkIn, checkOut);
    }
}