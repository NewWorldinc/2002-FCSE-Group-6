// File: HDBOfficer.java
package com.SC2002.bto.entities;

import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;
import com.SC2002.bto.utils.ProjectRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HDBOfficer represents an HDB officer who can manage projects and process applications.
 * This class has been refactored to extend BTOSystemUser and implement ProjectInteractor.
 * 
 * Following SOLID principles:
 * - Single Responsibility: Handles officer-specific functionality
 * - Liskov Substitution: Can be used wherever BTOSystemUser is expected
 */
public class HDBOfficer extends BTOSystemUser {

    // Track project IDs the officer has registered for
    private List<Integer> registeredProjectIds;

    /**
     * Constructs an HDBOfficer.
     *
     * @param name          the officer's name.
     * @param nric          the officer's NRIC.
     * @param password      the officer's password.
     * @param age           the officer's age.
     * @param maritalStatus the officer's marital status.
     */
    public HDBOfficer(String name, String nric, String password, int age, String maritalStatus) {
        super(name, nric, password, age, maritalStatus);
        this.registeredProjectIds = new ArrayList<>();
    }

    /**
     * Adds a project ID to the list of registered projects.
     * 
     * @param projectId the project ID to add
     */
    public void addRegisteredProjectId(int projectId) {
        if (!registeredProjectIds.contains(projectId)) {
            registeredProjectIds.add(projectId);
        }
    }

    /**
     * Gets the list of registered project IDs.
     * 
     * @return the list of registered project IDs
     */
    public List<Integer> getRegisteredProjectIds() {
        return registeredProjectIds;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Project> getViewableProjects() {
        List<Project> allProjects = getAllProjects();
        
        // Officers can view all projects they're assigned to, regardless of visibility
        List<Project> assignedProjects = allProjects.stream()
            .filter(p -> p.isOfficerAssigned(getNric()) || p.isOfficerAssigned(getName()))
            .collect(Collectors.toList());
            
        // For registration purposes, officers can also view visible projects with available slots
        List<Project> availableProjects = filterByVisibilityAndPeriod(allProjects).stream()
            .filter(p -> p.getOfficerSlots() > 0)
            .collect(Collectors.toList());
            
        // Combine both lists (assigned projects and available projects)
        List<Project> viewableProjects = new ArrayList<>(assignedProjects);
        
        // Add available projects that aren't already in the list
        for (Project p : availableProjects) {
            if (!viewableProjects.contains(p)) {
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
        // Officer can interact with a project if:
        // 1. They are assigned to it, or
        // 2. It's available for registration
        List<Project> projects = getViewableProjects();
        return projects.stream()
            .anyMatch(p -> p.getProjectId() == projectId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Enquiry> getRelevantEnquiries() {
        // Get the list of projects the officer is assigned to
        List<Project> assignedProjects = getAllProjects().stream()
            .filter(p -> p.isOfficerAssigned(getNric()) || p.isOfficerAssigned(getName()))
            .collect(Collectors.toList());
            
        if (assignedProjects.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Get the project IDs the officer is assigned to
        List<Integer> assignedProjectIds = assignedProjects.stream()
            .map(Project::getProjectId)
            .collect(Collectors.toList());
            
        // Load all enquiries and filter to only those for the officer's assigned projects
        return FileManager.loadAllEnquiries(Constants.ENQUIRY_CSV).stream()
            .filter(e -> assignedProjectIds.contains(e.getProjectId()))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets the list of pending registrations for this officer.
     * 
     * @return the list of pending project IDs
     */
    public List<Integer> getPendingRegistrations() {
        List<Integer> pendingProjectIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(Constants.REGISTRATION_CSV))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 2) continue;
                
                String nric = tokens[0].trim();
                if (nric.equalsIgnoreCase(getNric())) {
                    int projectId = Integer.parseInt(tokens[1].trim());
                    pendingProjectIds.add(projectId);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading registration file: " + e.getMessage());
        }
        return pendingProjectIds;
    }
    
    /**
     * Checks if the officer can register for a project.
     * This checks both projects the officer is assigned to as an officer and
     * projects the officer has applied for as an applicant.
     * 
     * @param projectId the project ID to check
     * @return true if the officer can register for the project, false otherwise
     */
    public boolean canRegisterForProject(int projectId) {
        // Check if officer has already registered for this project
        if (registeredProjectIds.contains(projectId) || getPendingRegistrations().contains(projectId)) {
            return false;
        }
        
        // Check if officer is already assigned to another project in the same period
        List<Project> assignedProjects = getAllProjects().stream()
            .filter(p -> p.isOfficerAssigned(getNric()) || p.isOfficerAssigned(getName()))
            .collect(Collectors.toList());
            
        Project targetProject = getAllProjects().stream()
            .filter(p -> p.getProjectId() == projectId)
            .findFirst()
            .orElse(null);
            
        if (targetProject == null) {
            return false;
        }
        
        // Check for date overlaps with assigned projects
        for (Project p : assignedProjects) {
            // Check if application periods overlap
            if (!(targetProject.getApplicationClosingDate().isBefore(p.getApplicationOpeningDate()) || 
                  targetProject.getApplicationOpeningDate().isAfter(p.getApplicationClosingDate()))) {
                return false;
            }
        }
        
        // Check if the officer has applied for any projects as an applicant
        List<User> applicants = FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant");
        for (User user : applicants) {
            if (user instanceof Applicant && user.getNric().equalsIgnoreCase(getNric())) {
                Applicant applicant = (Applicant) user;
                int appliedProjectId = applicant.getAppliedProjectId();
                
                // Skip if the officer hasn't applied for any project or if the application was unsuccessful
                if (appliedProjectId == -1 || 
                    applicant.getApplicationStatus() == ApplicationStatus.NOT_APPLIED || 
                    applicant.getApplicationStatus() == ApplicationStatus.UNSUCCESSFUL) {
                    continue;
                }
                
                // Find the project the officer has applied for
                Project appliedProject = getAllProjects().stream()
                    .filter(p -> p.getProjectId() == appliedProjectId)
                    .findFirst()
                    .orElse(null);
                    
                if (appliedProject != null) {
                    // Check if application periods overlap
                    if (!(targetProject.getApplicationClosingDate().isBefore(appliedProject.getApplicationOpeningDate()) || 
                          targetProject.getApplicationOpeningDate().isAfter(appliedProject.getApplicationClosingDate()))) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserCsvPath() {
        return Constants.OFFICER_CSV;
    }

    @Override
    public String toString() {
        return String.format("Officer: %s (%s) | Registered in %d project(s)", 
            getName(), getNric(), registeredProjectIds.size());
    }
}
