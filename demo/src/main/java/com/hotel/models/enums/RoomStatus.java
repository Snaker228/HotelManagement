package com.hotel.models.enums;

public enum RoomStatus {
    AVAILABLE("Свободен"),
    OCCUPIED("Занят");
    
    private final String displayName;
    
    RoomStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
    
    @Override
    public String toString() {
        return displayName;
    }
}