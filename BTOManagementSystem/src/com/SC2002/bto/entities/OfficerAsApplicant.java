package com.SC2002.bto.entities;

import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;

import java.util.List;

/**
 * OfficerAsApplicant is an adapter class that wraps an HDBOfficer instance
 * and presents it as an Applicant. This allows officers to apply for BTO projects
 * while maintaining their officer role.
 * 
 * This class follows the Adapter pattern to convert the HDBOfficer interface
 * into the Applicant interface expected by the application system.
 */
public class OfficerAsApplicant extends Applicant {
    
    private HDBOfficer officer;
    
    /**
     * Constructs an OfficerAsApplicant with the specified officer.
     * 
     * @param officer the HDBOfficer to wrap
     */
    public OfficerAsApplicant(HDBOfficer officer) {
        super(officer.getName(), officer.getNric(), officer.getPassword(), 
              officer.getAge(), officer.getMaritalStatus());
        this.officer = officer;
        
        // Load application data from CSV
        loadApplicationData();
    }
    
    /**
     * Gets the wrapped HDBOfficer.
     * 
     * @return the wrapped HDBOfficer
     */
    public HDBOfficer getOfficer() {
        return officer;
    }
    
    /**
     * Loads application data for this officer from the ApplicantList.csv file.
     * This ensures that if an officer has previously applied for a project,
     * that information is loaded correctly.
     */
    private void loadApplicationData() {
        List<User> applicants = FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant");
        
        for (User user : applicants) {
            if (user instanceof Applicant && user.getNric().equals(getNric())) {
                Applicant applicant = (Applicant) user;
                setAppliedProjectId(applicant.getAppliedProjectId());
                setApplicationStatus(applicant.getApplicationStatus());
                setAppliedFlatType(applicant.getAppliedFlatType());
                break;
            }
        }
    }
    
    /**
     * {@inheritDoc}
     * Overrides the changePassword method to ensure that the password is updated
     * in both the officer and applicant records.
     */
    @Override
    public boolean changePassword(String currentPassword, String newPassword) {
        // First, update the officer's password
        if (!officer.getPassword().equals(currentPassword)) {
            return false;
        }
        
        officer.setPassword(newPassword);
        boolean officerUpdated = FileManager.updatePasswordInCSV(Constants.OFFICER_CSV, officer.getNric(), newPassword);
        
        // Then update this instance's password
        setPassword(newPassword);
        
        // If the officer has an application record, update that too
        boolean applicantUpdated = true;
        if (FileManager.applicantNricExists(getNric())) {
            applicantUpdated = FileManager.updatePasswordInCSV(Constants.APPLICANT_CSV, getNric(), newPassword);
        }
        
        return officerUpdated && applicantUpdated;
    }
    
    /**
     * Checks if the officer can apply for a project.
     * Officers cannot apply for projects they are assigned to, have pending registrations for,
     * or that have overlapping time periods with projects they are assigned to.
     * 
     * @param projectId the project ID to check
     * @return true if the officer can apply for the project, false otherwise
     */
    public boolean canApplyForProject(int projectId) {
        // Check if the officer is assigned to this project
        List<Project> assignedProjects = officer.getViewableProjects().stream()
            .filter(p -> p.isOfficerAssigned(officer.getNric()) || p.isOfficerAssigned(officer.getName()))
            .toList();
            
        for (Project p : assignedProjects) {
            if (p.getProjectId() == projectId) {
                return false;
            }
        }
        
        // Check if the officer has pending registrations for this project
        List<Integer> pendingRegistrations = officer.getPendingRegistrations();
        if (pendingRegistrations.contains(projectId)) {
            return false;
        }
        
        // Get the target project
        Project targetProject = officer.getAllProjects().stream()
            .filter(p -> p.getProjectId() == projectId)
            .findFirst()
            .orElse(null);
            
        if (targetProject == null) {
            return false;
        }
        
        // Check for date overlaps with projects the officer is assigned to
        for (Project p : assignedProjects) {
            // Check if application periods overlap
            if (!(targetProject.getApplicationClosingDate().isBefore(p.getApplicationOpeningDate()) || 
                  targetProject.getApplicationOpeningDate().isAfter(p.getApplicationClosingDate()))) {
                return false;
            }
        }
        
        return true;
    }
}
