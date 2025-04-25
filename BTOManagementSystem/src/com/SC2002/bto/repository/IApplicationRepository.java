package com.SC2002.bto.repository;

import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.ApplicationStatus;
import java.util.List;

/**
 * Repository interface for managing applications.
 * Follows Interface Segregation Principle by providing a focused contract
 * for application-related operations.
 */
public interface IApplicationRepository {
    
    /**
     * Finds applications by their status.
     * 
     * @param status The application status to search for
     * @return A list of applicants with the specified application status
     */
    List<Applicant> findByStatus(ApplicationStatus status);
    
    /**
     * Finds applications for a specific project.
     * 
     * @param projectId The ID of the project
     * @return A list of applicants who have applied for the specified project
     */
    List<Applicant> findByProject(int projectId);
    
    /**
     * Finds applications for a specific flat type.
     * 
     * @param flatType The flat type to search for (e.g., "2-Room", "3-Room")
     * @return A list of applicants who have applied for the specified flat type
     */
    List<Applicant> findByFlatType(String flatType);
    
    /**
     * Updates the status of an application.
     * 
     * @param applicantNric The NRIC of the applicant
     * @param status The new application status
     * @return true if the status was updated successfully, false otherwise
     */
    boolean updateStatus(String applicantNric, ApplicationStatus status);
    
    /**
     * Applies for a project.
     * 
     * @param applicant The applicant
     * @param projectId The ID of the project
     * @param flatType The flat type (e.g., "2-Room", "3-Room")
     * @return true if the application was successful, false otherwise
     */
    boolean apply(Applicant applicant, int projectId, String flatType);
    
    /**
     * Withdraws an application.
     * 
     * @param applicantNric The NRIC of the applicant
     * @return true if the withdrawal was successful, false otherwise
     */
    boolean withdraw(String applicantNric);
    
    /**
     * Books a flat for an applicant.
     * 
     * @param applicantNric The NRIC of the applicant
     * @return true if the booking was successful, false otherwise
     */
    boolean book(String applicantNric);
    
    /**
     * Rejects an application.
     * 
     * @param applicantNric The NRIC of the applicant
     * @return true if the rejection was successful, false otherwise
     */
    boolean reject(String applicantNric);
}
