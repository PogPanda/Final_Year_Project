package com.mycompany.fypmaven;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DB_Connection {
    
//    DB database details
    private static String jdbcUrl = "jdbc:mysql://localhost:3306/passwordmanager"; 
    private static String username = "root";
    private static String password = "";
    private static Connection connection;
    private static final Logger logger = LogManager.getLogger(); 
    
   // Function to establish a MySQL database connection
    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish database connection
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            
            return connection;
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
            throw new SQLException("JDBC driver not found.");
        }
    }
    
//    Function to execute a read query that returns a string 
    public String executeReadQuery(String query, Object[] params, String column) {
        
        String result = null;
        try ( Connection connection = getConnection()) {
            try ( PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    result = resultSet.getString(column);
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return result;
    }

//  Function to execute queries
    public void executeQuery(String query, Object[] params) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
                int rowsAffected = preparedStatement.executeUpdate();
                System.out.println(rowsAffected + " row(s) affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
