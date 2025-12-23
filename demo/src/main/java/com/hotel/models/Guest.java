package com.hotel.models;

public class Guest {
    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    
    public Guest() {} // Конструктор без параметров
    
    public Guest(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }
    
    // Геттеры
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getFullName() { return firstName + " " + lastName; }
    
    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhone(String phone) { this.phone = phone; }
    
    @Override
    public String toString() {
        return getFullName() + " (" + phone + ")";
    }
}