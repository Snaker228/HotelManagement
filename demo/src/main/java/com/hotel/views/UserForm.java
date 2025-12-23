package com.hotel.views;

import com.hotel.models.Users;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public abstract class UserForm extends BorderPane {
    protected Users currentUser;
    protected Runnable logoutListener;
    
    public UserForm(Users user) {
        this.currentUser = user;
        initializeHeader();
        initializeContent();
    }
    
    private void initializeHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #2c3e50; -fx-padding: 10;");
        header.setSpacing(20);
        
        Label userInfo = new Label("Пользователь: " + currentUser.getName() + 
                                " (" + currentUser.getType().getDisplayName() + ")");
        userInfo.setTextFill(Color.WHITE);
        userInfo.setStyle("-fx-font-weight: bold;");
        
        Button logoutButton = new Button("Выйти");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> {
            if (logoutListener != null) {
                logoutListener.run();
            }
        });
        
        header.getChildren().addAll(userInfo, logoutButton);
        setTop(header);
    }
    
    protected abstract void initializeContent();
    
    public void setLogoutListener(Runnable listener) {
        this.logoutListener = listener;
    }
}