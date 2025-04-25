package com.SC2002.bto.service.impl;

import com.SC2002.bto.entities.Project;
import com.SC2002.bto.repository.IProjectRepository;
import com.SC2002.bto.service.IProjectService;
import com.SC2002.bto.utils.FileManager;
import com.SC2002.bto.utils.InputValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the project service.
 * Follows the Dependency Inversion Principle by depending on the repository interface.
 */
public class ProjectService implements IProjectService {
    
    private final IProjectRepository projectRepository;
    
    /**
     * Constructs a ProjectService with the specified repository.
     * 
     * @param projectRepository The project repository
     */
    public ProjectService(IProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }
    
    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
    
    @Override
    public List<Project> getVisibleProjects() {
        return projectRepository.findAllVisible();
    }
    
    @Override
    public Optional<Project> getProjectById(int projectId) {
        return projectRepository.findById(projectId);
    }
    
    @Override
    public List<Project> getProjectsByNeighborhood(String neighborhood) {
        return projectRepository.findByNeighborhood(neighborhood);
    }
    
    @Override
    public List<Project> getProjectsByFlatType(String flatType) {
        return projectRepository.findByFlatType(flatType);
    }
    
    @Override
    public List<Project> getProjectsByManager(String managerNric) {
        return projectRepository.findByManager(managerNric);
    }
    
    @Override
    public List<Project> getProjectsByOfficer(String officerNric) {
        return projectRepository.findByOfficer(officerNric);
    }
    
    @Override
    public List<Project> getProjectsByApplicationPeriod(LocalDate date) {
        return projectRepository.findByApplicationPeriod(date);
    }
    
    @Override
    public boolean updateVisibility(int projectId, boolean isVisible) {
        return projectRepository.updateVisibility(projectId, isVisible);
    }
    
    @Override
    public boolean updateAvailableUnits(int projectId, String flatType, int units) {
        return projectRepository.updateAvailableUnits(projectId, flatType, units);
    }
    
    @Override
    public boolean bookUnit(int projectId, String flatType) {
        return projectRepository.bookUnit(projectId, flatType);
    }
    
    @Override
    public List<Project> getEligibleProjects(int age, String maritalStatus) {
        // Filter projects based on eligibility criteria
        return getVisibleProjects().stream()
            .filter(project -> {
                // Check if the applicant is eligible for any flat type in this project
                boolean eligible2Room = InputValidator.validateFlatTypeEligibility("2-Room", maritalStatus);
                boolean eligible3Room = InputValidator.validateFlatTypeEligibility("3-Room", maritalStatus);
                
                // Check age eligibility
                boolean ageEligible = false;
                if ("Single".equalsIgnoreCase(maritalStatus)) {
                    ageEligible = InputValidator.validateSingleApplicantAge(age, maritalStatus);
                } else if ("Married".equalsIgnoreCase(maritalStatus)) {
                    ageEligible = InputValidator.validateMarriedApplicantAge(age, maritalStatus);
                }
                
                // Check if there are available units of eligible flat types
                boolean hasEligibleUnits = (eligible2Room && project.getType1Units() > 0) || 
                                          (eligible3Room && project.getType2Units() > 0);
                
                return ageEligible && hasEligibleUnits;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean hasAvailableUnits(int projectId, String flatType) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            if ("2-Room".equalsIgnoreCase(flatType)) {
                return project.getType1Units() > 0;
            } else if ("3-Room".equalsIgnoreCase(flatType)) {
                return project.getType2Units() > 0;
            }
        }
        return false;
    }
    
    @Override
    public int getBookedUnits(int projectId, String flatType) {
        return FileManager.getBookedUnits(projectId, flatType);
    }
    
    /**
     * Gets projects that are currently open for application.
     * 
     * @return A list of projects that are currently open for application
     */
    public List<Project> getCurrentlyOpenProjects() {
        LocalDate today = LocalDate.now();
        return getVisibleProjects().stream()
            .filter(p -> !today.isBefore(p.getApplicationOpeningDate()) && 
                         !today.isAfter(p.getApplicationClosingDate()))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds projects by keyword in name or neighborhood.
     * 
     * @param keyword The keyword to search for
     * @return A list of projects matching the keyword
     */
    public List<Project> findByKeyword(String keyword) {
        return getAllProjects().stream()
            .filter(p -> p.getProjectName().toLowerCase().contains(keyword.toLowerCase()) || 
                         p.getNeighborhood().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
    }
}
