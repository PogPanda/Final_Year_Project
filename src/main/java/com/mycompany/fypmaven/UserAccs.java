package com.mycompany.fypmaven;

public class UserAccs {
    
    private String ID;
    private String Username;
    private String Password;
    private String RecoveryPhrase;
    private String encryptKey;
            
    public UserAccs(String Username, String Password){
        
    this.Username = Username;
    this.Password = Password; 
        
    }
    
   public String getUserID(){
       
       return ID;
   }
    public String getUsername() {

        return Username;
    }

    public String getPassword(){
        
        return Password;
    }
    
    public String getRecoveryPhrase (){
        
        return RecoveryPhrase;
    }
    
    public String getEncryptKey(){
        
        return encryptKey;
    }
    
    public void setID(String ID){
        
        this.ID = ID;
    }
    
    public void setUsername(String Username){
        
        this.Username = Username;
    }
    
    public void setPassword (String Password){
        
        this.Password = Password; 
    }
    
    public void setRecoveryPhrase (String Recovery){
        
        this.RecoveryPhrase = Recovery;
    }
    
    public void setEncryptKey (String Key){
        
        this.encryptKey = Key;
    }
    
    
}
