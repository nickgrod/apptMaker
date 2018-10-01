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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TimeZone;
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
    private ListView<Customer> customers;
    @FXML
    private ComboBox<String> locationBox;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    private static int userIDInternal;
    private static String userNameInternal;
    private int appointmentIDInternal = 0;

    //We will assume the business opens at 8 am and closes at 6pm, no appointments outside of those hours

    TimeZone tz = TimeZone.getDefault();
    long userOffsetMillis = TimeZone.getDefault().getOffset(new Date().getTime());
    private int hourFromUTC = (int)userOffsetMillis/60/60/1000;
    private final int relativeOpening = 8;
    private final int relativeClosing = 18;

    //makes sure all fields are filled out entirely
    public boolean validateAllFields() {

        try {
            String apptType = appointmentTypeBox.getValue();
            LocalDate chosenDate = appointmentDatePicker.getValue();
            String fromHour = fromHourBox.getValue();
            String fromMinute = fromMinuteBox.getValue();
            String toHour = toHourBox.getValue();
            String toMinute = toMinuteBox.getValue();
            String fromAM = fromAmBox.getValue();
            String toAM = toAmBox.getValue();
            String description = descriptionField.getText();
            String location = locationBox.getValue();

            if (apptType.isEmpty() || location.isEmpty() || chosenDate == null || fromHour.isEmpty() || fromMinute.isEmpty() || toHour.isEmpty() || toMinute.isEmpty() || fromAM.isEmpty() || toAM.isEmpty() || description.isEmpty()) {
                warningMaker("More information needed.", "Not all fields completed.", "Make sure all fields are completed: Appointment Type, Date, From Time, To Time, and Description.");
                return false;
            } else {

                return true;
            }
        } catch (NullPointerException e) {
            warningMaker("More information needed.", "Not all fields completed.", "Make sure all fields are completed: Appointment Type, Date, From Time, To Time, and Description.");

            System.out.println("Null pointer exception " + e);
            return false;
        }

    }

    public boolean checkTimes(int fromHour, int fromMin, int toHour, int toMin) {
        if (fromHour > toHour || (fromHour == toHour && fromMin > toMin)) {
            warningMaker("Invalid Time", "Please change the time.", "Appointment end time cannot be before start time.");
            return false;
        } else if (fromHour < relativeOpening || toHour > relativeClosing || (toHour == relativeClosing && toMin > 0)) {
            warningMaker("Invalid Time", "Selected appointment is outside of business hours.", "Appointments can only be scheduled between 8AM and 6PM.");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        fromAmBox.getItems().addAll("AM", "PM");
        toAmBox.getItems().addAll("AM", "PM");
        locationBox.getItems().addAll("England", "New York", "Arizona");
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
    }

    //accepts the userName and userID from the previous screen for stored procs
    public void initData(int userID, String userName) {
        userIDInternal = userID;
        userNameInternal = userName;
    }

    //initializes the screen for editing
    public void initData(int userID, String userName, int apptID) {

        userIDInternal = userID;
        userNameInternal = userName;
        appointmentIDInternal = apptID;
        try {
            String query = "SELECT * FROM appointment WHERE appointmentid = " + apptID;
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            result.next();
            Timestamp theTime = result.getTimestamp("start");
            Timestamp endTime = result.getTimestamp("end");

            long localStart = theTime.getTime() + userOffsetMillis;
            long localEnd = endTime.getTime() + userOffsetMillis;
            
            Date startDate = new Date(localStart);
            Date endDate = new Date(localEnd);
            

            String userTZ1 = TimeZone.getDefault().getID();
            ZonedDateTime z = theTime.toLocalDateTime().atZone(ZoneId.of(userTZ1));
            ZonedDateTime z2 = endTime.toLocalDateTime().atZone(ZoneId.of(userTZ1));
            LocalDate date = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            appointmentTypeBox.setValue(result.getString("title"));
            descriptionField.setText(result.getString("description"));
            appointmentDatePicker.setValue(date);
            locationBox.setValue(result.getString("location"));


            int startHour = startDate.getHours();
            int startMinute = startDate.getMinutes();
            int endHour = endDate.getHours();
            int endMinute = endDate.getMinutes();
            //set start times
            fromAmBox.setValue(toAMPM(startHour));
            fromHourBox.setValue(toTwelveString(startHour));
            fromMinuteBox.setValue(toStringMin(startMinute));

            //set end times
            toAmBox.setValue(toAMPM(endHour));
            toHourBox.setValue(toTwelveString(endHour));
            toMinuteBox.setValue(toStringMin(endMinute));

            //get and set the customer if exists
            int selectedCust = result.getInt("customerId");
            for (int i = 0; i < customers.getItems().size(); i++) {
                if (selectedCust == customers.getItems().get(i).getId()) {
                    customers.getSelectionModel().select(customers.getItems().get(i));
                }
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
        } catch (SQLException e) {
            System.out.println("Error found in SQL: " + e);
        }

    }

    //converts an int for hour in 24 format to appropriate 12 format
    public String toTwelveString(int hour) {

        if (hour == 0) {
            hour = 12;
        } else if (hour >= 13) {
            hour = hour - 12;

        }
        String str = Integer.toString(hour);
        if (str.length() == 1) {
            str = "0" + str;
        }
        return str;

    }

    public String toStringMin(int min) {
        String str = Integer.toString(min);
        if (str.length() == 1) {
            str = "0" + str;
        }
        return str;
    }

    //returns the value AM or PM depending on the hour
    public String toAMPM(int hour) {
        if (hour >= 12) {
            return "PM";
        } else {
            return "AM";
        }
    }

    public void hourAdder(ComboBox box) {
        box.getItems().addAll("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
        box.setPromptText("HH");
    }

    //creates the customer listview used to select the associated customer
    public ObservableList<Customer> getCustomerList() {

        ObservableList<Customer> custList = FXCollections.observableArrayList();
        String query = "SELECT * FROM  customer_View";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);

            while (result.next()) {
                Customer temp = new Customer();
                temp.setName(result.getString("customerName"));
                temp.setId(result.getInt("customerId"));
                temp.setAddressID(result.getInt("addressId"));
                custList.add(temp);

            }
            return custList;
        } catch (SQLException e) {

            return null;
        }
    }

    //creates a customer object to be compared against the listview
    public Customer findCustomer(int custID) {
        String query = "SELECT * FROM customer_View WHERE customerId =" + custID;
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            result.next();
            Customer temp = new Customer();
            temp.setName(result.getString("customerName"));
            temp.setId(result.getInt("customerId"));
            temp.setAddressID(result.getInt("addressId"));
            return temp;
        } catch (SQLException e) {
            return null;
        }
    }

    public void returnCustomerInfo() {
        Customer temp = customers.getSelectionModel().getSelectedItem();
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

            }
        } catch (SQLException e) {

        }
    }

    public String convertTimeStr(String hours, String amPm) {
        int hour = Integer.parseInt(hours);
        String finalHour;
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
        return finalHour;
    }

    public int convertTimeInt(String hours, String amPm) {
        int hour = Integer.parseInt(hours);
        if (amPm.equals("AM") && hour == 12) {
            hour = 0;
        } else if (amPm.equals("PM") && hour != 12) {
            hour += 12;
        }

        return hour;
    }

    public Timestamp convertDate(String hours, String min, String amPm) {
//        int hour = Integer.parseInt(hours);
        String finalHour = convertTimeStr(hours, amPm);
        LocalDate chosen = appointmentDatePicker.getValue();

        String loc = chosen.toString();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String finall = (loc + " " + finalHour + ":" + min + ":00");
        try {
            Date date = formatter.parse(finall);
            long offsetD = date.getTime() - userOffsetMillis;
            
            Timestamp finalTime = new Timestamp(offsetD);
            System.out.println(offsetD);
            return finalTime;
        } catch (ParseException e) {
            System.out.println("Parse exception caught");
        }
        return null;

    }

    public void clickSaveButton(ActionEvent event) throws IOException {

        if (!validateAllFields()) {
            return;
        }
        String type = appointmentTypeBox.getValue();
        String hour = fromHourBox.getValue();
        String minute = fromMinuteBox.getValue();
        String amPm = fromAmBox.getValue();
        String hour2 = toHourBox.getValue();
        String minute2 = toMinuteBox.getValue();
        String amPm2 = toAmBox.getValue();
        String location = locationBox.getValue();
        int fromHour = convertTimeInt(hour, amPm);
        int toHour = convertTimeInt(hour2, amPm2);
        int fromMin = Integer.parseInt(minute);
        int toMin = Integer.parseInt(minute2);
        
        

        if (!checkTimes(fromHour, fromMin, toHour, toMin)) {
            return;
        }

        Timestamp fromTime = convertDate(hour, minute, amPm);
        Timestamp toTime = convertDate(hour2, minute2, amPm2);
        String description = descriptionField.getText();
        if(checkOverlap(fromTime)){
            return;
        }
        if(checkOverlap(toTime)){
            return;
        }

        try {
            int customerID = customers.getSelectionModel().getSelectedItem().getId();

            try {
                CallableStatement cStmt;
                cStmt = conn.prepareCall("{ CALL postAppointment(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }");
                cStmt.setInt(1, customerID);
                cStmt.setString(2, type);
                cStmt.setString(3, description);
                cStmt.setString(4, location);
                cStmt.setString(5, userNameInternal);
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
    
    //creates an appointment if current times overlap with another appointment
    public boolean checkOverlap(Timestamp ts){
        
        String query = "SELECT * FROM appointment WHERE '" + ts + "'  BETWEEN start AND end LIMIT 1;";
        
        try{
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            
            if(result.next()){
                
                Timestamp theTime = result.getTimestamp("start");
                Timestamp endTime = result.getTimestamp("end");
                    long localStart = theTime.getTime() + userOffsetMillis;
                    long localEnd = endTime.getTime() + userOffsetMillis;
            
            Date startDate = new Date(localStart);
            Date endDate = new Date(localEnd);
               
                String st = toTwelveString(startDate.getHours()) + ":" + toStringMin(startDate.getMinutes());
                String et = toTwelveString(endDate.getHours()) + ":" + toStringMin(endDate.getMinutes());
                warningMaker("Cannot Save Appointment", "Overlapping Appointment Found", "Cannot save appointment - overlapping appointment begins at " + st + " and ends at " + et + ".");
                
                
                return true;
            }else{
                return false;
            }
        } catch (SQLException e){
            System.out.println("Problem with SQL..." + e);
            return true;
        }
        
    }
    
}
