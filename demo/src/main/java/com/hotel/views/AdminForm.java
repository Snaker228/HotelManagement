package com.hotel.views;

import com.hotel.controllers.AdminController;
import com.hotel.models.Booking;
import com.hotel.models.Guest;
import com.hotel.models.Room;
import com.hotel.models.Users;
import com.hotel.models.enums.RoomStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.util.Map;

public class AdminForm extends BorderPane {
    protected Users currentUser;
    protected Runnable logoutListener;
    
    private AdminController adminController;
    
    private TableView<Booking> bookingsTable;
    private TableView<Guest> guestsTable;
    private TableView<Room> roomsTable;
    
    private ObservableList<Booking> bookingsData;
    private ObservableList<Guest> guestsData;
    private ObservableList<Room> roomsData;
    
    // Элементы для статистики
    private Label revenueLabel;
    private Label totalBookingsLabel;
    private Label occupancyRateLabel;
    private Label avgBookingValueLabel;
    
    public AdminForm(Users user) {
        this.currentUser = user;
        this.adminController = new AdminController();
        initializeUI();
        loadData();
    }
    
    private void initializeUI() {
        initializeHeader();
        initializeContent();
    }
    
    private void initializeHeader() {
        VBox header = new VBox();
        header.setStyle("-fx-background-color: #2c3e50; -fx-padding: 10;");
        header.setSpacing(10);
        
        // Первая строка: информация о пользователе и статистика
        HBox topRow = new HBox();
        topRow.setSpacing(20);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        // Информация о пользователе
        Label userInfo = new Label("Администратор: " + currentUser.getName());
        userInfo.setTextFill(Color.WHITE);
        userInfo.setStyle("-fx-font-weight: bold;");
        
        // Статистика доходов
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER);
        
        revenueLabel = createStatLabel("Доход: 0 ₽", "#27ae60");
        totalBookingsLabel = createStatLabel("Бронирований: 0", "#3498db");
        occupancyRateLabel = createStatLabel("Загрузка: 0%", "#f39c12");
        avgBookingValueLabel = createStatLabel("Ср. чек: 0 ₽", "#9b59b6");
        
        statsBox.getChildren().addAll(revenueLabel, totalBookingsLabel, occupancyRateLabel, avgBookingValueLabel);
        
        // Кнопка выхода
        Button logoutButton = new Button("Выйти");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> {
            if (logoutListener != null) {
                logoutListener.run();
            }
        });
        
        topRow.getChildren().addAll(userInfo, statsBox, logoutButton);
        HBox.setHgrow(statsBox, Priority.ALWAYS);
        
        // Вторая строка: быстрые действия
        HBox quickActions = new HBox();
        quickActions.setStyle("-fx-background-color: #8e44ad; -fx-padding: 15;");
        quickActions.setSpacing(15);
        quickActions.setAlignment(Pos.CENTER);
        
        Button manageGuestsButton = createActionButton("Управление гостями", "#3498db");
        Button manageRoomsButton = createActionButton("Управление номерами", "#27ae60");
        Button manageBookingsButton = createActionButton("Управление бронированиями", "#f39c12");
        Button statisticsButton = createActionButton("Статистика", "#9b59b6");
        Button refreshButton = createActionButton("Обновить", "#e74c3c");
        
        manageGuestsButton.setOnAction(e -> showGuestsManagement());
        manageRoomsButton.setOnAction(e -> showRoomsManagement());
        manageBookingsButton.setOnAction(e -> showBookingsManagement());
        statisticsButton.setOnAction(e -> showStatistics());
        refreshButton.setOnAction(e -> loadData());
        
        quickActions.getChildren().addAll(manageGuestsButton, manageRoomsButton, 
                                         manageBookingsButton, statisticsButton, refreshButton);
        
        header.getChildren().addAll(topRow, quickActions);
        setTop(header);
    }
    
    private void initializeContent() {
        // Начальный экран с приветствием
        VBox welcomePanel = new VBox();
        welcomePanel.setPadding(new Insets(50));
        welcomePanel.setSpacing(20);
        welcomePanel.setAlignment(Pos.CENTER);
        
        Label welcomeLabel = new Label("Добро пожаловать в панель администратора!");
        welcomeLabel.setFont(Font.font(24));
        welcomeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label instructionLabel = new Label("Используйте кнопки выше для управления системой");
        instructionLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14;");
        
        welcomePanel.getChildren().addAll(welcomeLabel, instructionLabel);
        setCenter(welcomePanel);
    }
    
    private void showGuestsManagement() {
        VBox guestsPanel = new VBox();
        guestsPanel.setPadding(new Insets(15));
        guestsPanel.setSpacing(10);
        
        Label title = new Label("Управление гостями");
        title.setFont(Font.font(18));
        title.setStyle("-fx-font-weight: bold;");
        
        // Таблица гостей
        guestsTable = new TableView<>();
        setupGuestsTable();
        
        // Кнопки управления
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);
        
        Button addGuestButton = new Button("Добавить гостя");
        Button editGuestButton = new Button("Редактировать");
        Button deleteGuestButton = new Button("Удалить");
        
        addGuestButton.setOnAction(e -> showAddGuestDialog());
        editGuestButton.setOnAction(e -> editSelectedGuest());
        deleteGuestButton.setOnAction(e -> deleteSelectedGuest());
        
        buttonsBox.getChildren().addAll(addGuestButton, editGuestButton, deleteGuestButton);
        
        guestsPanel.getChildren().addAll(title, guestsTable, buttonsBox);
        setCenter(guestsPanel);
        
        loadGuestsData();
    }
    
    private void showRoomsManagement() {
        VBox roomsPanel = new VBox();
        roomsPanel.setPadding(new Insets(15));
        roomsPanel.setSpacing(10);
        
        Label title = new Label("Управление номерами");
        title.setFont(Font.font(18));
        title.setStyle("-fx-font-weight: bold;");
        
        // Таблица номеров
        roomsTable = new TableView<>();
        setupRoomsTable();
        
        // Кнопки управления
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);
        
        Button addRoomButton = new Button("Добавить номер");
        Button editRoomButton = new Button("Редактировать");
        Button deleteRoomButton = new Button("Удалить");
        
        addRoomButton.setOnAction(e -> showAddRoomDialog());
        editRoomButton.setOnAction(e -> editSelectedRoom());
        deleteRoomButton.setOnAction(e -> deleteSelectedRoom());
        
        buttonsBox.getChildren().addAll(addRoomButton, editRoomButton, deleteRoomButton);
        
        roomsPanel.getChildren().addAll(title, roomsTable, buttonsBox);
        setCenter(roomsPanel);
        
        loadRoomsData();
    }
    
    private void showBookingsManagement() {
        VBox bookingsPanel = new VBox();
        bookingsPanel.setPadding(new Insets(15));
        bookingsPanel.setSpacing(10);
        
        Label title = new Label("Управление бронированиями");
        title.setFont(Font.font(18));
        title.setStyle("-fx-font-weight: bold;");
        
        // Таблица бронирований
        bookingsTable = new TableView<>();
        setupBookingsTable();
        
        // Кнопки управления
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);
        
        Button addBookingButton = new Button("Новое бронирование");
        Button editBookingButton = new Button("Редактировать");
        Button cancelBookingButton = new Button("Отменить");
        
        addBookingButton.setOnAction(e -> showNewBookingDialog());
        editBookingButton.setOnAction(e -> editSelectedBooking());
        cancelBookingButton.setOnAction(e -> cancelSelectedBooking());
        
        buttonsBox.getChildren().addAll(addBookingButton, editBookingButton, cancelBookingButton);
        
        bookingsPanel.getChildren().addAll(title, bookingsTable, buttonsBox);
        setCenter(bookingsPanel);
        
        loadBookingsData();
    }
    
    private void showStatistics() {
        VBox statsPanel = new VBox();
        statsPanel.setPadding(new Insets(20));
        statsPanel.setSpacing(15);
        
        Label title = new Label("Статистика отеля");
        title.setFont(Font.font(20));
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Статистика доходов
        Map<String, Object> revenueStats = adminController.getRevenueStatistics();
        Map<String, Object> occupancyStats = adminController.getOccupancyStatistics();
        
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(10);
        statsGrid.setPadding(new Insets(15));
        statsGrid.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 5;");
        
        statsGrid.add(new Label("Общий доход:"), 0, 0);
        statsGrid.add(new Label(String.format("%.2f ₽", revenueStats.get("totalRevenue"))), 1, 0);
        
        statsGrid.add(new Label("Всего бронирований:"), 0, 1);
        statsGrid.add(new Label(String.valueOf(revenueStats.get("totalBookings"))), 1, 1);
        
        statsGrid.add(new Label("Средний чек:"), 0, 2);
        statsGrid.add(new Label(String.format("%.2f ₽", revenueStats.get("avgBookingValue"))), 1, 2);
        
        statsGrid.add(new Label("Загрузка номеров:"), 0, 3);
        statsGrid.add(new Label(String.format("%.1f%%", occupancyStats.get("occupancyRate"))), 1, 3);
        
        statsGrid.add(new Label("Всего номеров:"), 2, 0);
        statsGrid.add(new Label(String.valueOf(occupancyStats.get("totalRooms"))), 3, 0);
        
        statsGrid.add(new Label("Занято номеров:"), 2, 1);
        statsGrid.add(new Label(String.valueOf(occupancyStats.get("occupiedRooms"))), 3, 1);
        
        statsGrid.add(new Label("Свободно номеров:"), 2, 2);
        statsGrid.add(new Label(String.valueOf(occupancyStats.get("availableRooms"))), 3, 2);
        
        // Месячная статистика
        Label monthlyTitle = new Label("Доход по месяцам:");
        monthlyTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        
        TextArea monthlyStatsArea = new TextArea();
        monthlyStatsArea.setPrefHeight(200);
        monthlyStatsArea.setEditable(false);
        
        Map<String, Double> monthlyRevenue = adminController.getMonthlyRevenue();
        StringBuilder monthlyStats = new StringBuilder();
        for (Map.Entry<String, Double> entry : monthlyRevenue.entrySet()) {
            monthlyStats.append(entry.getKey()).append(": ").append(String.format("%.2f ₽", entry.getValue())).append("\n");
        }
        
        if (monthlyStats.length() == 0) {
            monthlyStats.append("Нет данных за предыдущие месяцы");
        }
        monthlyStatsArea.setText(monthlyStats.toString());
        
        statsPanel.getChildren().addAll(title, statsGrid, monthlyTitle, monthlyStatsArea);
        setCenter(statsPanel);
    }
    
    @SuppressWarnings("unchecked")
    private void setupGuestsTable() {
        guestsTable.getColumns().clear();
        
        TableColumn<Guest, String> firstNameCol = new TableColumn<>("Имя");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        TableColumn<Guest, String> lastNameCol = new TableColumn<>("Фамилия");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        
        TableColumn<Guest, String> phoneCol = new TableColumn<>("Телефон");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        guestsTable.getColumns().addAll(firstNameCol, lastNameCol, phoneCol);
        guestsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }
    
    @SuppressWarnings("unchecked")
    private void setupRoomsTable() {
        roomsTable.getColumns().clear();
        
        TableColumn<Room, String> numberCol = new TableColumn<>("Номер");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        
        TableColumn<Room, String> priceCol = new TableColumn<>("Цена за ночь");
        priceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPricePerNight() + " ₽"));
        
        TableColumn<Room, String> capacityCol = new TableColumn<>("Вместимость");
        capacityCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCapacity() + " чел."));
        
        TableColumn<Room, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));
        
        roomsTable.getColumns().addAll(numberCol, priceCol, capacityCol, statusCol);
        roomsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }
    
    @SuppressWarnings("unchecked")
    private void setupBookingsTable() {
        bookingsTable.getColumns().clear();
        
        TableColumn<Booking, String> guestCol = new TableColumn<>("Гость");
        guestCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGuest().getFullName()));
        
        TableColumn<Booking, String> roomCol = new TableColumn<>("Номер");
        roomCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRoom().getNumber()));
        
        TableColumn<Booking, String> checkInCol = new TableColumn<>("Заезд");
        checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        
        TableColumn<Booking, String> checkOutCol = new TableColumn<>("Выезд");
        checkOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        
        TableColumn<Booking, String> priceCol = new TableColumn<>("Стоимость");
        priceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTotalPrice() + " ₽"));
        
        bookingsTable.getColumns().addAll(guestCol, roomCol, checkInCol, checkOutCol, priceCol);
        bookingsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }
    
    // Методы для работы с гостями
    private void showAddGuestDialog() {
        Dialog<Guest> dialog = new Dialog<>();
        dialog.setTitle("Добавление гостя");
        dialog.setHeaderText("Введите данные нового гостя");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Имя");
        
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Фамилия");
        
        TextField phoneField = new TextField();
        phoneField.setPromptText("Телефон");
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        grid.add(new Label("Имя:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Фамилия:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Телефон:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(errorLabel, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType createButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty()) {
                    errorLabel.setText("Заполните имя и фамилию");
                    return null;
                }
                
                if (phoneField.getText().isEmpty()) {
                    errorLabel.setText("Введите номер телефона");
                    return null;
                }
                
                try {
                    Guest guest = new Guest(
                        firstNameField.getText(),
                        lastNameField.getText(),
                        phoneField.getText()
                    );
                    return adminController.addGuest(guest);
                } catch (Exception e) {
                    errorLabel.setText(e.getMessage());
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(guest -> {
            if (guest != null) {
                showAlert("Успех", "Гость добавлен успешно!", Alert.AlertType.INFORMATION);
                loadGuestsData();
            }
        });
    }
    
    private void editSelectedGuest() {
        Guest selected = guestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите гостя для редактирования", Alert.AlertType.WARNING);
            return;
        }
        
        Dialog<Guest> dialog = new Dialog<>();
        dialog.setTitle("Редактирование гостя");
        dialog.setHeaderText("Редактирование данных гостя");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField firstNameField = new TextField(selected.getFirstName());
        TextField lastNameField = new TextField(selected.getLastName());
        TextField phoneField = new TextField(selected.getPhone());
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        grid.add(new Label("Имя:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Фамилия:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Телефон:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(errorLabel, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty()) {
                    errorLabel.setText("Заполните имя и фамилию");
                    return null;
                }
                
                if (phoneField.getText().isEmpty()) {
                    errorLabel.setText("Введите номер телефона");
                    return null;
                }
                
                try {
                    selected.setFirstName(firstNameField.getText());
                    selected.setLastName(lastNameField.getText());
                    selected.setPhone(phoneField.getText());
                    
                    if (adminController.updateGuest(selected)) {
                        return selected;
                    } else {
                        errorLabel.setText("Ошибка при обновлении");
                    }
                } catch (Exception e) {
                    errorLabel.setText(e.getMessage());
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(guest -> {
            if (guest != null) {
                showAlert("Успех", "Данные гостя обновлены!", Alert.AlertType.INFORMATION);
                loadGuestsData();
            }
        });
    }
    
    private void deleteSelectedGuest() {
        Guest selected = guestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите гостя для удаления", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удаление гостя");
        confirm.setContentText("Вы уверены, что хотите удалить гостя " + selected.getFullName() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (adminController.deleteGuest(selected.getId())) {
                    showAlert("Успех", "Гость удален успешно", Alert.AlertType.INFORMATION);
                    loadGuestsData();
                } else {
                    showAlert("Ошибка", "Не удалось удалить гостя", Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    // Методы для работы с номерами
    private void showAddRoomDialog() {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle("Добавление номера");
        dialog.setHeaderText("Введите данные нового номера");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField numberField = new TextField();
        numberField.setPromptText("Номер");
        
        TextField priceField = new TextField();
        priceField.setPromptText("Цена за ночь");
        
        TextField capacityField = new TextField();
        capacityField.setPromptText("Вместимость");
        
        ComboBox<RoomStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(RoomStatus.values());
        statusCombo.setValue(RoomStatus.AVAILABLE);
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        grid.add(new Label("Номер:"), 0, 0);
        grid.add(numberField, 1, 0);
        grid.add(new Label("Цена за ночь:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Вместимость:"), 0, 2);
        grid.add(capacityField, 1, 2);
        grid.add(new Label("Статус:"), 0, 3);
        grid.add(statusCombo, 1, 3);
        grid.add(errorLabel, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType createButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    if (numberField.getText().isEmpty()) {
                        errorLabel.setText("Введите номер комнаты");
                        return null;
                    }
                    
                    double price = Double.parseDouble(priceField.getText());
                    int capacity = Integer.parseInt(capacityField.getText());
                    
                    Room room = new Room(numberField.getText(), price, capacity);
                    room.setStatus(statusCombo.getValue());
                    
                    return adminController.addRoom(room);
                } catch (NumberFormatException e) {
                    errorLabel.setText("Введите корректные числовые значения");
                } catch (Exception e) {
                    errorLabel.setText(e.getMessage());
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(room -> {
            if (room != null) {
                showAlert("Успех", "Номер добавлен успешно!", Alert.AlertType.INFORMATION);
                loadRoomsData();
            }
        });
    }
    
    private void editSelectedRoom() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите номер для редактирования", Alert.AlertType.WARNING);
            return;
        }
        
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle("Редактирование номера");
        dialog.setHeaderText("Редактирование данных номера");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField numberField = new TextField(selected.getNumber());
        TextField priceField = new TextField(String.valueOf(selected.getPricePerNight()));
        TextField capacityField = new TextField(String.valueOf(selected.getCapacity()));
        
        ComboBox<RoomStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(RoomStatus.values());
        statusCombo.setValue(selected.getStatus());
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        grid.add(new Label("Номер:"), 0, 0);
        grid.add(numberField, 1, 0);
        grid.add(new Label("Цена за ночь:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Вместимость:"), 0, 2);
        grid.add(capacityField, 1, 2);
        grid.add(new Label("Статус:"), 0, 3);
        grid.add(statusCombo, 1, 3);
        grid.add(errorLabel, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    if (numberField.getText().isEmpty()) {
                        errorLabel.setText("Введите номер комнаты");
                        return null;
                    }
                    
                    double price = Double.parseDouble(priceField.getText());
                    int capacity = Integer.parseInt(capacityField.getText());
                    
                    // Проверяем уникальность номера (если изменился)
                    if (!selected.getNumber().equals(numberField.getText()) && 
                        adminController.isRoomNumberExists(numberField.getText())) {
                        errorLabel.setText("Номер с таким названием уже существует");
                        return null;
                    }
                    
                    selected.setNumber(numberField.getText());
                    selected.setPricePerNight(price);
                    selected.setCapacity(capacity);
                    selected.setStatus(statusCombo.getValue());
                    
                    if (adminController.updateRoom(selected)) {
                        return selected;
                    } else {
                        errorLabel.setText("Ошибка при обновлении");
                    }
                } catch (NumberFormatException e) {
                    errorLabel.setText("Введите корректные числовые значения");
                } catch (Exception e) {
                    errorLabel.setText(e.getMessage());
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(room -> {
            if (room != null) {
                showAlert("Успех", "Данные номера обновлены!", Alert.AlertType.INFORMATION);
                loadRoomsData();
            }
        });
    }
    
    private void deleteSelectedRoom() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите номер для удаления", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удаление номера");
        confirm.setContentText("Вы уверены, что хотите удалить номер " + selected.getNumber() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (adminController.deleteRoom(selected.getId())) {
                        showAlert("Успех", "Номер удален успешно", Alert.AlertType.INFORMATION);
                        loadRoomsData();
                    } else {
                        showAlert("Ошибка", "Не удалось удалить номер", Alert.AlertType.ERROR);
                    }
                } catch (Exception e) {
                    showAlert("Ошибка", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    // Методы для работы с бронированиями
    private void showNewBookingDialog() {
        // Реализация аналогична ReceptionForm
        showAlert("Информация", "Функция добавления бронирования", Alert.AlertType.INFORMATION);
    }
    
    private void editSelectedBooking() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите бронирование для редактирования", Alert.AlertType.WARNING);
            return;
        }
        
        showAlert("Информация", "Функция редактирования бронирования", Alert.AlertType.INFORMATION);
    }
    
    private void cancelSelectedBooking() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите бронирование для отмены", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение отмены");
        confirm.setHeaderText("Отмена бронирования");
        confirm.setContentText("Вы уверены, что хотите отменить бронирование для " + 
                              selected.getGuest().getFullName() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (adminController.cancelBooking(selected.getId())) {
                    showAlert("Успех", "Бронирование отменено", Alert.AlertType.INFORMATION);
                    loadBookingsData();
                } else {
                    showAlert("Ошибка", "Не удалось отменить бронирование", Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    // Методы загрузки данных
    private void loadData() {
        loadGuestsData();
        loadRoomsData();
        loadBookingsData();
        updateStatistics();
    }
    
    private void loadGuestsData() {
        if (guestsTable != null) {
            guestsData = FXCollections.observableArrayList(adminController.getAllGuests());
            guestsTable.setItems(guestsData);
        }
    }
    
    private void loadRoomsData() {
        if (roomsTable != null) {
            roomsData = FXCollections.observableArrayList(adminController.getAllRooms());
            roomsTable.setItems(roomsData);
        }
    }
    
    private void loadBookingsData() {
        if (bookingsTable != null) {
            bookingsData = FXCollections.observableArrayList(adminController.getAllBookings());
            bookingsTable.setItems(bookingsData);
        }
    }
    
    private void updateStatistics() {
        Map<String, Object> revenueStats = adminController.getRevenueStatistics();
        Map<String, Object> occupancyStats = adminController.getOccupancyStatistics();
        
        revenueLabel.setText(String.format("Доход: %.2f ₽", revenueStats.get("totalRevenue")));
        totalBookingsLabel.setText("Бронирований: " + revenueStats.get("totalBookings"));
        occupancyRateLabel.setText(String.format("Загрузка: %.1f%%", occupancyStats.get("occupancyRate")));
        avgBookingValueLabel.setText(String.format("Ср. чек: %.2f ₽", revenueStats.get("avgBookingValue")));
    }
    
    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefSize(160, 40);
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold;",
            color
        ));
        return button;
    }
    
    private Label createStatLabel(String text, String color) {
        Label label = new Label(text);
        label.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-font-weight: bold; -fx-border-radius: 3px;",
            color
        ));
        return label;
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void setLogoutListener(Runnable listener) {
        this.logoutListener = listener;
    }
}