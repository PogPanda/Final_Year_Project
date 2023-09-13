package com.mycompany.fypmaven;
public class Account {
    
// Declaration of variables     
    private String Description;
    private String Username;
    private String Password;
    private String URL;
    private String userID;
    
//  Account Constructor
    public Account(String Description, String Username, String Password, String URL, String userID){  
        this.Description = Description;
        this.Username = Username;
        this.Password = Password;
        this.URL = URL;
        this.userID = userID;
    }
    
//  Getter and Setters 
    public String getDescription(){
        
        return Description;
    }
    
    public String getUserName(){
        
        return Username;
    }
    
    public String getPassword(){
        
        return Password;
    }
    
    public String getURL(){
        
        return URL;
    }
    
    public String getUserID(){
        
        return userID;
    }
   
}
