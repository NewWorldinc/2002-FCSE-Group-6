// File: src/com/SC2002/bto/entities/Applicant.java
package com.SC2002.bto.entities;

import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;
import com.SC2002.bto.utils.InputValidator;
import com.SC2002.bto.utils.ProjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Applicant represents a user who can apply for BTO projects.
 * This class has been refactored to extend BTOSystemUser and implement ProjectInteractor.
 * 
 * Following SOLID principles:
 * - Single Responsibility: Handles applicant-specific functionality
 * - Liskov Substitution: Can be used wherever BTOSystemUser is expected
 */
public class Applicant extends BTOSystemUser {
    
    private int appliedProjectId;
    private ApplicationStatus applicationStatus;
    private String appliedFlatType;
    
    /**
     * Constructs an Applicant with the specified details.
     *
     * @param name the applicant's full name.
     * @param nric the applicant's NRIC.
     * @param password the applicant's password.
     * @param age the applicant's age.
     * @param maritalStatus the applicant's marital status.
     */
    public Applicant(String name, String nric, String password, int age, String maritalStatus) {
        super(name, nric, password, age, maritalStatus);
        this.appliedProjectId = -1;
        this.applicationStatus = ApplicationStatus.NOT_APPLIED;
        this.appliedFlatType = "";      // default: none
    }
    
    /**
     * Gets the ID of the project the applicant has applied for.
     * 
     * @return the applied project ID, or -1 if not applied
     */
    public int getAppliedProjectId() {
        return appliedProjectId;
    }
    
    /**
     * Sets the ID of the project the applicant has applied for.
     * 
     * @param appliedProjectId the applied project ID
     */
    public void setAppliedProjectId(int appliedProjectId) {
        this.appliedProjectId = appliedProjectId;
    }
    
    /**
     * Gets the application status.
     * 
     * @return the application status
     */
    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    /**
     * Sets the application status.
     * 
     * @param applicationStatus the application status
     */
    public void setApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    /**
     * Gets the flat type the applicant has applied for.
     * 
     * @return the applied flat type
     */
    public String getAppliedFlatType() {
        return appliedFlatType;
    }
    
    /**
     * Sets the flat type the applicant has applied for.
     * 
     * @param appliedFlatType the applied flat type
     */
    public void setAppliedFlatType(String appliedFlatType) {
        this.appliedFlatType = appliedFlatType;
    }

    /**
     * Resets all application data.
     */
    public void resetApplication() {
        this.appliedProjectId = -1;
        this.applicationStatus = ApplicationStatus.NOT_APPLIED;
        this.appliedFlatType = "";
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Project> getViewableProjects() {
        List<Project> allProjects = getAllProjects();
        if (allProjects.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Project> viewableProjects = new ArrayList<>();
        String ms = getMaritalStatus();
        int age = getAge();
        
        // Validate marital status
        if (!InputValidator.validateMaritalStatus(ms)) {
            return new ArrayList<>();
        }
        
        // Include the project the applicant has already applied for, if any
        if (appliedProjectId != -1) {
            allProjects.stream()
                .filter(p -> p.getProjectId() == appliedProjectId)
                .findFirst()
                .ifPresent(viewableProjects::add);
        }
        
        // Filter projects by visibility and application period
        List<Project> visibleProjects = filterByVisibilityAndPeriod(allProjects);
        
        // Apply eligibility criteria based on marital status and age
        for (Project p : visibleProjects) {
            if ("Single".equalsIgnoreCase(ms)) {
                if (!InputValidator.validateSingleApplicantAge(age, ms)) {
                    continue;
                }
                boolean has2Room = "2-Room".equalsIgnoreCase(p.getType1Desc()) || 
                                  "2-Room".equalsIgnoreCase(p.getType2Desc());
                if (has2Room) {
                    viewableProjects.add(p);
                }
            } else if ("Married".equalsIgnoreCase(ms)) {
                if (!InputValidator.validateMarriedApplicantAge(age, ms)) {
                    continue;
                }
                viewableProjects.add(p);
            }
        }
        
        return viewableProjects;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canInteractWithProject(int projectId) {
        // Applicant can interact with a project if:
        // 1. They have already applied for it, or
        // 2. It's in their viewable projects list
        if (projectId == appliedProjectId) {
            return true;
        }
        
        return getViewableProjects().stream()
            .anyMatch(p -> p.getProjectId() == projectId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Enquiry> getRelevantEnquiries() {
        // For applicants, relevant enquiries are those they submitted
        return FileManager.loadAllEnquiries(Constants.ENQUIRY_CSV).stream()
            .filter(e -> e.getUserNric().equalsIgnoreCase(getNric()))
            .collect(Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserCsvPath() {
        return Constants.APPLICANT_CSV;
    }
    
    @Override
    public String toString() {
        return String.format("Applicant: %s (%s) | Status: %s", 
            getName(), getNric(), applicationStatus);
    }
}
