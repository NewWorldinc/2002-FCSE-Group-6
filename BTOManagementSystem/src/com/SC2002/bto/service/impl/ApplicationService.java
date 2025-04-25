package com.SC2002.bto.service.impl;

import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.ApplicationStatus;
import com.SC2002.bto.repository.IApplicationRepository;
import com.SC2002.bto.repository.IProjectRepository;
import com.SC2002.bto.service.IApplicationService;
import com.SC2002.bto.utils.InputValidator;

import java.util.List;

/**
 * Implementation of the application service.
 * Follows the Dependency Inversion Principle by depending on repository interfaces.
 */
public class ApplicationService implements IApplicationService {
    
    private final IApplicationRepository applicationRepository;
    private final IProjectRepository projectRepository;
    
    /**
     * Constructs an ApplicationService with the specified repositories.
     * 
     * @param applicationRepository The application repository
     * @param projectRepository The project repository
     */
    public ApplicationService(IApplicationRepository applicationRepository, IProjectRepository projectRepository) {
        this.applicationRepository = applicationRepository;
        this.projectRepository = projectRepository;
    }
    
    @Override
    public boolean applyForProject(Applicant applicant, int projectId, String flatType) {
        // Check if the applicant is eligible
        if (!isEligible(applicant, projectId, flatType)) {
            return false;
        }
        
        // Check if the project has available units
        if (!projectRepository.findById(projectId).isPresent()) {
            return false;
        }
        
        // Apply for the project
        return applicationRepository.apply(applicant, projectId, flatType);
    }
    
    @Override
    public boolean withdrawApplication(Applicant applicant) {
        // Check if the applicant has an active application
        if (applicant.getApplicationStatus() == ApplicationStatus.NOT_APPLIED ||
            applicant.getApplicationStatus() == ApplicationStatus.UNSUCCESSFUL) {
            return false;
        }
        
        // Withdraw the application
        return applicationRepository.withdraw(applicant.getNric());
    }
    
    @Override
    public boolean processApplication(String applicantNric, ApplicationStatus status) {
        // Check if the status is valid for processing
        if (status != ApplicationStatus.SUCCESSFUL && 
            status != ApplicationStatus.UNSUCCESSFUL && 
            status != ApplicationStatus.PENDING_WITHDRAWAL) {
            return false;
        }
        
        // Process the application
        return applicationRepository.updateStatus(applicantNric, status);
    }
    
    @Override
    public boolean bookFlat(String applicantNric) {
        // Book the flat
        return applicationRepository.book(applicantNric);
    }
    
    @Override
    public List<Applicant> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }
    
    @Override
    public List<Applicant> getApplicationsByProject(int projectId) {
        return applicationRepository.findByProject(projectId);
    }
    
    @Override
    public List<Applicant> getApplicationsByFlatType(String flatType) {
        return applicationRepository.findByFlatType(flatType);
    }
    
    @Override
    public List<Applicant> getApplicationsByManager(String managerNric) {
        // This would require a more complex implementation that checks which projects
        // the manager is managing and then finds applications for those projects
        // For now, we'll return an empty list
        return List.of();
    }
    
    @Override
    public List<Applicant> getApplicationsByOfficer(String officerNric) {
        // This would require a more complex implementation that checks which projects
        // the officer is assigned to and then finds applications for those projects
        // For now, we'll return an empty list
        return List.of();
    }
    
    @Override
    public boolean isEligible(Applicant applicant, int projectId, String flatType) {
        // Check if the applicant already has an active application
        if (applicant.getApplicationStatus() == ApplicationStatus.PENDING ||
            applicant.getApplicationStatus() == ApplicationStatus.SUCCESSFUL ||
            applicant.getApplicationStatus() == ApplicationStatus.BOOKED) {
            return false;
        }
        
        // Check if the applicant is eligible for the flat type
        if (!validateFlatTypeEligibility(flatType, applicant.getMaritalStatus())) {
            return false;
        }
        
        // Check if the applicant meets the age requirement
        if ("Single".equalsIgnoreCase(applicant.getMaritalStatus())) {
            if (!InputValidator.validateSingleApplicantAge(applicant.getAge(), applicant.getMaritalStatus())) {
                return false;
            }
        } else if ("Married".equalsIgnoreCase(applicant.getMaritalStatus())) {
            if (!InputValidator.validateMarriedApplicantAge(applicant.getAge(), applicant.getMaritalStatus())) {
                return false;
            }
        } else {
            return false; // Invalid marital status
        }
        
        // Check if the project exists and has available units
        return projectRepository.findById(projectId)
            .map(project -> {
                if ("2-Room".equalsIgnoreCase(flatType)) {
                    return project.getType1Units() > 0;
                } else if ("3-Room".equalsIgnoreCase(flatType)) {
                    return project.getType2Units() > 0;
                } else {
                    return false; // Invalid flat type
                }
            })
            .orElse(false);
    }
    
    @Override
    public boolean validateFlatTypeEligibility(String flatType, String maritalStatus) {
        return InputValidator.validateFlatTypeEligibility(flatType, maritalStatus);
    }
}
