// File: HDBManager.java
package com.SC2002.bto.entities;

import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;
import com.SC2002.bto.utils.ProjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HDBManager represents an HDB manager, a user who manages project listings.
 * This class has been refactored to extend BTOSystemUser and implement ProjectInteractor.
 * 
 * Following SOLID principles:
 * - Single Responsibility: Handles manager-specific functionality
 * - Liskov Substitution: Can be used wherever BTOSystemUser is expected
 */
public class HDBManager extends BTOSystemUser {

    // Track the project IDs managed by this manager
    private List<Integer> managedProjectIds;

    /**
     * Constructs an HDBManager.
     *
     * @param name          the manager's name.
     * @param nric          the manager's NRIC.
     * @param password      the manager's password.
     * @param age           the manager's age.
     * @param maritalStatus the manager's marital status.
     */
    public HDBManager(String name, String nric, String password, int age, String maritalStatus) {
        super(name, nric, password, age, maritalStatus);
        this.managedProjectIds = new ArrayList<>();
    }

    /**
     * Adds a project ID to the list of projects managed by this manager.
     * 
     * @param projectId the project ID to add
     */
    public void addManagedProjectId(int projectId) {
        if (!managedProjectIds.contains(projectId)) {
            managedProjectIds.add(projectId);
        }
    }

    /**
     * Gets the list of project IDs managed by this manager.
     * 
     * @return the list of managed project IDs
     */
    public List<Integer> getManagedProjectIds() {
        return managedProjectIds;
    }

    /**
     * Returns a string representation of this manager.
     * 
     * @return a string containing the manager's name, NRIC, and number of managed projects
     */
    @Override
    public String toString() {
        return String.format("Manager: %s (%s) | Managing %d project(s)", getName(), getNric(), managedProjectIds.size());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Project> getViewableProjects() {
        // Managers can view all projects
        return getAllProjects();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canInteractWithProject(int projectId) {
        // Managers can interact with any project
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Enquiry> getRelevantEnquiries() {
        // For managers, relevant enquiries are those for projects they manage
        List<Project> managedProjects = getAllProjects().stream()
            .filter(p -> p.getManager().equalsIgnoreCase(getName()))
            .collect(Collectors.toList());
            
        if (managedProjects.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Integer> managedProjectIds = managedProjects.stream()
            .map(Project::getProjectId)
            .collect(Collectors.toList());
            
        return FileManager.loadAllEnquiries(Constants.ENQUIRY_CSV).stream()
            .filter(e -> managedProjectIds.contains(e.getProjectId()))
            .collect(Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserCsvPath() {
        return Constants.MANAGER_CSV;
    }
}
