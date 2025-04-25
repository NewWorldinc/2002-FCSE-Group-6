package com.SC2002.bto.service;

import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.Project;
import java.util.List;

/**
 * Service interface for report generation operations.
 * Follows Interface Segregation Principle by providing a focused contract.
 */
public interface IReportService {
    
    /**
     * Generates a report on applicants.
     * 
     * @param applicants The list of applicants to include in the report
     * @return The generated report as a string
     */
    String generateApplicantReport(List<Applicant> applicants);
    
    /**
     * Generates a report on flat bookings.
     * 
     * @param projects The list of projects to include in the report
     * @return The generated report as a string
     */
    String generateFlatBookingReport(List<Project> projects);
    
    /**
     * Generates a report on projects.
     * 
     * @param projects The list of projects to include in the report
     * @return The generated report as a string
     */
    String generateProjectReport(List<Project> projects);
    
    /**
     * Generates a report on applicants filtered by marital status.
     * 
     * @param maritalStatus The marital status to filter by
     * @return The generated report as a string
     */
    String generateApplicantReportByMaritalStatus(String maritalStatus);
    
    /**
     * Generates a report on applicants filtered by age range.
     * 
     * @param minAge The minimum age
     * @param maxAge The maximum age
     * @return The generated report as a string
     */
    String generateApplicantReportByAgeRange(int minAge, int maxAge);
    
    /**
     * Generates a report on applicants filtered by flat type.
     * 
     * @param flatType The flat type to filter by
     * @return The generated report as a string
     */
    String generateApplicantReportByFlatType(String flatType);
    
    /**
     * Generates a report on applicants filtered by application status.
     * 
     * @param status The application status to filter by
     * @return The generated report as a string
     */
    String generateApplicantReportByStatus(String status);
    
    /**
     * Generates a report on applicants for a specific project.
     * 
     * @param projectId The ID of the project
     * @return The generated report as a string
     */
    String generateApplicantReportByProject(int projectId);
    
    /**
     * Generates a receipt for a specific applicant.
     * 
     * @param applicantNric The NRIC of the applicant
     * @return The generated receipt as a string
     */
    String generateReceipt(String applicantNric);
    
    /**
     * Exports a report to a file.
     * 
     * @param report The report to export
     * @param filePath The path of the file to export to
     * @return true if the export was successful, false otherwise
     */
    boolean exportReportToFile(String report, String filePath);
}
