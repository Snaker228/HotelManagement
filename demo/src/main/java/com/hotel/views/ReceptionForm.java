package com.hotel.views;

import com.hotel.controllers.BookingController;
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
import java.util.List;

public class ReceptionForm extends BorderPane {
    protected Users currentUser;
    protected Runnable logoutListener;
    
    private BookingController bookingController;
    
    private TableView<Booking> bookingsTable;
    private TableView<Guest> guestsTable;
    private TableView<Room> roomsTable;
    private Button newBookingButton;
    private Button newGuestButton;
    private Button cancelBookingButton;
    private Button refreshButton;
    
    private ObservableList<Booking> bookingsData;
    private ObservableList<Guest> guestsData;
    private ObservableList<Room> roomsData;
    
    // Элементы для статистики
    private Label totalRoomsLabel;
    private Label availableRoomsLabel;
    private Label occupiedRoomsLabel;
    
    public ReceptionForm(Users user) {
        this.currentUser = user;
        this.bookingController = new BookingController();
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
        Label userInfo = new Label("Пользователь: " + currentUser.getName() + 
                                 " (" + currentUser.getType().getDisplayName() + ")");
        userInfo.setTextFill(Color.WHITE);
        userInfo.setStyle("-fx-font-weight: bold;");
        
        // Статистика номеров
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER);
        
        totalRoomsLabel = createStatLabel("Всего: 0", "#3498db");
        availableRoomsLabel = createStatLabel("Свободно: 0", "#27ae60");
        occupiedRoomsLabel = createStatLabel("Занято: 0", "#e74c3c");
        
        statsBox.getChildren().addAll(totalRoomsLabel, availableRoomsLabel, occupiedRoomsLabel);
        
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
        quickActions.setStyle("-fx-background-color: #3498db; -fx-padding: 15;");
        quickActions.setSpacing(15);
        quickActions.setAlignment(Pos.CENTER);
        
        newBookingButton = createActionButton("Новое бронирование", "#27ae60");
        newGuestButton = createActionButton("Добавить гостя", "#f39c12");
        cancelBookingButton = createActionButton("Отменить бронь", "#e74c3c");
        refreshButton = createActionButton("Обновить", "#9b59b6");
        
        newBookingButton.setOnAction(e -> showNewBookingDialog());
        newGuestButton.setOnAction(e -> showNewGuestDialog());
        cancelBookingButton.setOnAction(e -> cancelSelectedBooking());
        refreshButton.setOnAction(e -> loadData());
        
        quickActions.getChildren().addAll(newBookingButton, newGuestButton, cancelBookingButton, refreshButton);
        
        header.getChildren().addAll(topRow, quickActions);
        setTop(header);
    }
    
    private void initializeContent() {
        TabPane tabPane = new TabPane();
        
        // Вкладка с текущими бронированиями
        Tab currentBookingsTab = new Tab("Текущие бронирования");
        currentBookingsTab.setClosable(false);
        currentBookingsTab.setContent(createBookingsTab());
        
        // Вкладка с номерами
        Tab roomsTab = new Tab("Номера отеля");
        roomsTab.setClosable(false);
        roomsTab.setContent(createRoomsTab());
        
        // Вкладка с гостями
        Tab guestsTab = new Tab("Гости");
        guestsTab.setClosable(false);
        guestsTab.setContent(createGuestsTab());
        
        tabPane.getTabs().addAll(currentBookingsTab, roomsTab, guestsTab);
        setCenter(tabPane);
    }
    
    private VBox createBookingsTab() {
        VBox bookingsTab = new VBox();
        bookingsTab.setPadding(new Insets(15));
        bookingsTab.setSpacing(10);
        
        Label title = new Label("Текущие бронирования");
        title.setFont(Font.font(16));
        title.setStyle("-fx-font-weight: bold;");
        
        // Таблица бронирований
        bookingsTable = new TableView<>();
        setupBookingsTable();
        
        bookingsTab.getChildren().addAll(title, bookingsTable);
        return bookingsTab;
    }
    
    private VBox createRoomsTab() {
        VBox roomsTab = new VBox();
        roomsTab.setPadding(new Insets(15));
        roomsTab.setSpacing(10);
        
        Label title = new Label("Все номера отеля");
        title.setFont(Font.font(16));
        title.setStyle("-fx-font-weight: bold;");
        
        // Таблица номеров
        roomsTable = new TableView<>();
        setupRoomsTable();
        
        // Фильтры для таблицы
        HBox filtersBox = new HBox(10);
        filtersBox.setAlignment(Pos.CENTER_LEFT);
        
        Button allRoomsButton = new Button("Все номера");
        Button availableRoomsButton = new Button("Только свободные");
        Button occupiedRoomsButton = new Button("Только занятые");
        
        allRoomsButton.setOnAction(e -> showAllRooms());
        availableRoomsButton.setOnAction(e -> showAvailableRooms());
        occupiedRoomsButton.setOnAction(e -> showOccupiedRooms());
        
        filtersBox.getChildren().addAll(allRoomsButton, availableRoomsButton, occupiedRoomsButton);
        
        roomsTab.getChildren().addAll(title, filtersBox, roomsTable);
        return roomsTab;
    }
    
    private VBox createGuestsTab() {
        VBox guestsTab = new VBox();
        guestsTab.setPadding(new Insets(15));
        guestsTab.setSpacing(10);
        
        Label title = new Label("Зарегистрированные гости");
        title.setFont(Font.font(16));
        title.setStyle("-fx-font-weight: bold;");
        
        // Таблица гостей
        guestsTable = new TableView<>();
        setupGuestsTable();
        
        guestsTab.getChildren().addAll(title, guestsTable);
        return guestsTab;
    }
    
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
        
        TableColumn<Booking, String> nightsCol = new TableColumn<>("Ночей");
        nightsCol.setCellValueFactory(cellData -> {
            long nights = cellData.getValue().getCheckOutDate().toEpochDay() - 
                         cellData.getValue().getCheckInDate().toEpochDay();
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(nights));
        });
        
        bookingsTable.getColumns().addAll(guestCol, roomCol, checkInCol, checkOutCol, nightsCol, priceCol);
        
        // Автоматическое растяжение колонок для JavaFX 25
        guestCol.prefWidthProperty().bind(bookingsTable.widthProperty().multiply(0.25));
        roomCol.prefWidthProperty().bind(bookingsTable.widthProperty().multiply(0.10));
        checkInCol.prefWidthProperty().bind(bookingsTable.widthProperty().multiply(0.15));
        checkOutCol.prefWidthProperty().bind(bookingsTable.widthProperty().multiply(0.15));
        nightsCol.prefWidthProperty().bind(bookingsTable.widthProperty().multiply(0.10));
        priceCol.prefWidthProperty().bind(bookingsTable.widthProperty().multiply(0.25));
    }
    
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
        
        // Раскрашиваем статусы
        statusCol.setCellFactory(column -> new TableCell<Room, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Свободен")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        roomsTable.getColumns().addAll(numberCol, priceCol, capacityCol, statusCol);
        roomsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void setupGuestsTable() {
        guestsTable.getColumns().clear();
        
        TableColumn<Guest, String> firstNameCol = new TableColumn<>("Имя");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        TableColumn<Guest, String> lastNameCol = new TableColumn<>("Фамилия");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        
        TableColumn<Guest, String> phoneCol = new TableColumn<>("Телефон");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        guestsTable.getColumns().addAll(firstNameCol, lastNameCol, phoneCol);
        guestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void loadData() {
        // Загрузка бронирований
        bookingsData = FXCollections.observableArrayList(bookingController.getCurrentBookings());
        bookingsTable.setItems(bookingsData);
        
        // Загрузка гостей
        guestsData = FXCollections.observableArrayList(bookingController.getAllGuests());
        guestsTable.setItems(guestsData);
        
        // Загрузка всех номеров
        roomsData = FXCollections.observableArrayList(bookingController.getAllRooms());
        roomsTable.setItems(roomsData);
        
        // Обновление статистики
        updateStatistics();
    }
    
    private void updateStatistics() {
        totalRoomsLabel.setText("Всего: " + bookingController.getTotalRoomsCount());
        availableRoomsLabel.setText("Свободно: " + bookingController.getAvailableRoomsCount());
        occupiedRoomsLabel.setText("Занято: " + bookingController.getOccupiedRoomsCount());
    }
    
    private void showAllRooms() {
        roomsData = FXCollections.observableArrayList(bookingController.getAllRooms());
        roomsTable.setItems(roomsData);
    }
    
    private void showAvailableRooms() {
        roomsData = FXCollections.observableArrayList(bookingController.getAvailableRooms());
        roomsTable.setItems(roomsData);
    }
    
    private void showOccupiedRooms() {
        roomsData = FXCollections.observableArrayList(bookingController.getOccupiedRooms());
        roomsTable.setItems(roomsData);
    }
    
    private void showNewBookingDialog() {
        Dialog<Booking> dialog = new Dialog<>();
        dialog.setTitle("Новое бронирование");
        dialog.setHeaderText("Создание нового бронирования");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Guest> guestCombo = new ComboBox<>();
        guestCombo.setItems(FXCollections.observableArrayList(bookingController.getAllGuests()));
        guestCombo.setPromptText("Выберите гостя");

        ComboBox<Room> roomCombo = new ComboBox<>();
        roomCombo.setPromptText("Выберите номер");

        DatePicker checkInPicker = new DatePicker();
        checkInPicker.setValue(LocalDate.now());

        DatePicker checkOutPicker = new DatePicker();
        checkOutPicker.setValue(LocalDate.now().plusDays(1));

        // Информация о выбранном номере
        Label roomInfoLabel = new Label();
        roomInfoLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        // Обновляем список номеров при изменении дат
        Runnable updateAvailableRooms = () -> {
            if (checkInPicker.getValue() != null && checkOutPicker.getValue() != null) {
                try {
                    List<Room> availableRooms = bookingController.getAvailableRoomsForPeriod(
                        checkInPicker.getValue(), 
                        checkOutPicker.getValue()
                    );
                    roomCombo.setItems(FXCollections.observableArrayList(availableRooms));
                    
                    if (availableRooms.isEmpty()) {
                        roomInfoLabel.setText("Нет доступных номеров на выбранные даты");
                    } else {
                        roomInfoLabel.setText("Доступно номеров: " + availableRooms.size());
                    }
                } catch (Exception e) {
                    roomInfoLabel.setText("Ошибка: " + e.getMessage());
                }
            }
        };

        checkInPicker.setOnAction(e -> updateAvailableRooms.run());
        checkOutPicker.setOnAction(e -> updateAvailableRooms.run());

        roomCombo.setOnAction(e -> {
            Room selectedRoom = roomCombo.getValue();
            if (selectedRoom != null) {
                roomInfoLabel.setText("Номер " + selectedRoom.getNumber() + 
                    " | " + selectedRoom.getPricePerNight() + " ₽/ночь | " + 
                    selectedRoom.getCapacity() + " чел.");
            } else {
                roomInfoLabel.setText("");
            }
        });

        // Инициализируем список номеров
        updateAvailableRooms.run();

        grid.add(new Label("Гость:"), 0, 0);
        grid.add(guestCombo, 1, 0);
        grid.add(new Label("Дата заезда:"), 0, 1);
        grid.add(checkInPicker, 1, 1);
        grid.add(new Label("Дата выезда:"), 0, 2);
        grid.add(checkOutPicker, 1, 2);
        grid.add(new Label("Номер:"), 0, 3);
        grid.add(roomCombo, 1, 3);
        grid.add(roomInfoLabel, 1, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType createButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    return bookingController.createBooking(
                        guestCombo.getValue(),
                        roomCombo.getValue(),
                        checkInPicker.getValue(),
                        checkOutPicker.getValue()
                    );
                } catch (Exception e) {
                    showAlert("Ошибка", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(booking -> {
            if (booking != null) {
                showAlert("Успех", "Бронирование создано успешно!", Alert.AlertType.INFORMATION);
                loadData();
            }
        });
    }
    
    private void showNewGuestDialog() {
        Dialog<Guest> dialog = new Dialog<>();
        dialog.setTitle("Новый гость");
        dialog.setHeaderText("Добавление нового гостя");
        
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
        
        // Label для отображения ошибок
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
                
                // Проверяем уникальность телефона
                if (bookingController.isPhoneExists(phoneField.getText())) {
                    errorLabel.setText("Гость с таким номером телефона уже существует");
                    return null;
                }
                
                Guest guest = new Guest(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    phoneField.getText()
                );
                return bookingController.addGuest(guest);
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(guest -> {
            if (guest != null) {
                showAlert("Успех", "Гость добавлен успешно!", Alert.AlertType.INFORMATION);
                loadData();
            }
        });
    }
    
    private void cancelSelectedBooking() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите бронирование для отмены", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Отмена бронирования");
        confirm.setContentText("Вы уверены, что хотите отменить бронирование для " +
                            selected.getGuest().getFullName() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (bookingController.cancelBooking(selected.getId())) {
                    showAlert("Успех", "Бронирование отменено", Alert.AlertType.INFORMATION);
                    loadData();
                } else {
                    showAlert("Ошибка", "Не удалось отменить бронирование", Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefSize(180, 50);
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 12; -fx-font-weight: bold;",
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