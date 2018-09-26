package appointmentsystem;

import static appointmentsystem.DBConnection.conn;
import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author nickg
 */
public class AppointmentScreenController implements Initializable {

    @FXML
    private Label appointmentTitleLabel;
    @FXML
    private TextField appointmentTitleField;
    @FXML
    private ComboBox<String> appointmentTypeBox;
    @FXML
    private DatePicker appointmentDatePicker;
    @FXML
    private ComboBox<String> fromHourBox;
    @FXML
    private ComboBox<String> fromMinuteBox;
    @FXML
    private ComboBox<String> fromAmBox;
    @FXML
    private ComboBox<String> toHourBox;
    @FXML
    private ComboBox<String> toMinuteBox;
    @FXML
    private ComboBox<String> toAmBox;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField toolTipBox;
    @FXML
    private ListView<Customer> customers;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    
    
    private static int userIDInternal;
    private static String userNameInternal;
    private int appointmentIDInternal = 0;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        fromAmBox.getItems().addAll("AM", "PM");
        toAmBox.getItems().addAll("AM", "PM");
        fromMinuteBox.getItems().addAll("00", "15", "30", "45");
        toMinuteBox.getItems().addAll("00", "15", "30", "45");
        appointmentTypeBox.getItems().addAll("First Appt", "Consultation", "Follow-up");
        appointmentTypeBox.setPromptText("Select...");
        hourAdder(fromHourBox);
        hourAdder(toHourBox);

        fromAmBox.setPromptText("--");
        toAmBox.setPromptText("--");
        fromHourBox.setPromptText("HH");
        toHourBox.setPromptText("HH");
        fromMinuteBox.setPromptText("MM");
        toMinuteBox.setPromptText("MM");
        customerList = getCustomerList();
        customers.setItems(customerList);

        customers.setCellFactory(param -> new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getName() == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        getTimes();

    }
    
    //accepts the userName and userID from the previous screen for stored procs
    public void initData(int userID, String userName) {
        userIDInternal = userID;
        userNameInternal = userName;
        System.out.println(userNameInternal + "is the current name");
    }

    public void hourAdder(ComboBox box) {
        box.getItems().addAll("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
        box.setPromptText("MM");
    }

    public ObservableList<Customer> getCustomerList() {

        ObservableList<Customer> custList = FXCollections.observableArrayList();
        String query = "SELECT * FROM  customer_View";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);

            while (result.next()) {
                Customer temp = new Customer();
                temp.setName(result.getString("customerName"));
                temp.setId(result.getInt("customerid"));
                temp.setAddressID(result.getInt("addressId"));
                custList.add(temp);

            }
            return custList;
        } catch (SQLException e) {

            return null;
        }
    }

    public void returnCustomerInfo() {
        Customer temp = customers.getSelectionModel().getSelectedItem();
        System.out.println("SELECTED: " + temp.getName() + " customer number " + temp.getId());
    }

    public void saveAppointment() {
        String apptTitle = appointmentTitleField.getText();
        String type = appointmentTypeBox.getSelectionModel().getSelectedItem();

    }

    public void getTimes() {
        DateFormat formatter;
        Date date;
        formatter = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        Timestamp time;
        String query = "SELECT * FROM customer;";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            while (result.next()) {
                date = result.getTimestamp("createDate");
                String myStr = formatter.format(date);
                time = new Timestamp(date.getTime());
                System.out.println(time);
                System.out.println(myStr);

            }
        } catch (SQLException e) {

        }
    }

    public Timestamp convertDate(String hours, String min, String amPm) {
        int hour = Integer.parseInt(hours);
        String finalHour;
        LocalDate chosen = appointmentDatePicker.getValue();
        if (amPm.equals("AM") && hour == 12) {
            hour = 0;
        } else if (amPm.equals("PM") && hour != 12) {
            hour += 12;
        }
        if (hour < 10) {
            finalHour = "0" + Integer.toString(hour);
        } else {
            finalHour = Integer.toString(hour);
        }

        String loc = chosen.toString();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(loc);
        String times = finalHour + ":" + min + ":00.0";
        System.out.println(times);

        String finall = (loc + " " + finalHour + ":" + min + ":00");
        try {
            Date date = formatter.parse(finall);
            Timestamp finalTime = new Timestamp(date.getTime());
            System.out.println(finalTime);
            return finalTime;
        } catch (ParseException e) {
            System.out.println("Parse exception caught");
        }
        return null;

    }

    public void clickSaveButton(ActionEvent event) throws IOException {

        String title = appointmentTitleField.getText();
        String type = appointmentTypeBox.getValue();
        String hour = fromHourBox.getValue();
        String minute = fromMinuteBox.getValue();
        String amPm = fromAmBox.getValue();
        String hour2 = toHourBox.getValue();
        String minute2 = toMinuteBox.getValue();
        String amPm2 = toAmBox.getValue();
        Timestamp fromTime = convertDate(hour, minute, amPm);
        Timestamp toTime = convertDate(hour2, minute2, amPm2);
        String description = descriptionField.getText();

        try {
            int customerID = customers.getSelectionModel().getSelectedItem().getId();

            try {
                CallableStatement cStmt;
                cStmt = conn.prepareCall("{ CALL postAppointment(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }");
                cStmt.setInt(1, customerID);
                cStmt.setString(2, title);
                cStmt.setString(3, description);
                cStmt.setString(4, "Englad");
                cStmt.setString(5, "Admin");
                cStmt.setString(6, "");
                cStmt.setTimestamp(7, fromTime);
                cStmt.setTimestamp(8, toTime);
                cStmt.setString(9, userNameInternal);
                cStmt.setInt(10, appointmentIDInternal);
                cStmt.execute();

                try {
                    cStmt.close();
                    warningMaker("Appointment saved.", "Appointment added to db.", "Appointment has been saved.");
                    pressCancel(event);
                } catch (SQLException e) {
                    System.out.println("Couldn't close statement, " + e);
                }
            } catch (SQLException e) {
                System.out.println("ERROR: " + e);
            } 
        } catch (NullPointerException e) {
            warningMaker("Incomplete Data", "Please select a customer.", "Customer selection needed to continue.");
        }

    }
     
    
    public void pressCancel(ActionEvent event) throws IOException{
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
