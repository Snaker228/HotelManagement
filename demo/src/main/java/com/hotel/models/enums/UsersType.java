package com.hotel.models.enums;

public enum UsersType {
    ADMIN("Администратор"),
    DEFAULT("Ресепшн");
    
    private final String displayName;
    
    UsersType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
    
    @Override
    public String toString() {
        return displayName;
    }
}
