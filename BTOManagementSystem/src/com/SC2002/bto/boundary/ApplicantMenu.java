// File: src/com/SC2002/bto/boundary/ApplicantMenu.java
package com.SC2002.bto.boundary;

import com.SC2002.bto.control.ApplicationController;
import com.SC2002.bto.control.ProjectController;
import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.Enquiry;
import com.SC2002.bto.entities.Project;
import com.SC2002.bto.utils.ProjectRepository;
import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;
import com.SC2002.bto.utils.InputValidator;
import com.SC2002.bto.entities.ApplicationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * ApplicantMenu provides the user interface for applicant operations.
 * This class handles all interactions with applicants, including viewing projects,
 * applying for projects, managing enquiries, and handling application withdrawals.
 * Follows the Single Responsibility Principle by focusing only on applicant-specific operations.
 */
public class ApplicantMenu {
    /** Scanner for reading user input */
    private Scanner scanner;
    
    /** The applicant using this menu */
    private Applicant applicant;

    /**
     * Loads all enquiries submitted by the current applicant.
     * 
     * @return a list of enquiries submitted by the current applicant
     */
    private List<Enquiry> loadMyEnquiries() {
        return FileManager.loadAllEnquiries(Constants.ENQUIRY_CSV).stream()
            .filter(e -> e.getUserNric().equalsIgnoreCase(applicant.getNric()))
            .collect(Collectors.toList());
    }

    /**
     * Constructs an ApplicantMenu with the specified scanner and applicant.
     * 
     * @param scanner the scanner for reading user input
     * @param applicant the applicant using this menu
     */
    public ApplicantMenu(Scanner scanner, Applicant applicant) {
        this.scanner = scanner;
        this.applicant = applicant;
    }

    /**
     * Displays the applicant menu and handles user input.
     * This method shows the main menu options for applicants and processes
     * the user's selection until they choose to logout.
     */
    public void displayMenu() {
        while (true) {
            System.out.println("\n--- Applicant Menu ---");
            System.out.println("1. View Available Projects");
            System.out.println("2. Apply for a Project");
            System.out.println("3. View Application Status");
            System.out.println("4. View Enquiries & Responses");
            System.out.println("5. Submit Enquiry");
            System.out.println("6. Edit/Delete Enquiry");
            System.out.println("7. Change Password");
            System.out.println("8. Withdraw Application");
            System.out.println("9. Logout");
            
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1": viewAvailableProjects(); 		  break;
                case "2": applyForProject();       		  break;
                case "3": viewApplicationStatus(); 		  break;
                case "4": viewMyEnquiriesWithResponses(); break;
                case "5": submitEnquiry();           	  break;
                case "6": editOrDeleteEnquiry();    	  break;	 
                case "7": if (changePassword()) return;     break;
                case "8": withdrawApplication();  	      break;
                case "9": return;
                
                default:  System.out.println("Invalid option.");
            }
        }
    }

    /**
     * Displays all projects that the applicant is eligible to view.
     * This method filters projects based on the applicant's marital status and age,
     * showing only projects that match their eligibility criteria.
     */
    private void viewAvailableProjects() {
        System.out.println("\n--- Available Projects ---");
        // Use ProjectController to get currently open projects
        ProjectController projectController = new ProjectController();
        // Refresh the project repository to ensure we have the latest data
        ProjectRepository.refresh();
        List<Project> all = ProjectRepository.getAll();
        if (all.isEmpty()) {
            System.out.println("No projects available.");
            return;
        }
        List<Project> filtered = new ArrayList<>();
        String ms = applicant.getMaritalStatus();
        int age = applicant.getAge();
        int appliedId = applicant.getAppliedProjectId();
        
        // Validate marital status
        if (!InputValidator.validateMaritalStatus(ms)) {
            System.out.println("Invalid marital status in your profile. Please contact support.");
            return;
        }
        
        for (Project p : all) {
            // Show if applicant has applied for this project, regardless of visibility or date
            if (p.getProjectId() == appliedId) {
                filtered.add(p);
                continue;
            }
            // Skip invisible projects or projects outside application period
            if (!p.isVisible() || !p.isCurrentlyOpen()) continue;

            if ("Single".equalsIgnoreCase(ms)) {
                if (!InputValidator.validateSingleApplicantAge(age, ms)) {
                    System.out.println("As a single applicant you must be at least 35 to view any projects.");
                    return;
                }
                boolean has2Room = "2-Room".equalsIgnoreCase(p.getType1Desc()) || "2-Room".equalsIgnoreCase(p.getType2Desc());
                if (has2Room) filtered.add(p);
            } else if ("Married".equalsIgnoreCase(ms)) {
                if (!InputValidator.validateMarriedApplicantAge(age, ms)) {
                    System.out.println("As a married applicant you must be at least 21 to view any projects.");
                    return;
                }
                filtered.add(p);
            }
        }

        if (filtered.isEmpty()) {
            System.out.println("No available projects match your eligibility criteria.");
            return;
        }

        for (Project p : filtered) {
            System.out.println("Project ID: " + p.getProjectId());
            System.out.println("  Name          : " + p.getProjectName());
            System.out.println("  Neighbourhood : " + p.getNeighborhood());

            if ("Single".equalsIgnoreCase(ms) && age >= 35) {
                if ("2-Room".equalsIgnoreCase(p.getType1Desc())) {
                    System.out.println("  Flat Type     : 2-Room | Units: " + p.getType1Units() + " | Price: " + p.getType1Price());
                }
                if ("2-Room".equalsIgnoreCase(p.getType2Desc())) {
                    System.out.println("  Flat Type     : 2-Room | Units: " + p.getType2Units() + " | Price: " + p.getType2Price());
                }
            } else {
                System.out.println("  Type1         : " + p.getType1Desc() + " | Units: " + p.getType1Units() + " | Price: " + p.getType1Price());
                System.out.println("  Type2         : " + p.getType2Desc() + " | Units: " + p.getType2Units() + " | Price: " + p.getType2Price());
            }

            System.out.println("  Period        : " + p.getApplicationOpeningDate() + " to " + p.getApplicationClosingDate());
            System.out.println("  Manager       : " + p.getManager());
            System.out.println("  Officers      : " + String.join(", ", p.getOfficers()) + " (Slots: " + p.getOfficerSlots() + ")");
            System.out.println("-------------------------------------");
        }
    }

    
    /**
     * Handles the process of applying for a project.
     * This method checks the applicant's eligibility, allows them to select a project
     * and flat type, and submits the application.
     */
    private void applyForProject() {
        // First check if the applicant already has an application
        ApplicationStatus status = applicant.getApplicationStatus();
        
        if (status == ApplicationStatus.SUCCESSFUL || status == ApplicationStatus.BOOKED) {
            System.out.println("You already have an approved or booked application.");
            return;
        }
        
        if (applicant.getAppliedProjectId() != -1 && 
            status != ApplicationStatus.NOT_APPLIED && 
            status != ApplicationStatus.UNSUCCESSFUL) {
            System.out.println("You have a pending application.");
            return;
        }
        
        // Get the list of projects the applicant can view (using the same logic as viewAvailableProjects)
        // Refresh the project repository to ensure we have the latest data
        ProjectRepository.refresh();
        List<Project> all = ProjectRepository.getAll();
        if (all.isEmpty()) {
            System.out.println("No projects available.");
            return;
        }
        
        List<Project> viewableProjects = new ArrayList<>();
        String ms = applicant.getMaritalStatus();
        int age = applicant.getAge();
        int appliedId = applicant.getAppliedProjectId();
        
        // Validate marital status
        if (!InputValidator.validateMaritalStatus(ms)) {
            System.out.println("Invalid marital status in your profile. Please contact support.");
            return;
        }
        
        // Apply the same filtering logic as in viewAvailableProjects
        for (Project p : all) {
            // Show if applicant has applied for this project, regardless of visibility or date
            if (p.getProjectId() == appliedId) {
                viewableProjects.add(p);
                continue;
            }
            // Skip invisible projects or projects outside application period
            if (!p.isVisible() || !p.isCurrentlyOpen()) continue;

            if ("Single".equalsIgnoreCase(ms)) {
                if (!InputValidator.validateSingleApplicantAge(age, ms)) {
                    System.out.println("As a single applicant, you must be at least 35 years old to apply for any project.");
                    return;
                }
                boolean has2Room = "2-Room".equalsIgnoreCase(p.getType1Desc()) || "2-Room".equalsIgnoreCase(p.getType2Desc());
                if (has2Room) viewableProjects.add(p);
            } else if ("Married".equalsIgnoreCase(ms)) {
                if (!InputValidator.validateMarriedApplicantAge(age, ms)) {
                    System.out.println("As a married applicant, you must be at least 21 years old to apply for any project.");
                    return;
                }
                viewableProjects.add(p);
            }
        }
        
        if (viewableProjects.isEmpty()) {
            System.out.println("No available projects match your eligibility criteria.");
            return;
        }
        
        // Display available projects for reference
        System.out.println("\nProjects you can apply for:");
        for (Project p : viewableProjects) {
            System.out.printf("ID: %d | %s @ %s%n", p.getProjectId(), p.getProjectName(), p.getNeighborhood());
        }

        System.out.print("\nEnter Project ID to apply: ");
        int pid;
        try {
            pid = Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid ID.");
            return;
        }

        // Check if the project ID is in the list of viewable projects
        Project p = viewableProjects.stream()
                       .filter(proj -> proj.getProjectId() == pid)
                       .findFirst()
                       .orElse(null);
        if (p == null) {
            System.out.println("Invalid Project ID or you are not eligible to apply for this project.");
            return;
        }

        String flatType = "";
        if ("Single".equalsIgnoreCase(ms)) {
            System.out.print("Confirm 2-Room (Y/N): ");
            if (!"Y".equalsIgnoreCase(scanner.nextLine())) {
                System.out.println("Cancelled.");
                return;
            }
            flatType = "2-Room";
            
            // Validate flat type eligibility
            if (!InputValidator.validateFlatTypeEligibility(flatType, ms)) {
                System.out.println("Singles can only apply for 2-Room flats.");
                return;
            }
        } else {
            System.out.print("2-Room? (Y for 2-Room, N for 3-Room): ");
            String r = scanner.nextLine();
            if ("Y".equalsIgnoreCase(r)) flatType = "2-Room";
            else if ("N".equalsIgnoreCase(r)) flatType = "3-Room";
            else {
                System.out.println("Invalid choice.");
                return;
            }
            
            // Validate flat type eligibility
            if (!InputValidator.validateFlatTypeEligibility(flatType, ms)) {
                System.out.println("Married applicants can only apply for 2-Room or 3-Room flats.");
                return;
            }
        }

        boolean ok = new ApplicationController().applyForProject(applicant, pid, flatType);
        System.out.println(ok ? "Applied → Pending." : "Application failed.");
    }

    /**
     * Displays the current status of the applicant's application.
     * This method shows details about the project the applicant has applied for
     * and the current status of their application.
     */
    private void viewApplicationStatus() {
        int appliedProjectId = applicant.getAppliedProjectId();
        ApplicationStatus status = applicant.getApplicationStatus();

        if (appliedProjectId < 0 || status == ApplicationStatus.NOT_APPLIED) {
            System.out.println("You have not applied for any project.");
        } else {
            System.out.printf("Applied Project ID: %d | Status: %s%n", appliedProjectId, status);
            
            // Get project details
            Project p = ProjectRepository.getAll().stream()
                .filter(proj -> proj.getProjectId() == appliedProjectId)
                .findFirst()
                .orElse(null);
                
            if (p != null) {
                System.out.printf("Project: %s @ %s\n", p.getProjectName(), p.getNeighborhood());
                System.out.printf("Flat Type: %s\n", applicant.getAppliedFlatType());
            }
        }
    }
    
    /**
     * Displays all enquiries submitted by the applicant and their responses.
     * This method shows the enquiry text, project ID, and any responses received.
     */
    private void viewMyEnquiriesWithResponses() {
        List<Enquiry> myEnquiries = loadMyEnquiries();
        if (myEnquiries.isEmpty()) {
            System.out.println("You have not submitted any enquiries.");
            return;
        }

        System.out.println("\n--- Your Enquiries & Responses ---");
        for (Enquiry e : myEnquiries) {
            System.out.println("Enquiry ID   : " + e.getEnquiryId());
            System.out.println("Project ID   : " + e.getProjectId());
            System.out.println("Your Message : " + e.getEnquiryText());
            System.out.println("Response     : " + (e.getResponse().isEmpty() ? "(Pending)" : e.getResponse()));
            System.out.println("-----------------------------------------");
        }
    }
    
    /**
     * Handles the process of submitting a new enquiry.
     * This method allows the applicant to select a project and enter enquiry text,
     * then submits the enquiry to the system.
     */
    private void submitEnquiry() {
        // Get the list of projects the applicant can view (using the same logic as viewAvailableProjects)
        // Refresh the project repository to ensure we have the latest data
        ProjectRepository.refresh();
        List<Project> all = ProjectRepository.getAll();
        if (all.isEmpty()) {
            System.out.println("No projects available.");
            return;
        }
        
        List<Project> viewableProjects = new ArrayList<>();
        String ms = applicant.getMaritalStatus();
        int age = applicant.getAge();
        int appliedId = applicant.getAppliedProjectId();
        
        // Validate marital status
        if (!InputValidator.validateMaritalStatus(ms)) {
            System.out.println("Invalid marital status in your profile. Please contact support.");
            return;
        }
        
        // Validate age requirements
        if ("Single".equalsIgnoreCase(ms) && !InputValidator.validateSingleApplicantAge(age, ms)) {
            System.out.println("As a single applicant, you must be at least 35 years old to submit an enquiry for a project.");
            return;
        }

        if ("Married".equalsIgnoreCase(ms) && !InputValidator.validateMarriedApplicantAge(age, ms)) {
            System.out.println("As a married applicant, you must be at least 21 years old to submit an enquiry for a project.");
            return;
        }
        
        // Apply the same filtering logic as in viewAvailableProjects
        for (Project p : all) {
            // Show if applicant has applied for this project, regardless of visibility or date
            if (p.getProjectId() == appliedId) {
                viewableProjects.add(p);
                continue;
            }
            // Skip invisible projects or projects outside application period
            if (!p.isVisible() || !p.isCurrentlyOpen()) continue;

            if ("Single".equalsIgnoreCase(ms)) {
                boolean has2Room = "2-Room".equalsIgnoreCase(p.getType1Desc()) || "2-Room".equalsIgnoreCase(p.getType2Desc());
                if (has2Room) viewableProjects.add(p);
            } else if ("Married".equalsIgnoreCase(ms)) {
                viewableProjects.add(p);
            }
        }
        
        if (viewableProjects.isEmpty()) {
            System.out.println("No available projects match your eligibility criteria.");
            return;
        }
        
        // Display available projects for reference
        System.out.println("\nProjects you can submit enquiries for:");
        for (Project p : viewableProjects) {
            System.out.printf("ID: %d | %s @ %s%n", p.getProjectId(), p.getProjectName(), p.getNeighborhood());
        }

        System.out.print("\nProject ID for enquiry: ");
        int projectId;
        try {
            projectId = Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid project ID.");
            return;
        }

        // Check if the project ID is in the list of viewable projects
        boolean exists = viewableProjects.stream()
            .anyMatch(p -> p.getProjectId() == projectId);
        if (!exists) {
            System.out.println("Invalid Project ID or you are not eligible to submit enquiries for this project.");
            return;
        }

        System.out.print("Enquiry text: ");
        String text = scanner.nextLine();

        List<Enquiry> allEnquiries = FileManager.loadAllEnquiries(Constants.ENQUIRY_CSV);
        int enquiryId = allEnquiries.size() + 1;

        Enquiry newEnquiry = new Enquiry(enquiryId, applicant.getNric(), text, projectId);
        allEnquiries.add(newEnquiry);
        FileManager.saveAllEnquiries(allEnquiries);

        System.out.println("Enquiry #" + enquiryId + " submitted.");
    }


    /**
     * Handles the process of editing or deleting an existing enquiry.
     * This method allows the applicant to select an enquiry and either
     * edit its text or delete it entirely.
     */
    private void editOrDeleteEnquiry() {
        List<Enquiry> myEnquiries = loadMyEnquiries();
        if (myEnquiries.isEmpty()) {
            System.out.println("No enquiries.");
            return;
        }

        System.out.println("Your Enquiries:");
        for (Enquiry e : myEnquiries) {
            System.out.printf("%d: %s%n", e.getEnquiryId(), e.getEnquiryText());
        }

        System.out.print("ID to edit/delete: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid input.");
            return;
        }

        Enquiry selected = myEnquiries.stream()
            .filter(e -> e.getEnquiryId() == id)
            .findFirst()
            .orElse(null);

        if (selected == null) {
            System.out.println("Enquiry not found.");
            return;
        }

        if (selected.getResponse() != null && !selected.getResponse().isEmpty()) {
            System.out.println("This enquiry has already been replied to and cannot be edited or deleted.");
            return;
        }

        System.out.print("1 = Edit,  2 = Delete: ");
        String choice = scanner.nextLine();

        List<Enquiry> allEnquiries = FileManager.loadAllEnquiries(Constants.ENQUIRY_CSV);
        allEnquiries.removeIf(e -> e.getEnquiryId() == id);

        if ("1".equals(choice)) {
            System.out.print("New text: ");
            selected.setEnquiryText(scanner.nextLine());
            allEnquiries.add(selected);
            System.out.println("Enquiry updated.");
        } else if ("2".equals(choice)) {
            System.out.println("Enquiry deleted.");
        } else {
            System.out.println("Invalid choice.");
            return;
        }

        FileManager.saveAllEnquiries(allEnquiries);
    }

    /**
     * Handles the process of changing the applicant's password.
     * This method verifies the current password, validates the new password,
     * and updates it in the system.
     * 
     * @return true if the password was changed and the user should be logged out,
     *         false otherwise
     */
    private boolean changePassword() {
        System.out.print("Current password: ");
        String current = scanner.nextLine();
        if (!current.equals(applicant.getPassword())) {
            System.out.println("Incorrect current password.");
            return false;
        }

        System.out.print("New password: ");
        String newPassword = scanner.nextLine();
        if (newPassword.trim().isEmpty()) {
            System.out.println("Password cannot be empty.");
            return false;
        }

        if (!InputValidator.validatePassword(newPassword)) {
            System.out.println("Password must be at least 8 characters. Change password failed");
            return false;
        }

        applicant.setPassword(newPassword);
        boolean updated = FileManager.updatePasswordInCSV(Constants.APPLICANT_CSV, applicant.getNric(), newPassword);

        if (updated) {
            System.out.println("Password updated successfully.");
            System.out.println("For security reasons, you will be logged out and need to re-login with your new password.");
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            // Return to main menu by exiting this menu
            return true;
        } else {
            System.out.println("Error updating password.");
        }

        if ("password".equalsIgnoreCase(newPassword)) {
            System.out.println("Warning: 'password' is not a secure password.");
        }
        
        return false;
    }

    /**
     * Handles the process of withdrawing an application.
     * This method allows the applicant to request withdrawal of their application,
     * which will be pending approval from an HDB Manager.
     */
    private void withdrawApplication() {
        ApplicationStatus status = applicant.getApplicationStatus();

        if (status != ApplicationStatus.PENDING && 
            status != ApplicationStatus.SUCCESSFUL && 
            status != ApplicationStatus.BOOKED && 
            status != ApplicationStatus.PENDING_WITHDRAWAL) {
            System.out.println("You do not have any application to withdraw.");
            return;
        }
        
        if (status == ApplicationStatus.PENDING_WITHDRAWAL) {
            System.out.println("Your withdrawal request is pending approval from the HDB Manager.");
            return;
        }

        System.out.print("Are you sure you want to withdraw your application? (Y/N): ");
        String response = scanner.nextLine().trim();
        if (!"Y".equalsIgnoreCase(response)) {
            System.out.println("Withdrawal cancelled.");
            return;
        }

        boolean success = new ApplicationController().requestWithdrawal(applicant);
        if (success) {
            System.out.println("Withdrawal request submitted. Pending approval from HDB Manager.");
        } else {
            System.out.println("An error occurred. Withdrawal request failed.");
        }
    }

}
