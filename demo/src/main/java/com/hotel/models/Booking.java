package com.hotel.models;

import java.time.LocalDate;

public class Booking {
    private int id;
    private Guest guest;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private long totalPrice;
    
    public Booking(Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = calculateTotalPrice();
    }
    
    public long calculateTotalPrice() {
        if (checkInDate != null && checkOutDate != null && room != null) {
            long nights = checkOutDate.toEpochDay() - checkInDate.toEpochDay();
            return (long) (nights * room.getPricePerNight());
        } else return 0;
    }
    
    // Геттеры
    public int getId() { return id; }
    public Guest getGuest() { return guest; }
    public Room getRoom() { return room; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public long getTotalPrice() { return totalPrice; }
    
    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setGuest(Guest guest) { this.guest = guest; }
    public void setRoom(Room room) { this.room = room; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    
    @Override
    public String toString() {
        return guest.getFullName() + " - " + room.getNumber() + " (" + checkInDate + " до " + checkOutDate + ")";
    }
}