package com.SC2002.bto.entities;

import java.util.List;

/**
 * ProjectInteractor defines common functionality for users who can interact with projects.
 * This interface is part of a refactoring to improve code reuse between Applicant and HDBOfficer classes.
 * 
 * Following SOLID principles:
 * - Interface Segregation: Focused interface for project interaction
 * - Open/Closed: System is open for extension with new user types
 * - Dependency Inversion: High-level modules depend on this abstraction
 */
public interface ProjectInteractor {
    
    /**
     * Gets the list of projects this user can view.
     * Implementation will vary based on user type (e.g., eligibility criteria for applicants).
     * 
     * @return List of projects the user can view
     */
    List<Project> getViewableProjects();
    
    /**
     * Checks if the user can interact with the specified project.
     * 
     * @param projectId The ID of the project to check
     * @return true if the user can interact with the project, false otherwise
     */
    boolean canInteractWithProject(int projectId);
    
    /**
     * Gets the list of enquiries relevant to this user.
     * For applicants, this would be their own enquiries.
     * For officers, this would be enquiries for their assigned projects.
     * 
     * @return List of relevant enquiries
     */
    List<Enquiry> getRelevantEnquiries();
    
    /**
     * Changes the user's password.
     * 
     * @param currentPassword The current password
     * @param newPassword The new password
     * @return true if the password was changed successfully, false otherwise
     */
    boolean changePassword(String currentPassword, String newPassword);
}
