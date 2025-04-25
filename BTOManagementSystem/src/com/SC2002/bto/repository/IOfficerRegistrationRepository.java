package com.SC2002.bto.repository;

import java.util.List;

/**
 * Repository interface for managing officer registrations.
 * Follows Interface Segregation Principle by providing a focused contract
 * for officer registration operations.
 */
public interface IOfficerRegistrationRepository {
    
    /**
     * Registers an officer for a project.
     * 
     * @param officerNric The NRIC of the officer
     * @param projectId The ID of the project
     * @return true if the registration was successful, false otherwise
     */
    boolean register(String officerNric, int projectId);
    
    /**
     * Approves an officer registration.
     * 
     * @param officerNric The NRIC of the officer
     * @param projectId The ID of the project
     * @return true if the approval was successful, false otherwise
     */
    boolean approve(String officerNric, int projectId);
    
    /**
     * Rejects an officer registration.
     * 
     * @param officerNric The NRIC of the officer
     * @param projectId The ID of the project
     * @return true if the rejection was successful, false otherwise
     */
    boolean reject(String officerNric, int projectId);
    
    /**
     * Finds pending registrations for a specific officer.
     * 
     * @param officerNric The NRIC of the officer
     * @return A list of project IDs that the officer has pending registrations for
     */
    List<Integer> findPendingByOfficer(String officerNric);
    
    /**
     * Finds pending registrations for a specific project.
     * 
     * @param projectId The ID of the project
     * @return A list of officer NRICs that have pending registrations for the project
     */
    List<String> findPendingByProject(int projectId);
    
    /**
     * Checks if an officer has a pending registration for a specific project.
     * 
     * @param officerNric The NRIC of the officer
     * @param projectId The ID of the project
     * @return true if the officer has a pending registration for the project, false otherwise
     */
    boolean hasPendingRegistration(String officerNric, int projectId);
    
    /**
     * Gets the count of pending registrations for a specific project.
     * 
     * @param projectId The ID of the project
     * @return The number of pending registrations for the project
     */
    int getPendingRegistrationCount(int projectId);
}
