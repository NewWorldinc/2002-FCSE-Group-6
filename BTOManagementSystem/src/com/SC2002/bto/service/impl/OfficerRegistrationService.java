package com.SC2002.bto.service.impl;

import com.SC2002.bto.repository.IOfficerRegistrationRepository;
import com.SC2002.bto.repository.IProjectRepository;
import com.SC2002.bto.service.IOfficerRegistrationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the officer registration service.
 * Follows the Dependency Inversion Principle by depending on repository interfaces.
 */
public class OfficerRegistrationService implements IOfficerRegistrationService {
    
    private final IOfficerRegistrationRepository officerRegistrationRepository;
    private final IProjectRepository projectRepository;
    
    /**
     * Constructs an OfficerRegistrationService with the specified repositories.
     * 
     * @param officerRegistrationRepository The officer registration repository
     * @param projectRepository The project repository
     */
    public OfficerRegistrationService(IOfficerRegistrationRepository officerRegistrationRepository, IProjectRepository projectRepository) {
        this.officerRegistrationRepository = officerRegistrationRepository;
        this.projectRepository = projectRepository;
    }
    
    @Override
    public boolean registerForProject(String officerNric, int projectId) {
        // Check if the officer is eligible to register
        if (!isEligibleForRegistration(officerNric, projectId)) {
            return false;
        }
        
        // Register the officer
        return officerRegistrationRepository.register(officerNric, projectId);
    }
    
    @Override
    public boolean approveRegistration(String officerNric, int projectId) {
        // Check if the registration exists
        if (!hasPendingRegistration(officerNric, projectId)) {
            return false;
        }
        
        // Approve the registration
        return officerRegistrationRepository.approve(officerNric, projectId);
    }
    
    @Override
    public boolean rejectRegistration(String officerNric, int projectId) {
        // Check if the registration exists
        if (!hasPendingRegistration(officerNric, projectId)) {
            return false;
        }
        
        // Reject the registration
        return officerRegistrationRepository.reject(officerNric, projectId);
    }
    
    @Override
    public List<Integer> getPendingRegistrationsByOfficer(String officerNric) {
        return officerRegistrationRepository.findPendingByOfficer(officerNric);
    }
    
    @Override
    public List<String> getPendingRegistrationsByProject(int projectId) {
        return officerRegistrationRepository.findPendingByProject(projectId);
    }
    
    @Override
    public List<OfficerProjectPair> getAllPendingRegistrations() {
        List<OfficerProjectPair> result = new ArrayList<>();
        
        // Get all projects
        projectRepository.findAll().forEach(project -> {
            int projectId = project.getProjectId();
            
            // Get all pending registrations for this project
            List<String> officerNrics = officerRegistrationRepository.findPendingByProject(projectId);
            
            // Create OfficerProjectPair objects
            officerNrics.forEach(officerNric -> {
                result.add(new OfficerProjectPair(officerNric, projectId));
            });
        });
        
        return result;
    }
    
    @Override
    public boolean isEligibleForRegistration(String officerNric, int projectId) {
        // Check if the project exists
        if (!projectRepository.existsById(projectId)) {
            return false;
        }
        
        // Check if the officer already has a pending registration for this project
        if (hasPendingRegistration(officerNric, projectId)) {
            return false;
        }
        
        // Check if the project has reached its registration limit
        return !hasReachedRegistrationLimit(projectId);
    }
    
    @Override
    public boolean hasPendingRegistration(String officerNric, int projectId) {
        return officerRegistrationRepository.hasPendingRegistration(officerNric, projectId);
    }
    
    @Override
    public int getPendingRegistrationCount(int projectId) {
        return officerRegistrationRepository.getPendingRegistrationCount(projectId);
    }
    
    @Override
    public boolean hasReachedRegistrationLimit(int projectId) {
        // Check if the project exists
        return projectRepository.findById(projectId)
            .map(project -> {
                // Get the number of pending registrations
                int pendingCount = getPendingRegistrationCount(projectId);
                
                // Get the number of officer slots
                int officerSlots = project.getOfficerSlots();
                
                // Check if the number of pending registrations has reached the limit
                return pendingCount >= officerSlots;
            })
            .orElse(true); // If the project doesn't exist, consider it as having reached the limit
    }
}
