package com.mycompany.fypmaven;

import static com.mycompany.fypmaven.DB_Connection.getConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.mindrot.jbcrypt.BCrypt;
import org.jasypt.util.text.BasicTextEncryptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GeneralFunctions {

    private static final Logger logger = LogManager.getLogger();

//  This is the function used to check if the parameters entered already exist within the database.
    public static boolean checkDuplicate(Object[] params, String query) {

//      Prepares the query for execute
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

//              Sets the parameters of the query 
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }

//              Executes and checks the query 
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return true; //Returns true if there is a match
                }
            }

        } catch (SQLException e) {
//          Logs the error message if there is an error
            logger.error(e.getMessage());
        }

        return false; // Returns false if there is no match
    }

//  This is an overload of the previous function but this version of the function has values that are required to exclude
    public static boolean checkDuplicate(Object[] params, String query, Object[] excludedValues) {

//     Edits the query so that it excludes the parameters provided in the excluded values list 
        if (excludedValues != null && excludedValues.length > 0) {
            query += " AND Description NOT IN (";
            for (int i = 0; i < excludedValues.length; i++) {
                if (i > 0) {
                    query += ",";
                }
                query += "?";
            }
            query += ")";
        }

//      Prepares the query for execute
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

//              Sets the parameters of the query 
                int paramIndex = 1;
                for (Object param : params) {
                    preparedStatement.setObject(paramIndex++, param);
                }
                if (excludedValues != null && excludedValues.length > 0) {
                    // Set the parameters from excludedValues[]
                    for (Object excludedValue : excludedValues) {
                        preparedStatement.setObject(paramIndex++, excludedValue);
                    }
                }

//              Executes and checks the query 
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return true; //Returns true if there is a match
                }
            }
        } catch (SQLException e) {
//          Logs the error message if there is an error
            logger.error(e.getMessage());
        }

        return false;// Returns false if there is no match
    }

//  This is a function used to check the passwords by running it under the python script written
    public static List<Account> checkPasswords(UserAccs user) throws SQLException {

//      Declaration of list to store matched accounts 
        List<Account> matchedAccounts = new ArrayList<>();

//      Temporary file directory where the executable file will be stored
        File tempDir = new File(System.getProperty("java.io.tmpdir"));

        try {
            
//          Declaration of variables
            String query = "Select * From accounts where user_ID = ? ";
            Object[] param = {user.getUserID()};
            
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

//          Gets a list of accounts that are associated with the user
            List<Account> accountList = new ArrayList<>();
            for (int i = 0; i < param.length; i++) {
                statement.setObject(i + 1, param[i]);
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {

                // Retrieve data from the result set for each row
                String description = resultSet.getString("Description");
                String username = resultSet.getString("Username");
                String password = resultSet.getString("Password");
                String url = resultSet.getString("URL");
                String userid = resultSet.getString("user_id");

                // Create an Account object and add it to the list
                Account account = new Account(description, username, password, url, userid);
                accountList.add(account);
            }

//          Inputs each account within the account list to the python script file
            for (Account account : accountList) {

//              Gets the password of the account and decrypts it
                String password = account.getPassword();
                password = decrypt(password, user.getEncryptKey());

//              Declaration of the name of the executable
                String exeFileName = "check_password.exe";

//              Declaration of path of the executable
                InputStream inputStream = GeneralFunctions.class.getResourceAsStream("/Scripts/check_password.exe");
                File exeFile = new File(tempDir, exeFileName);
                
//              Setup to input info into the executable
                try (InputStream exeInputStream = inputStream; FileOutputStream fileOutputStream = new FileOutputStream(exeFile)) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = exeInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                }

                // Declaraation of process builder 
                ProcessBuilder processBuilder = new ProcessBuilder(exeFile.getAbsolutePath());
                Process process = processBuilder.start();

                // Declaration of Output stream of the process
                InputStream processInputStream = process.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(processInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                // Inputting the passwords to the executable
                process.getOutputStream().write((password + "\n").getBytes());
                process.getOutputStream().flush();

                // Reading the executable's output
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                // Wait for the executable to complete
                int exitCode = process.waitFor();

                // Checks the exit code
                if (exitCode == 0) {
                    System.out.println("Executable executed successfully.");
                } else {
                    System.err.println("Error: Executable execution failed with exit code " + exitCode);
                }

                // Checks the script's output
                int scriptOutput = Integer.parseInt(output.toString().trim());
                
//              Adds the account based on the output of the script
                if (scriptOutput > 1) {
                    matchedAccounts.add(account);
                }

            }
            
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage());
        }

        return matchedAccounts;
    }

//  This is a function used to notify the user that there the current accounts are at risk 
    public static void showMatchingAccounts(List<Account> matchingAccounts) {
        
//      Builds the message based on the input received
        if (!matchingAccounts.isEmpty()) {
            StringBuilder message = new StringBuilder("These accounts are at risk. Please change their passwords as soon as possible. :\n\n");
            message.append("Accounts: \n");
            for (Account account : matchingAccounts) {
                message.append("- ").append(account.getDescription()).append("\n");

            }
            
//          Outputs the option pane 
            JOptionPane.showMessageDialog(
                    null,
                    message.toString(),
                    "The account(s) shown are at risk of being breached, please change them as soon as possible",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

//    This function is used to check if the input submitted follows the correct regex pattern
    public static boolean isValidPassword(String password) {
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

//    This function is used to display a notification to the user
    public static void showNotification(String message, String title) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

//    This function is used to hash the string parameter inputted
    public static String hash(String password) {
        
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        return hashedPassword;
    }

//    This function is used to encrypt the string parameter inputted based on the key provided
    public static String encrypt(String password, String encryptKey) {
        
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(encryptKey);

        return textEncryptor.encrypt(password);

    }

//    This function is used to decrypt the string parameter inputted based on the key provided
    public static String decrypt(String encryptedText, String encryptionKey) {

        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(encryptionKey);
        
        try {
            
            return textEncryptor.decrypt(encryptedText);
            
        } catch (EncryptionOperationNotPossibleException ex) {
            
            logger.error(ex.getMessage());
            return null;
        }

    }

//    Function to login to the database
    public static boolean Login(UserAccs user) {

//        The login query 
        String query = "SELECT * FROM user_accounts WHERE Name = ? ";

//        Checking if there is a match in the database 
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setString(1, user.getUsername());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    if (resultSet.next()) {
                        String storedPassword = resultSet.getString("Password");
                        String EncryptionKey = resultSet.getString("Secret_Key");
                        String ID = resultSet.getString("User_ID");

                        // Check if the entered password matches the stored hashed password
                        if (BCrypt.checkpw(user.getPassword(), storedPassword)) {
                            user.setID(ID);
                            user.setEncryptKey(EncryptionKey);

                            return true; // If a matching user is found
                        }
                    };
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return false;
    }

}
