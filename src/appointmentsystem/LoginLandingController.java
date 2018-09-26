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
    private ComboBox monthBox;
    @FXML
    private TableView<Appointment> appointmentTable;
    @FXML
    private TableColumn<Appointment, Date> appointmentDateColumn;
    @FXML
    private TableColumn<Appointment, String> appointmentDescriptionColumn;

    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();

    private static int userIDInternal;
    private static String userNameInternal;

    @FXML
    private void handleButtonAction(ActionEvent event) {

    }

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
    }

    public ObservableList<Appointment> getAppointments() {
        try {

            String query = "SELECT * from appointment WHERE start >= NOW();";
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
