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
public class ReportsScreenController implements Initializable {

    @FXML
    private Button cancelButton;
    @FXML
    private ComboBox<String> monthBox;
    @FXML
    private ComboBox<String> userBox;
    @FXML
    private ComboBox<String> locationBox;
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


    //checks for the locale and sets the language accordingly
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        appointmentDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        appointmentDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        monthBox.getItems().addAll("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
        locationBox.getItems().addAll("England", "Arizona", "New York");
        findUsers();

    }

    
        //Returns to the dashboard page.
    public void pressCancel(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("LoginLanding.fxml"));
            Parent userWindow = loader.load();

            Scene scene = new Scene(userWindow);

            LoginLandingController controller = loader.getController();
            int passedID = userIDInternal;
            String passedName = userNameInternal;
            controller.initData(passedID, passedName);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
            window.setTitle("Dashboard");
        } catch (NullPointerException e) {
            System.out.println(e);
        }
    }
    
 //searches for appointment by selected username   
    public ObservableList<Appointment> getAppointmentsByUser(String user) {

// prepares the calendar and date to be converted for the sql query        
        java.util.Date utilDate = new java.util.Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(utilDate);
        cal.set(Calendar.MILLISECOND, 0);
        Timestamp time = new java.sql.Timestamp(cal.getTimeInMillis());

        try {

            String query = "SELECT * from appointment WHERE start >= UNIX_TIMESTAMP('" + time + "') AND contact = '" + user + "' ORDER BY start";
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
    
       public ObservableList<Appointment> getAppointmentsByLocation(String location) {

// prepares the calendar and date to be converted for the sql query        
        java.util.Date utilDate = new java.util.Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(utilDate);
        cal.set(Calendar.MILLISECOND, 0);
        Timestamp time = new java.sql.Timestamp(cal.getTimeInMillis());

        try {

            String query = "SELECT * from appointment WHERE start >= UNIX_TIMESTAMP('" + time + "') AND location = '" + location + "' ORDER BY start";
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
    
    
    
// completes the pulling of user data and error handling
    public void searchByUser(){
        try{
            String user = userBox.getValue();
            appointments.clear();
         appointments = getAppointmentsByUser(user);
        appointmentTable.getItems().clear();
        appointmentTable.getItems().setAll(appointments);           
        } catch (NullPointerException e){
            warningMaker("Error", "No user selected.", "Please select a user.");
        }

    }
    
    
    //completes the locations search
    
     public void searchByLocation(){
        try{
            String location = locationBox.getValue();
            appointments.clear();
         appointments = getAppointmentsByLocation(location);
        appointmentTable.getItems().clear();
        appointmentTable.getItems().setAll(appointments);           
        } catch (NullPointerException e){
            warningMaker("Error", "No location selected.", "Please select a location.");
        }

    }
       
    
    public void findUsers(){
        try{
            String query = "SELECT userName FROM user";
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            
            while(result.next()){
                String userName = result.getString("userName");
                userBox.getItems().add(userName);
            }
            
            
        } catch (SQLException e){
            System.out.println("Problem with the SQL");
        }
    }


    public void searchMonth(ActionEvent event) throws IOException {

        try {
            String month = monthBox.getSelectionModel().getSelectedItem();
            int val;
            switch (month) {
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
                    warningMaker("Error", "No Month Selected.", "Please select a Month.");
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
                String query = "SELECT * FROM appointment WHERE start BETWEEN '" + year + "-" + val + "-01' AND '" + year2 + "-" + val2 + "-01' ORDER BY start";
                Statement stmt = conn.createStatement();
                ResultSet result = stmt.executeQuery(query);
                int firstAppts = 0;
                int consultations = 0;
                int followUps = 0;
                while (result.next()) {
                    String type = result.getString("title");
                    switch(type){
                        case "First Appt":
                            firstAppts++;
                            break;
                        case "Consultation":
                            consultations++;
                            break;
                        case "Follow-Up":
                            followUps++;
                            break;
                        default:
                            break;
                    }
                }
                
                warningMaker("Appointment Totals:", "Totals for the Month of " + month + ":", "First Appointments: " + firstAppts + "\nConsultations: " + consultations +
                        "\nFollow-Ups: " + followUps);
              
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
            warningMaker("Error", "No Month Selected.", "Please select a Month.");
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
