package com.hotel.models;

import com.hotel.models.enums.RoomStatus;

public class Room {
    private int id;
    private String number;
    private double pricePerNight;
    private RoomStatus status;
    private int capacity;
    
    public Room(String number, double pricePerNight, int capacity) {
        this.number = number;
        this.pricePerNight = pricePerNight;
        this.status = RoomStatus.AVAILABLE;
        this.capacity = capacity;
    }
    
    // Геттеры
    public int getId() { return id; }
    public String getNumber() { return number; }
    public double getPricePerNight() { return pricePerNight; }
    public RoomStatus getStatus() { return status; }
    public int getCapacity() { return capacity; }
    
    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setNumber(String number) { this.number = number; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
    public void setStatus(RoomStatus status) { this.status = status; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    
    @Override
    public String toString() {
        return "Номер " + number + " " + pricePerNight + " руб./ночь - " + status;
    }
}