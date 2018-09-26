/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentsystem;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author nickg
 */
public class DBConnection {
    
    private static final String dbName = "U05KzF";
    private static final String db_URL = "jdbc:mysql://52.206.157.109/" + dbName;
    private static final String username = "U05KzF";
    private static final String password = "53688528724";
    private static final String driver = "com.mysql.jdbc.Driver";
    static Connection conn;
    
    public static void makeConnection() throws ClassNotFoundException, SQLException, Exception{
        
        Class.forName(driver);
        conn = (Connection)DriverManager.getConnection(db_URL, username, password);
        System.out.println("Successfully connected to DB.");
    }
    public static void closeConnection() throws ClassNotFoundException, SQLException, Exception {
        
        conn.close();
        System.out.println("Connection has been closed.");
    }
    
}
