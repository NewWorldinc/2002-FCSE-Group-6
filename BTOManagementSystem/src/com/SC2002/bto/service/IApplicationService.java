package com.SC2002.bto.service;

import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.ApplicationStatus;
import java.util.List;

/**
 * Service interface for application-related operations.
 * Follows Interface Segregation Principle by providing a focused contract.
 */
public interface IApplicationService {
    
    /**
     * Applies for a project.
     * 
     * @param applicant The applicant
     * @param projectId The ID of the project
     * @param flatType The flat type (e.g., "2-Room", "3-Room")
     * @return true if the application was successful, false otherwise
     */
    boolean applyForProject(Applicant applicant, int projectId, String flatType);
    
    /**
     * Withdraws an application.
     * 
     * @param applicant The applicant
     * @return true if the withdrawal was successful, false otherwise
     */
    boolean withdrawApplication(Applicant applicant);
    
    /**
     * Processes an application (approves or rejects).
     * 
     * @param applicantNric The NRIC of the applicant
     * @param status The new application status
     * @return true if the processing was successful, false otherwise
     */
    boolean processApplication(String applicantNric, ApplicationStatus status);
    
    /**
     * Books a flat for an applicant.
     * 
     * @param applicantNric The NRIC of the applicant
     * @return true if the booking was successful, false otherwise
     */
    boolean bookFlat(String applicantNric);
    
    /**
     * Gets applications by their status.
     * 
     * @param status The application status to search for
     * @return A list of applicants with the specified application status
     */
    List<Applicant> getApplicationsByStatus(ApplicationStatus status);
    
    /**
     * Gets applications for a specific project.
     * 
     * @param projectId The ID of the project
     * @return A list of applicants who have applied for the specified project
     */
    List<Applicant> getApplicationsByProject(int projectId);
    
    /**
     * Gets applications for a specific flat type.
     * 
     * @param flatType The flat type to search for (e.g., "2-Room", "3-Room")
     * @return A list of applicants who have applied for the specified flat type
     */
    List<Applicant> getApplicationsByFlatType(String flatType);
    
    /**
     * Gets applications for projects managed by a specific HDB manager.
     * 
     * @param managerNric The NRIC of the manager
     * @return A list of applicants who have applied for projects managed by the specified manager
     */
    List<Applicant> getApplicationsByManager(String managerNric);
    
    /**
     * Gets applications for projects that a specific HDB officer is assigned to.
     * 
     * @param officerNric The NRIC of the officer
     * @return A list of applicants who have applied for projects the officer is assigned to
     */
    List<Applicant> getApplicationsByOfficer(String officerNric);
    
    /**
     * Checks if an applicant is eligible to apply for a project.
     * 
     * @param applicant The applicant
     * @param projectId The ID of the project
     * @param flatType The flat type (e.g., "2-Room", "3-Room")
     * @return true if the applicant is eligible, false otherwise
     */
    boolean isEligible(Applicant applicant, int projectId, String flatType);
    
    /**
     * Validates the flat type eligibility based on marital status.
     * 
     * @param flatType The flat type (e.g., "2-Room", "3-Room")
     * @param maritalStatus The marital status
     * @return true if the flat type is eligible for the marital status, false otherwise
     */
    boolean validateFlatTypeEligibility(String flatType, String maritalStatus);
}
