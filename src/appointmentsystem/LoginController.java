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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 *
 * @author nickg
 */
public class LoginController implements Initializable {

    @FXML
    private Label loginLabel;

    @FXML
    private TextField usernameBox;
    @FXML
    private TextField passwordBox;
    @FXML
    private Button loginButton;
    @FXML
    private Button cancelButton;

    public String alertMsg = "Invalid username and/or password.";
    public String alertHeader = "Login Error:";
    public String alertTitle = "ERROR";

    @FXML
    private void handleButtonAction(ActionEvent event) {

    }

    //exits the program on click of the "exit" button
    public void leaveProgram() {
        System.exit(0);
    }

    //checks for the locale and sets the language accordingly
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Locale current = Locale.getDefault();
        System.out.println(current);
        if (current.getLanguage().equals("en")) {
            System.out.println("English detected");
        } else if (current.getLanguage().equals("fr")) {
            setToFrench();
        }

    }

    //changes all field values to french if needed
    public void setToFrench() {
        System.out.println("French detected");
        loginLabel.setText("S'identifier");
        loginButton.setText("Entrer");
        cancelButton.setText("Résilier");
        usernameBox.setPromptText("Nom d'utilisateur");
        passwordBox.setPromptText("Mot de passe");
        alertMsg = "Nom d'utilisateur ou mot de passe incorrect.";
        alertHeader = "Erreur d'identification";
        alertTitle = "Erreur";
    }

    public void checkLogin(ActionEvent event) throws IOException {
        String name = usernameBox.getText();
        String pass = passwordBox.getText();
        String query = "SELECT * FROM user WHERE userName = '" + name + "' AND password = '" + pass + "'";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);

            //The faster way to do this would be to just run an "if result.next()", but for the sake
            //of incorporating different error handlers, here's a longer route
            result.next();
            try {
                int success = result.getInt("userid");
                System.out.println("Valid password, logged in as user number " + success);
                checkFifteenStart();
                checkFifteenOngoing();
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
                openLandingScreen(event, success, name);
            } catch (SQLException e) {
                warningMaker(alertTitle, alertHeader, alertMsg);
                System.out.println("SQL Exception Error: " + e);
            }
        } catch (SQLException e) {
            Logger.getLogger(AppointmentSystem.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("SQL Exception Error: " + e);
        }

    }

    public void warningMaker(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void openLandingScreen(ActionEvent event, int userID, String userName) throws IOException {

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("LoginLanding.fxml"));
            Parent loginWindow = loader.load();

            Scene scene = new Scene(loginWindow);

            LoginLandingController controller = loader.getController();
            int passedID = userID;
            String passedName = userName;
            controller.initData(passedID, passedName);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
            window.setTitle("Dashboard");
        } catch (NullPointerException e) {
            System.out.println("");
        }
    }

    public void enterPress(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            loginButton.fire();
        }
    }

    
        //checks whether or not there is an appointment starting in the next 15 minutes
    public void checkFifteenStart() {
        String query = "SELECT * FROM appointment WHERE start BETWEEN NOW() AND (NOW() + INTERVAL 15 MINUTE) LIMIT 1";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            if (result.next()) {
                warningMaker("Upcoming Appointment", "You have an appointment starting soon.", "Please check your schedule.");
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
            System.out.println("Problem with checking for the 15..." + e);
        }

    }

    //checks whether or not there is an appointment ongoing
    public void checkFifteenOngoing() {
        String query = "SELECT * FROM appointment WHERE NOW() BETWEEN start AND end LIMIT 1";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            if (result.next()) {
                warningMaker("Ongoing Appointment", "There is an appointment ongoing.", "Please check your schedule.");
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
            System.out.println("Problem with checking for the ongoing.." + e);

        }
    }
}
