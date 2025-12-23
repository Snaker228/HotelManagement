module com.hotel {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    
    opens com.hotel to javafx.fxml;
    opens com.hotel.models to javafx.base;
    opens com.hotel.controllers to javafx.fxml;
    opens com.hotel.views to javafx.fxml;
    
    exports com.hotel;
    exports com.hotel.models;
    exports com.hotel.controllers;
    exports com.hotel.views;
}