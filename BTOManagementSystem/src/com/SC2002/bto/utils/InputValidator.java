// File: InputValidator.java
package com.SC2002.bto.utils;

/**
 * InputValidator contains methods for validating user input such as NRIC, password formats,
 * age requirements, and marital status.
 */
public class InputValidator {
 
    /**
     * Validates the provided NRIC. The valid NRIC should start with 'S' or 'T',
     * followed by 7 digits and end with a letter.
     *
     * @param nric the NRIC string to validate.
     * @return true if the NRIC is valid; false otherwise.
     */
    public static boolean validateNric(String nric) {
        if (nric == null) {
            return false;
        }
        // Regex: ^[ST] for starting with S or T, \d{7} for 7 digits, and [A-Z] for the ending letter.
        return nric.matches("^[ST]\\d{7}[A-Z]$");
    }
 
    /**
     * Validates the provided password. This method checks if the password is non-null and meets
     * the minimum length requirement.
     *
     * @param password the password to validate.
     * @return true if the password is valid; false otherwise.
     */
    public static boolean validatePassword(String pw) {
    	// Password must be at least 8 characters long
        return pw != null && pw.trim().length() >= 8;
    }

    /**
     * Checks whether the provided password matches the default password.
     *
     * @param password the password to check.
     * @return true if the password is equal to the default password; false otherwise.
     */
    public static boolean isDefaultPassword(String password) {
    	return Constants.DEFAULT_PASSWORD.equals(password);
    }
    
    /**
     * Validates if a single applicant meets the age requirement (35 years or older).
     *
     * @param age the applicant's age.
     * @param maritalStatus the applicant's marital status.
     * @return true if the single applicant meets the age requirement; false otherwise.
     */
    public static boolean validateSingleApplicantAge(int age, String maritalStatus) {
        if (!"Single".equalsIgnoreCase(maritalStatus)) {
            return false; // Not applicable for non-single applicants
        }
        return age >= 35;
    }
    
    /**
     * Validates if a married applicant meets the age requirement (21 years or older).
     *
     * @param age the applicant's age.
     * @param maritalStatus the applicant's marital status.
     * @return true if the married applicant meets the age requirement; false otherwise.
     */
    public static boolean validateMarriedApplicantAge(int age, String maritalStatus) {
        if (!"Married".equalsIgnoreCase(maritalStatus)) {
            return false; // Not applicable for non-married applicants
        }
        return age >= 21;
    }
    
    /**
     * Validates if an applicant is eligible to apply for a specific flat type based on their marital status.
     *
     * @param flatType the flat type (e.g., "2-Room", "3-Room").
     * @param maritalStatus the applicant's marital status.
     * @return true if the applicant is eligible for the flat type; false otherwise.
     */
    public static boolean validateFlatTypeEligibility(String flatType, String maritalStatus) {
        if ("Single".equalsIgnoreCase(maritalStatus)) {
            // Singles can only apply for 2-Room flats
            return "2-Room".equalsIgnoreCase(flatType);
        } else if ("Married".equalsIgnoreCase(maritalStatus)) {
            // Married applicants can apply for 2-Room or 3-Room flats
            return "2-Room".equalsIgnoreCase(flatType) || "3-Room".equalsIgnoreCase(flatType);
        }
        return false; // Invalid marital status
    }
    
    /**
     * Validates if the marital status is valid (either "Single" or "Married").
     *
     * @param maritalStatus the marital status to validate.
     * @return true if the marital status is valid; false otherwise.
     */
    public static boolean validateMaritalStatus(String maritalStatus) {
        return "Single".equalsIgnoreCase(maritalStatus) || "Married".equalsIgnoreCase(maritalStatus);
    }
    
    /**
     * Validates if an applicant meets all eligibility criteria for applying for a flat.
     *
     * @param age the applicant's age.
     * @param maritalStatus the applicant's marital status.
     * @param flatType the flat type the applicant wants to apply for.
     * @return true if the applicant meets all eligibility criteria; false otherwise.
     */
    public static boolean validateApplicantEligibility(int age, String maritalStatus, String flatType) {
        if (!validateMaritalStatus(maritalStatus)) {
            return false; // Invalid marital status
        }
        
        if ("Single".equalsIgnoreCase(maritalStatus)) {
            return validateSingleApplicantAge(age, maritalStatus) && validateFlatTypeEligibility(flatType, maritalStatus);
        } else if ("Married".equalsIgnoreCase(maritalStatus)) {
            return validateMarriedApplicantAge(age, maritalStatus) && validateFlatTypeEligibility(flatType, maritalStatus);
        }
        
        return false; // Should not reach here if validateMaritalStatus is correct
    }
}
