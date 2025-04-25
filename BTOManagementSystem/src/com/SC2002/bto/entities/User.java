// File: User.java
package com.SC2002.bto.entities;

/**
 * User is an abstract base class that defines common attributes for all users.
 * Fields (as per CSV): Name, NRIC, Age, Marital Status, and Password.
 */
public abstract class User {
    protected String name;
    protected String nric;
    protected String password;
    protected int age;
    protected String maritalStatus;
    
    /**
     * Constructs a User object with the specified details.
     *
     * @param name the user's full name.
     * @param nric the user's NRIC.
     * @param password the user's password.
     * @param age the user's age.
     * @param maritalStatus the user's marital status.
     */
    public User(String name, String nric, String password, int age, String maritalStatus) {
        this.name = name;
        this.nric = nric;
        this.password = password;
        this.age = age;
        this.maritalStatus = maritalStatus;
    }
    
    // Getters and setters
    
    /**
     * Gets the user's full name.
     * 
     * @return the user's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the user's NRIC (National Registration Identity Card) number.
     * This serves as a unique identifier for the user.
     * 
     * @return the user's NRIC
     */
    public String getNric() {
        return nric;
    }
    
    /**
     * Gets the user's password.
     * 
     * @return the user's password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Gets the user's age.
     * 
     * @return the user's age in years
     */
    public int getAge() {
        return age;
    }
    
    /**
     * Gets the user's marital status.
     * 
     * @return the user's marital status (e.g., "Single", "Married")
     */
    public String getMaritalStatus() {
        return maritalStatus;
    }
    
    /**
     * Sets the user's full name.
     * 
     * @param name the new name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Sets the user's NRIC.
     * 
     * @param nric the new NRIC to set
     */
    public void setNric(String nric) {
        this.nric = nric;
    }
    
    /**
     * Sets the user's password.
     * 
     * @param password the new password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Sets the user's age.
     * 
     * @param age the new age to set
     */
    public void setAge(int age) {
        this.age = age;
    }
    
    /**
     * Sets the user's marital status.
     * 
     * @param maritalStatus the new marital status to set (e.g., "Single", "Married")
     */
    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }
}
