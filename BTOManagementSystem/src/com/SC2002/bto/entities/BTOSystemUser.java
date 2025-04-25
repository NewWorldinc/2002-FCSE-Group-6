package com.SC2002.bto.entities;

import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;
import com.SC2002.bto.utils.InputValidator;
import com.SC2002.bto.utils.ProjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BTOSystemUser is an abstract class that extends User and implements ProjectInteractor.
 * It provides common functionality for users who interact with the BTO system.
 * 
 * This class is part of a refactoring to improve code reuse between Applicant and HDBOfficer classes.
 * 
 * Following SOLID principles:
 * - Single Responsibility: Handles common user functionality
 * - Open/Closed: Open for extension by Applicant and HDBOfficer
 * - Liskov Substitution: Subclasses can be used wherever BTOSystemUser is expected
 */
public abstract class BTOSystemUser extends User implements ProjectInteractor {
    
    /**
     * Constructs a BTOSystemUser with the specified details.
     *
     * @param name the user's full name.
     * @param nric the user's NRIC.
     * @param password the user's password.
     * @param age the user's age.
     * @param maritalStatus the user's marital status.
     */
    public BTOSystemUser(String name, String nric, String password, int age, String maritalStatus) {
        super(name, nric, password, age, maritalStatus);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changePassword(String currentPassword, String newPassword) {
        // Verify current password
        if (!getPassword().equals(currentPassword)) {
            return false;
        }
        
        // Validate new password
        if (!InputValidator.validatePassword(newPassword)) {
            return false;
        }
        
        // Update password
        setPassword(newPassword);
        
        // Determine which CSV file to update based on user type
        String csvPath = getUserCsvPath();
        
        // Update password in CSV file
        return FileManager.updatePasswordInCSV(csvPath, getNric(), newPassword);
    }
    
    /**
     * Gets the path to the CSV file for this user type.
     * 
     * @return the path to the CSV file
     */
    protected abstract String getUserCsvPath();
    
    /**
     * Gets the list of all projects from the repository.
     * 
     * @return the list of all projects
     */
    protected List<Project> getAllProjects() {
        // Refresh the project repository to ensure we have the latest data
        ProjectRepository.refresh();
        return ProjectRepository.getAll();
    }
    
    /**
     * Filters projects based on visibility and application period.
     * 
     * @param projects the list of projects to filter
     * @return the filtered list of projects
     */
    protected List<Project> filterByVisibilityAndPeriod(List<Project> projects) {
        return projects.stream()
            .filter(p -> p.isVisible() && p.isCurrentlyOpen())
            .collect(Collectors.toList());
    }
}
