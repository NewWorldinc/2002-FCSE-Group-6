package com.SC2002.bto.control;

import com.SC2002.bto.di.ServiceLocator;
import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.ApplicationStatus;
import com.SC2002.bto.service.IApplicationService;
import com.SC2002.bto.utils.InputValidator;

/**
 * Handles applicant actions: apply and withdraw.
 * Follows the Dependency Inversion Principle by depending on the service interface.
 */
public class ApplicationController {
    
    private final IApplicationService applicationService;
    
    /**
     * Constructs an ApplicationController with the default application service.
     */
    public ApplicationController() {
        this.applicationService = ServiceLocator.get(IApplicationService.class);
    }
    
    /**
     * Constructs an ApplicationController with the specified application service.
     * 
     * @param applicationService The application service
     */
    public ApplicationController(IApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Applies for a project: checks eligibility, records flat type,
     * sets status to PENDING, and persists the applicant.
     */
    public boolean applyForProject(Applicant applicant, int projectId, String flatType) {
        // Check if the user is an HDBManager (by class type)
        if (!(applicant instanceof Applicant)) {
            System.out.println("HDB Managers are not allowed to apply for BTO projects.");
            return false;
        }
        
        // Special handling for officers acting as applicants
        if (applicant instanceof com.SC2002.bto.entities.OfficerAsApplicant) {
            com.SC2002.bto.entities.OfficerAsApplicant officerApplicant = 
                (com.SC2002.bto.entities.OfficerAsApplicant) applicant;
                
            // Check if the officer can apply for this project
            if (!officerApplicant.canApplyForProject(projectId)) {
                System.out.println("You cannot apply for this project as you are assigned to it as an officer or have a pending registration.");
                return false;
            }
        }
        
        ApplicationStatus status = applicant.getApplicationStatus();

        if (status == ApplicationStatus.SUCCESSFUL || status == ApplicationStatus.BOOKED) {
            System.out.println("You already have an approved or booked application.");
            return false;
        }

        if (status == ApplicationStatus.UNSUCCESSFUL) {
            applicant.setAppliedProjectId(-1);
            applicant.setApplicationStatus(ApplicationStatus.NOT_APPLIED);
            applicant.setAppliedFlatType("");
        }

        if (applicant.getAppliedProjectId() != -1 &&
            applicant.getApplicationStatus() != ApplicationStatus.NOT_APPLIED) {
            System.out.println("You have a pending application.");
            return false;
        }

        // Eligibility check using InputValidator
        String maritalStatus = applicant.getMaritalStatus();
        int age = applicant.getAge();

        if (!InputValidator.validateMaritalStatus(maritalStatus)) {
            System.out.println("Invalid marital status: " + maritalStatus);
            return false;
        }

        if (!InputValidator.validateFlatTypeEligibility(flatType, maritalStatus)) {
            if ("Single".equalsIgnoreCase(maritalStatus)) {
                System.out.println("Singles can only apply for 2-Room flats.");
            } else {
                System.out.println("Married applicants can only apply for 2-Room or 3-Room flats.");
            }
            return false;
        }

        if ("Single".equalsIgnoreCase(maritalStatus) && !InputValidator.validateSingleApplicantAge(age, maritalStatus)) {
            System.out.println("Singles must be 35 years or older to apply.");
            return false;
        }

        if ("Married".equalsIgnoreCase(maritalStatus) && !InputValidator.validateMarriedApplicantAge(age, maritalStatus)) {
            System.out.println("Married applicants must be 21 years or older to apply.");
            return false;
        }

        // Use the application service to apply for the project
        return applicationService.applyForProject(applicant, projectId, flatType);
    }

    /**
     * Requests withdrawal of an application, setting status to PENDING_WITHDRAWAL.
     * HDBManager will need to approve or reject the withdrawal request.
     */
    public boolean requestWithdrawal(Applicant applicant) {
        ApplicationStatus status = applicant.getApplicationStatus();

        if (!(status == ApplicationStatus.PENDING
              || status == ApplicationStatus.SUCCESSFUL
              || status == ApplicationStatus.BOOKED)) {
            System.out.println("No active application to withdraw.");
            return false;
        }

        // Set to pending withdrawal instead of immediately processing
        applicant.setApplicationStatus(ApplicationStatus.PENDING_WITHDRAWAL);
        
        // Use the application service to update the application status
        return applicationService.processApplication(applicant.getNric(), ApplicationStatus.PENDING_WITHDRAWAL);
    }
    
    /**
     * Approves a withdrawal request, resetting the application.
     * Inventory restoration—if needed—happens in the HDB Officer's booking flow.
     */
    public boolean approveWithdrawal(Applicant applicant) {
        ApplicationStatus status = applicant.getApplicationStatus();

        if (status != ApplicationStatus.PENDING_WITHDRAWAL) {
            System.out.println("No pending withdrawal request to approve.");
            return false;
        }

        // Use the application service to withdraw the application
        return applicationService.withdrawApplication(applicant);
    }
    
    /**
     * Rejects a withdrawal request, reverting the status to the previous status.
     */
    public boolean rejectWithdrawal(Applicant applicant) {
        ApplicationStatus status = applicant.getApplicationStatus();

        if (status != ApplicationStatus.PENDING_WITHDRAWAL) {
            System.out.println("No pending withdrawal request to reject.");
            return false;
        }

        // Revert to PENDING status (simplification - in a real system we would store the previous status)
        return applicationService.processApplication(applicant.getNric(), ApplicationStatus.PENDING);
    }
    
    /**
     * Legacy method for backward compatibility.
     * Now uses requestWithdrawal internally.
     */
    public boolean withdrawApplication(Applicant applicant) {
        return requestWithdrawal(applicant);
    }
}
