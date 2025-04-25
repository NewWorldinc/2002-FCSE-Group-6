package com.SC2002.bto.repository.csv;

import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.ApplicationStatus;
import com.SC2002.bto.repository.IApplicationRepository;
import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CSV-based implementation of the application repository.
 * Follows the Single Responsibility Principle by focusing only on application data access.
 */
public class CSVApplicationRepository implements IApplicationRepository {
    
    /**
     * Finds applications by their status.
     * 
     * @param status The application status to search for
     * @return A list of applicants with the specified application status
     */
    @Override
    public List<Applicant> findByStatus(ApplicationStatus status) {
        List<Applicant> applicants = loadApplicants();
        return applicants.stream()
            .filter(a -> a.getApplicationStatus() == status)
            .collect(Collectors.toList());
    }
    
    /**
     * Finds applications for a specific project.
     * 
     * @param projectId The ID of the project
     * @return A list of applicants who have applied for the specified project
     */
    @Override
    public List<Applicant> findByProject(int projectId) {
        List<Applicant> applicants = loadApplicants();
        return applicants.stream()
            .filter(a -> a.getAppliedProjectId() == projectId)
            .collect(Collectors.toList());
    }
    
    /**
     * Finds applications for a specific flat type.
     * 
     * @param flatType The flat type to search for (e.g., "2-Room", "3-Room")
     * @return A list of applicants who have applied for the specified flat type
     */
    @Override
    public List<Applicant> findByFlatType(String flatType) {
        List<Applicant> applicants = loadApplicants();
        return applicants.stream()
            .filter(a -> a.getAppliedFlatType().equalsIgnoreCase(flatType))
            .collect(Collectors.toList());
    }
    
    /**
     * Updates the status of an application.
     * 
     * @param applicantNric The NRIC of the applicant
     * @param status The new application status
     * @return true if the status was updated successfully, false otherwise
     */
    @Override
    public boolean updateStatus(String applicantNric, ApplicationStatus status) {
        List<Applicant> applicants = loadApplicants();
        for (Applicant applicant : applicants) {
            if (applicant.getNric().equalsIgnoreCase(applicantNric)) {
                applicant.setApplicationStatus(status);
                return FileManager.updateApplicantApplication(applicant);
            }
        }
        return false;
    }
    
    /**
     * Applies for a project.
     * 
     * @param applicant The applicant
     * @param projectId The ID of the project
     * @param flatType The flat type (e.g., "2-Room", "3-Room")
     * @return true if the application was successful, false otherwise
     */
    @Override
    public boolean apply(Applicant applicant, int projectId, String flatType) {
        applicant.setAppliedProjectId(projectId);
        applicant.setApplicationStatus(ApplicationStatus.PENDING);
        applicant.setAppliedFlatType(flatType);
        return FileManager.updateApplicantApplication(applicant);
    }
    
    /**
     * Withdraws an application.
     * 
     * @param applicantNric The NRIC of the applicant
     * @return true if the withdrawal was successful, false otherwise
     */
    @Override
    public boolean withdraw(String applicantNric) {
        List<Applicant> applicants = loadApplicants();
        for (Applicant applicant : applicants) {
            if (applicant.getNric().equalsIgnoreCase(applicantNric)) {
                applicant.setAppliedProjectId(-1);
                applicant.setApplicationStatus(ApplicationStatus.NOT_APPLIED);
                applicant.setAppliedFlatType("");
                return FileManager.updateApplicantApplication(applicant);
            }
        }
        return false;
    }
    
    /**
     * Books a flat for an applicant.
     * 
     * @param applicantNric The NRIC of the applicant
     * @return true if the booking was successful, false otherwise
     */
    @Override
    public boolean book(String applicantNric) {
        List<Applicant> applicants = loadApplicants();
        for (Applicant applicant : applicants) {
            if (applicant.getNric().equalsIgnoreCase(applicantNric)) {
                applicant.setApplicationStatus(ApplicationStatus.BOOKED);
                return FileManager.updateApplicantApplication(applicant);
            }
        }
        return false;
    }
    
    /**
     * Rejects an application.
     * 
     * @param applicantNric The NRIC of the applicant
     * @return true if the rejection was successful, false otherwise
     */
    @Override
    public boolean reject(String applicantNric) {
        List<Applicant> applicants = loadApplicants();
        for (Applicant applicant : applicants) {
            if (applicant.getNric().equalsIgnoreCase(applicantNric)) {
                applicant.setApplicationStatus(ApplicationStatus.UNSUCCESSFUL);
                return FileManager.updateApplicantApplication(applicant);
            }
        }
        return false;
    }
    
    /**
     * Loads all applicants from the CSV file.
     * 
     * @return A list of all applicants
     */
    private List<Applicant> loadApplicants() {
        return FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant").stream()
            .filter(user -> user instanceof Applicant)
            .map(user -> (Applicant) user)
            .collect(Collectors.toList());
    }
}
