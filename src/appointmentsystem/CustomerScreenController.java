/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentsystem;

import static appointmentsystem.DBConnection.conn;
import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;
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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 *
 * @author nickg
 */
public class CustomerScreenController implements Initializable {

    @FXML
    private TextField nameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField address2Field;
    @FXML
    private TextField cityField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField postalCodeField;
    @FXML
    private TextField countryField;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button cancelButton;

    private static int userIDInternal;
    private static String userNameInternal;
    private static int customerID = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void initData(int userID, String userName) {
        userIDInternal = userID;
        userNameInternal = userName;

    }

    public void initData(int userID, String userName, int customerIDIn) {
        
        userIDInternal = userID;
        userNameInternal = userName;       
        try {
            customerID = customerIDIn;
            String query = "SELECT * FROM customer_View WHERE customerId = " + customerIDIn;
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            result.next();

            nameField.setText(result.getString("customerName"));
            addressField.setText(result.getString("address"));
            address2Field.setText(result.getString("address2"));
            postalCodeField.setText(result.getString("postalCode"));
            phoneField.setText(result.getString("phone"));
            cityField.setText(result.getString("city"));
            countryField.setText(result.getString("country"));
            saveButton.setText("Update Entry");
            deleteButton.setDisable(false);
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

    //validates customer data prior to allowing a save
    public boolean validateCustomerData() {
        String name = nameField.getText();
        String country = countryField.getText();
        String city = cityField.getText();
        String address1 = addressField.getText();
        String zip = postalCodeField.getText();
        String phone = phoneField.getText();

        if (name.isEmpty() || address1.isEmpty() || country.isEmpty() || city.isEmpty() || zip.isEmpty() || phone.isEmpty()) {
            warningMaker("Cannot Update Information", "Incomplete Data Provided", "The following fields are required to save: Name, Address, City, Phone, Postal Code, and Country.");
            return false;

        } else {
            return true;
        }

    }

    //saves the customer data to the customer table using stored proc
    public void saveCustomer(ActionEvent event) throws IOException, ClassNotFoundException {

        boolean check = validateCustomerData();

        if (check == false) {
            return;
        }

        String name = nameField.getText();
        String country = countryField.getText();
        String city = cityField.getText();
        String address1 = addressField.getText();
        String address2 = address2Field.getText();
        if (address2.isEmpty()) {
            address2 = "";
        }
        String zip = postalCodeField.getText();
        String phone = phoneField.getText();
        String createdBy = userNameInternal;

        try {

            CallableStatement cStmt;
            cStmt = conn.prepareCall("{ CALL fullAddressCreate(?, ?, ?, ?, ?, ?, ?) }");
            cStmt.setString(1, country);
            cStmt.setString(2, city);
            cStmt.setString(3, address1);
            cStmt.setString(4, address2);
            cStmt.setString(5, zip);
            cStmt.setString(6, phone);
            cStmt.setString(7, createdBy);
            ResultSet result = cStmt.executeQuery();
            try {
                result.next();
                int addressID = result.getInt("addressId");
                CallableStatement stmt;
                stmt = conn.prepareCall("{ CALL postCustomer(?, ?, ?, ?) }");
                stmt.setString(1, name);
                stmt.setInt(2, addressID);
                stmt.setString(3, createdBy);
                stmt.setInt(4, customerID);
                ResultSet result2 = stmt.executeQuery();
                try {
                    result2.next();
                    System.out.println("Customer successfully created");
                } catch (SQLException e) {
                    System.out.println("Customer could not be created");
                } finally {
                    try {
                        result.close();
                    } catch (SQLException e) {
                        System.out.println("Couldn't close result set");
                    }
                    try {
                        result2.close();
                    } catch (SQLException e) {
                        System.out.println("Couldn't close result set");
                    }
                    try {
                        cStmt.close();
                    } catch (SQLException e) {
                        System.out.println("Couldn't close statement");
                    }
                    
                    warningMaker("Success", "Customer information saved", "Successfully saved");
                    pressCancel(event);
                }

            } catch (SQLException e) {
                System.out.println("ERROR AT STORED PROCEDURE: " + e);
            }

        } catch (SQLException e) {
            System.out.println("SQL EXCEPTION: " + e);
        }
    }

    //deletes the customer being edited
    public void deleteCustomer(ActionEvent event) throws IOException {
        boolean result = confirmBox("Customer and associated appointments will be deleted.");
        if(result == false){
        }else{
         try {
             
            CallableStatement stmt;
            stmt = conn.prepareCall("{ CALL deleteCustomer(?) }");
            stmt.setInt(1, customerID);
            stmt.executeQuery();
            try {
                stmt.close();
               warningMaker("Success", "Customer information deleted", "Successfully deleted customer.");

                pressCancel(event);
                
                
            } catch (SQLException exx) {
                System.out.println("Couldn't close statement, " + exx);
            }
        } catch (SQLException ex) {
            System.out.println("SQL Exception occurred: " + ex);
        }
           
        }

    }

    //takes back to the dashboard page
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
}
