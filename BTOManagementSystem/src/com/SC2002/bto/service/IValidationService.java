package com.SC2002.bto.service;

/**
 * Service interface for validation operations.
 * Follows Interface Segregation Principle by providing a focused contract.
 */
public interface IValidationService {
    
    /**
     * Validates a user's NRIC format.
     * 
     * @param nric The NRIC to validate
     * @return true if the NRIC format is valid, false otherwise
     */
    boolean validateNric(String nric);
    
    /**
     * Validates a user's password.
     * 
     * @param password The password to validate
     * @return true if the password is valid, false otherwise
     */
    boolean validatePassword(String password);
    
    /**
     * Validates a user's marital status.
     * 
     * @param maritalStatus The marital status to validate
     * @return true if the marital status is valid, false otherwise
     */
    boolean validateMaritalStatus(String maritalStatus);
    
    /**
     * Validates a single applicant's age.
     * 
     * @param age The age to validate
     * @param maritalStatus The marital status
     * @return true if the age is valid for a single applicant, false otherwise
     */
    boolean validateSingleApplicantAge(int age, String maritalStatus);
    
    /**
     * Validates a married applicant's age.
     * 
     * @param age The age to validate
     * @param maritalStatus The marital status
     * @return true if the age is valid for a married applicant, false otherwise
     */
    boolean validateMarriedApplicantAge(int age, String maritalStatus);
    
    /**
     * Validates the flat type eligibility based on marital status.
     * 
     * @param flatType The flat type (e.g., "2-Room", "3-Room")
     * @param maritalStatus The marital status
     * @return true if the flat type is eligible for the marital status, false otherwise
     */
    boolean validateFlatTypeEligibility(String flatType, String maritalStatus);
    
    /**
     * Validates an applicant's eligibility to apply for a project.
     * 
     * @param age The age of the applicant
     * @param maritalStatus The marital status of the applicant
     * @param flatType The flat type (e.g., "2-Room", "3-Room")
     * @return true if the applicant is eligible, false otherwise
     */
    boolean validateApplicantEligibility(int age, String maritalStatus, String flatType);
    
    /**
     * Validates a project's application period.
     * 
     * @param openingDate The opening date of the application period
     * @param closingDate The closing date of the application period
     * @return true if the application period is valid, false otherwise
     */
    boolean validateApplicationPeriod(String openingDate, String closingDate);
    
    /**
     * Validates a project's flat types.
     * 
     * @param type1 The first flat type
     * @param type2 The second flat type
     * @return true if the flat types are valid, false otherwise
     */
    boolean validateFlatTypes(String type1, String type2);
    
    /**
     * Validates a project's flat units.
     * 
     * @param units The number of units
     * @return true if the number of units is valid, false otherwise
     */
    boolean validateFlatUnits(int units);
    
    /**
     * Validates a project's flat price.
     * 
     * @param price The price
     * @return true if the price is valid, false otherwise
     */
    boolean validateFlatPrice(int price);
}
