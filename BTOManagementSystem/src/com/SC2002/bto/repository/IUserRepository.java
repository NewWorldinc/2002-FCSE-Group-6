package com.SC2002.bto.repository;

import com.SC2002.bto.entities.User;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entities.
 * Follows Interface Segregation Principle by extending the generic repository
 * and adding user-specific operations.
 */
public interface IUserRepository extends IRepository<User, String> {
    
    /**
     * Finds a user by their NRIC.
     * 
     * @param nric The NRIC to search for
     * @return An Optional containing the user if found, empty otherwise
     */
    Optional<User> findByNric(String nric);
    
    /**
     * Authenticates a user with the given credentials.
     * 
     * @param nric The user's NRIC
     * @param password The user's password
     * @return An Optional containing the authenticated user if successful, empty otherwise
     */
    Optional<User> authenticate(String nric, String password);
    
    /**
     * Updates a user's password.
     * 
     * @param nric The user's NRIC
     * @param newPassword The new password
     * @return true if the password was updated successfully, false otherwise
     */
    boolean updatePassword(String nric, String newPassword);
    
    /**
     * Finds all users of a specific type.
     * 
     * @param userType The type of user to find (e.g., "Applicant", "HDBOfficer", "HDBManager")
     * @return A list of users of the specified type
     */
    List<User> findByUserType(String userType);
}
