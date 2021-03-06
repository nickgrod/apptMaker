/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentsystem;

import static appointmentsystem.DBConnection.conn;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author nickg
 */
public class AppointmentSystem extends Application {
    private static int USERID;
    private static String user;
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        
        try{
 
         DBConnection.makeConnection();           

        
        launch(args);
        DBConnection.closeConnection();           
        } catch (SQLException e){
            Logger.getLogger(AppointmentSystem.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("SQL Exception Error: " + e);
        } catch (Exception e) {
            Logger.getLogger(AppointmentSystem.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Error: " + e);
        }
        
        
        

    }
    
}
