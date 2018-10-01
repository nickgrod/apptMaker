/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentsystem;

import static appointmentsystem.DBConnection.conn;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 *
 * @author nickg
 */
public class LoginLandingController implements Initializable {

    @FXML
    private Button exitButton;
    @FXML
    private Button newAppointmentButton;
    @FXML
    private Button editAppointmentButton;
    @FXML
    private Button deleteAppointmentButton;
    @FXML
    private Button viewCustomerButton;
    @FXML
    private Button viewAllCustomersButton;
    @FXML
    private Button addNewCustomerButton;
    @FXML
    private ListView<Appointment> appointmentList;
    @FXML
    private Label nowViewingLabel;

    @FXML
    private ComboBox<String> monthBox;
    @FXML
    private ComboBox<String> weekBox;
    @FXML
    private TableView<Appointment> appointmentTable;
    @FXML
    private TableColumn<Appointment, Date> appointmentDateColumn;
    @FXML
    private TableColumn<Appointment, String> appointmentDescriptionColumn;

    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();

    private static int userIDInternal;
    private static String userNameInternal;

    public void initData(int userID, String userName) {
        userIDInternal = userID;
        userNameInternal = userName;

    }

    //exits the program on click of the "exit" button
    public void leaveProgram() {
        System.exit(0);
    }

    //checks for the locale and sets the language accordingly
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        appointmentDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        appointmentDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        appointments = getAppointments();
        appointmentTable.getItems().setAll(appointments);
        monthBox.getItems().addAll("All", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
        weekBox.getItems().addAll("Show All", "This Week", "Next Week");
        nowViewingLabel.setText("Now Viewing: All Upcoming");

    }

    public ObservableList<Appointment> getAppointments() {

// prepares the calendar and date to be converted for the sql query        
        java.util.Date utilDate = new java.util.Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(utilDate);
        cal.set(Calendar.MILLISECOND, 0);
        Timestamp time = new java.sql.Timestamp(cal.getTimeInMillis());

        try {

            String query = "SELECT * from appointment WHERE start >= UNIX_TIMESTAMP('" + time + "') ORDER BY start";
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            while (result.next()) {
                Appointment temp = new Appointment();
                temp.setId(result.getInt("appointmentid"));
                temp.setCustomerID(result.getInt("customerId"));
                temp.setDescription(result.getString("description"));
                temp.setStartDate(result.getDate("start"));
                appointments.add(temp);
            }
            try {
                result.close();
            } catch (SQLException e) {
                System.out.println("Couldn't close result set, " + e);
            }
            try {
                stmt.close();
            } catch (SQLException e) {
                System.out.println("Couldn't close statement, " + e);
            }
            return appointments;
        } catch (SQLException e) {
            System.out.println("ERROR: " + e);
        }
        return null;

    }

    //opens the new appointment window
    public void addAppointment(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("AppointmentScreen.fxml"));
            Parent userWindow = loader.load();

            Scene scene = new Scene(userWindow);

            AppointmentScreenController controller = loader.getController();
            int passedID = userIDInternal;
            String passedName = userNameInternal;
            controller.initData(passedID, passedName);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
            window.setTitle("Add New Appointment");
        } catch (NullPointerException e) {
            System.out.println(e);
        }
    }

    //takes to the new user screen with no edit
    public void addUser(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("CustomerScreen.fxml"));
            Parent userWindow = loader.load();

            Scene scene = new Scene(userWindow);

            CustomerScreenController controller = loader.getController();
            int passedID = userIDInternal;
            String passedName = userNameInternal;
            controller.initData(passedID, passedName);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
            window.setTitle("Add New Customer");
        } catch (NullPointerException e) {
            System.out.println(e);
        }
    }

    //takes to customer screen with prefilled fields ready to be edited
    public void editCustomer(ActionEvent event) throws IOException {
        try {
            Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
            int customerID = selected.getCustomerID();
            openCustomerScreenEdit(event, customerID);

        } catch (NullPointerException e) {
            warningMaker("No selected appointment.", "No appointment selected.", "Please select an appointment.");
        }

    }

    public void printHello(ActionEvent event) throws IOException {
        System.out.println("hello");

    }

    public void changeMonth(ActionEvent event) throws IOException {

        try {
            String month = monthBox.getSelectionModel().getSelectedItem();
            int val;
            switch (month) {
                case "All":
                    val = 1000;
                    break;
                case "January":
                    val = 1;
                    break;
                case "February":
                    val = 2;
                    break;
                case "March":
                    val = 3;
                    break;
                case "April":
                    val = 4;
                    break;
                case "May":
                    val = 5;
                    break;
                case "June":
                    val = 6;
                    break;
                case "July":
                    val = 7;
                    break;
                case "August":
                    val = 8;
                    break;
                case "September":
                    val = 9;
                    break;
                case "October":
                    val = 10;
                    break;
                case "November":
                    val = 11;
                    break;
                case "December":
                    val = 12;
                    break;
                default:
                    return;
            }

            int year = Calendar.getInstance().get(Calendar.YEAR);
            int currMonth = 1 + Calendar.getInstance().get(Calendar.MONTH);
            int val2 = val + 1;
            if (val < currMonth) {
                year++;
            }
            int year2 = year;
            if (val == 12) {
                year2++;
                val2 = 1;
            }

            try {
                ObservableList<Appointment> appointments2 = FXCollections.observableArrayList();
                String query;
                if (val == 1000) {
                    java.util.Date utilDate = new java.util.Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(utilDate);
                    cal.set(Calendar.MILLISECOND, 0);
                    Timestamp time = new java.sql.Timestamp(cal.getTimeInMillis());
                    query = "SELECT * from appointment WHERE start >= UNIX_TIMESTAMP('" + time + "') ORDER BY start";
                    nowViewingLabel.setText("Now Viewing: All Upcoming");
                } else {
                    query = "SELECT * FROM appointment WHERE start BETWEEN '" + year + "-" + val + "-01' AND '" + year2 + "-" + val2 + "-01' ORDER BY start";
                    nowViewingLabel.setText("Now Viewing: " + month);

                }
                Statement stmt = conn.createStatement();
                ResultSet result = stmt.executeQuery(query);
                while (result.next()) {
                    Appointment temp = new Appointment();
                    temp.setId(result.getInt("appointmentid"));
                    temp.setCustomerID(result.getInt("customerId"));
                    temp.setDescription(result.getString("description"));
                    temp.setStartDate(result.getDate("start"));
                    appointments2.add(temp);
                }
                appointmentTable.getItems().clear();
                appointmentTable.getItems().setAll(appointments2);
                try {
                    result.close();
                } catch (SQLException e) {
                    System.out.println("Couldn't close result set, " + e);
                }
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.out.println("Couldn't close statement, " + e);
                }

            } catch (SQLException e) {
                System.out.println("Something went wrong: " + e);
            }

        } catch (NullPointerException ex) {
            System.out.println("Null pointer exception on the month box");
        }

    }

    public void changeWeek(ActionEvent event) throws IOException {

        try {
            String week = weekBox.getSelectionModel().getSelectedItem();
            String query = "";
            int code;
            switch (week) {
                case "Show All":
                    code = 1;
                    break;
                case "This Week":
                    code = 2;
                    break;
                case "Next Week":
                    code = 3;
                    break;
                default:
                    return;

            }
            ObservableList<Appointment> appointments2 = FXCollections.observableArrayList();
            if (code == 1) {
                java.util.Date utilDate = new java.util.Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(utilDate);
                cal.set(Calendar.MILLISECOND, 0);
                Timestamp time = new java.sql.Timestamp(cal.getTimeInMillis());
                query = "SELECT * from appointment WHERE start >= UNIX_TIMESTAMP('" + time + "') ORDER BY start;";
                nowViewingLabel.setText("Now Viewing: All Upcoming");
            }
            if (code == 2) {
                query = "SELECT * FROM appointment WHERE YEARWEEK(start, 1) = YEARWEEK(CURDATE(),1) ORDER BY start;";
                nowViewingLabel.setText("Now Viewing: This Week");
            }
            if (code == 3) {
                query = "SELECT * FROM appointment WHERE YEARWEEK(start, 1) = YEARWEEK(CURDATE() + INTERVAL 1 WEEK, 1) ORDER BY start;";
                nowViewingLabel.setText("Now Viewing: Next Week");
            }

            try {
                Statement stmt = conn.createStatement();
                ResultSet result = stmt.executeQuery(query);
                while (result.next()) {
                    Appointment temp = new Appointment();
                    temp.setId(result.getInt("appointmentid"));
                    temp.setCustomerID(result.getInt("customerId"));
                    temp.setDescription(result.getString("description"));
                    temp.setStartDate(result.getDate("start"));
                    appointments2.add(temp);
                }
                appointmentTable.getItems().clear();
                appointmentTable.getItems().setAll(appointments2);
                try {
                    result.close();
                } catch (SQLException e) {
                    System.out.println("Couldn't close result set, " + e);
                }
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.out.println("Couldn't close statement, " + e);
                }

            } catch (SQLException e) {
                System.out.println("Something went wrong: " + e);
            }

        } catch (NullPointerException e) {
            System.out.println("Week has thrown a null exception: " + e);
        }
    }

    //opens the customer screen with information needed to edit an existing customer
    public void openCustomerScreenEdit(ActionEvent event, int customerID) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("CustomerScreen.fxml"));
            Parent userWindow = loader.load();

            Scene scene = new Scene(userWindow);

            CustomerScreenController controller = loader.getController();
            int passedID = userIDInternal;
            String passedName = userNameInternal;
            controller.initData(passedID, passedName, customerID);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
            window.setTitle("Edit Customer");
        } catch (NullPointerException e) {
            System.out.println(e);
        }
    }

    //takes the selected appointment and sends it to the edit screen
    public void editAppointment(ActionEvent event) throws IOException {
        try {
            Appointment appt = appointmentTable.getSelectionModel().getSelectedItem();
            int apptID = appt.getId();
            openAppointmentScreenEdit(event, apptID);
        } catch (NullPointerException e) {
            warningMaker("Error", "No appointment selected.", "Please select an appointment to edit.");
        }
    }

    //opens appointment screen with pre-filled appointment to edit
    public void openAppointmentScreenEdit(ActionEvent event, int apptID) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("AppointmentScreen.fxml"));
            Parent userWindow = loader.load();

            Scene scene = new Scene(userWindow);

            AppointmentScreenController controller = loader.getController();
            int passedID = userIDInternal;
            String passedName = userNameInternal;
            controller.initData(passedID, passedName, apptID);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
            window.setTitle("Edit Appointment");
        } catch (NullPointerException e) {
            System.out.println(e);
        }
    }
    
    
       //opens the reports screen
    public void openReportsScreen(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("ReportsScreen.fxml"));
            Parent userWindow = loader.load();

            Scene scene = new Scene(userWindow);

            ReportsScreenController controller = loader.getController();
            int passedID = userIDInternal;
            String passedName = userNameInternal;
            controller.initData(passedID, passedName);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
            window.setTitle("Reports");
        } catch (NullPointerException e) {
            System.out.println(e);
        }
    }

    //deletes whatever appointment is selected on the list
    public void deleteAppointment(ActionEvent event) throws IOException {
        int appointmentID;
        try {
            appointmentID = appointmentTable.getSelectionModel().getSelectedItem().getId();
            boolean result = confirmBox("Deleted appointments CANNOT be restored.");
            if (result) {
                try {
                    String query = "DELETE FROM appointment WHERE appointmentid = " + appointmentID;
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(query);
                    appointments.clear();
                    appointments = getAppointments();
                    appointmentTable.getItems().clear();
                    appointmentTable.getItems().setAll(appointments);
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        System.out.println("Couldn't close statement, " + e);
                    }

                } catch (SQLException e) {
                    System.out.println("SQL EXCEPTION OCCURRED " + e);
                }

            } else {

            }
        } catch (NullPointerException e) {
            warningMaker("No selected appointment.", "No appointment selected.", "Please select an appointment to delete.");
        }

    }



    //@param message - String that displays a message to confirm
    //returns a boolean based on user answer to confirming the selection they made
    public boolean confirmBox(String message) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Are you sure?");
        alert.setHeaderText("Confirm your selection.");
        alert.setContentText(message);
        final Optional<ButtonType> result = alert.showAndWait();
        return result.get() == ButtonType.OK;

    }

    //@param accepts strings for title, header, and message
    //displays a warning message to the user
    public void warningMaker(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
