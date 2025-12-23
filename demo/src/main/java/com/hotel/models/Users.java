package com.hotel.models;

import com.hotel.models.enums.UsersType;

public class Users {
    private int id;
    private String name;
    private String password;
    private UsersType type;

    public Users(String name, String password) {
        this.name = name;
        this.password = password;
        this.type = UsersType.DEFAULT;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public UsersType getType() { return type; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPass(String password) { this.password = password; }
    public void setType(UsersType type) { this.type = type; }


    @Override
    public String toString() {
        return name;
    }
}
