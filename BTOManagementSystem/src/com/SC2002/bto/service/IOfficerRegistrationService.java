package com.SC2002.bto.service;

import java.util.List;

/**
 * Service interface for officer registration operations.
 * Follows Interface Segregation Principle by providing a focused contract.
 */
public interface IOfficerRegistrationService {
    
    /**
     * Registers an officer for a project.
     * 
     * @param officerNric The NRIC of the officer
     * @param projectId The ID of the project
     * @return true if the registration was successful, false otherwise
     */
    boolean registerForProject(String officerNric, int projectId);
    
    /**
     * Approves an officer registration.
     * 
     * @param officerNric The NRIC of the officer
     * @param projectId The ID of the project
     * @return true if the approval was successful, false otherwise
     */
    boolean approveRegistration(String officerNric, int projectId);
    
    /**
     * Rejects an officer registration.
     * 
     * @param officerNric The NRIC of the officer
     * @param projectId The ID of the project
     * @return true if the rejection was successful, false otherwise
     */
    boolean rejectRegistration(String officerNric, int projectId);
    
    /**
     * Gets pending registrations for a specific officer.
     * 
     * @param officerNric The NRIC of the officer
     * @return A list of project IDs that the officer has pending registrations for
     */
    List<Integer> getPendingRegistrationsByOfficer(String officerNric);
    
    /**
     * Gets pending registrations for a specific project.
     * 
     * @param projectId The ID of the project
     * @return A list of officer NRICs that have pending registrations for the project
     */
    List<String> getPendingRegistrationsByProject(int projectId);
    
    /**
     * Gets all pending registrations.
     * 
     * @return A list of officer-project registration pairs
     */
    List<OfficerProjectPair> getAllPendingRegistrations();
    
    /**
     * Checks if an officer is eligible to register for a project.
     * 
     * @param officerNric The NRIC of the officer
     * @param projectId The ID of the project
     * @return true if the officer is eligible, false otherwise
     */
    boolean isEligibleForRegistration(String officerNric, int projectId);
    
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
    
    /**
     * Checks if a project has reached its officer registration limit.
     * 
     * @param projectId The ID of the project
     * @return true if the project has reached its registration limit, false otherwise
     */
    boolean hasReachedRegistrationLimit(int projectId);
    
    /**
     * A simple class to represent an officer-project registration pair.
     */
    class OfficerProjectPair {
        private final String officerNric;
        private final int projectId;
        
        public OfficerProjectPair(String officerNric, int projectId) {
            this.officerNric = officerNric;
            this.projectId = projectId;
        }
        
        public String getOfficerNric() {
            return officerNric;
        }
        
        public int getProjectId() {
            return projectId;
        }
    }
}
