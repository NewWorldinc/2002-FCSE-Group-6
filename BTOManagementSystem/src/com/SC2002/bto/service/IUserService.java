package com.SC2002.bto.service;

import com.SC2002.bto.entities.User;
import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.HDBOfficer;
import com.SC2002.bto.entities.HDBManager;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for user-related operations.
 * Follows Interface Segregation Principle by providing a focused contract.
 */
public interface IUserService {
    
    /**
     * Authenticates a user with the given credentials.
     * 
     * @param role The role of the user (e.g., "Applicant", "HDBOfficer", "HDBManager")
     * @param nric The user's NRIC
     * @param password The user's password
     * @return An Optional containing the authenticated user if successful, empty otherwise
     */
    Optional<User> authenticate(String role, String nric, String password);
    
    /**
     * Changes a user's password.
     * 
     * @param nric The user's NRIC
     * @param currentPassword The current password
     * @param newPassword The new password
     * @return true if the password was changed successfully, false otherwise
     */
    boolean changePassword(String nric, String currentPassword, String newPassword);
    
    /**
     * Gets all applicants.
     * 
     * @return A list of all applicants
     */
    List<Applicant> getAllApplicants();
    
    /**
     * Gets all HDB officers.
     * 
     * @return A list of all HDB officers
     */
    List<HDBOfficer> getAllOfficers();
    
    /**
     * Gets all HDB managers.
     * 
     * @return A list of all HDB managers
     */
    List<HDBManager> getAllManagers();
    
    /**
     * Gets a user by their NRIC.
     * 
     * @param nric The NRIC to search for
     * @return An Optional containing the user if found, empty otherwise
     */
    Optional<User> getUserByNric(String nric);
    
    /**
     * Gets an applicant by their NRIC.
     * 
     * @param nric The NRIC to search for
     * @return An Optional containing the applicant if found, empty otherwise
     */
    Optional<Applicant> getApplicantByNric(String nric);
    
    /**
     * Gets an HDB officer by their NRIC.
     * 
     * @param nric The NRIC to search for
     * @return An Optional containing the HDB officer if found, empty otherwise
     */
    Optional<HDBOfficer> getOfficerByNric(String nric);
    
    /**
     * Gets an HDB manager by their NRIC.
     * 
     * @param nric The NRIC to search for
     * @return An Optional containing the HDB manager if found, empty otherwise
     */
    Optional<HDBManager> getManagerByNric(String nric);
    
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
}
