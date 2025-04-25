// File: src/com/SC2002/bto/boundary/HDBOfficerMenu.java
package com.SC2002.bto.boundary;

import com.SC2002.bto.control.ProjectController;
import com.SC2002.bto.entities.*;
import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;
import com.SC2002.bto.utils.InputValidator;
import com.SC2002.bto.utils.ProjectRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * HDBOfficerMenu is the boundary class for HDB officers.
 * It has been updated to provide both officer and applicant functionalities.
 * 
 * Following SOLID principles:
 * - Single Responsibility: Handles the UI for HDB officers
 * - Open/Closed: Extended to support dual functionality without modifying existing code
 */
public class HDBOfficerMenu {
    private Scanner scanner;
    private HDBOfficer officer;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d/M/yy");

    /**
     * Constructs an HDBOfficerMenu with the specified scanner and officer.
     * 
     * @param scanner the scanner to use for input
     * @param officer the officer to display the menu for
     */
    public HDBOfficerMenu(Scanner scanner, HDBOfficer officer) {
        this.scanner = scanner;
        this.officer = officer;
        ProjectRepository.init(Constants.PROJECT_CSV);
    }

    /**
     * Displays the main menu and handles user input.
     * Provides a choice between officer and applicant functionalities.
     */
    public void displayMenu() {
        while (true) {
            System.out.println("\n--- HDB Officer Main Menu ---");
            System.out.println("1. Use Officer Functionalities");
            System.out.println("2. Use Applicant Functionalities");
            System.out.println("3. Logout");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": displayOfficerMenu(); break;
                case "2": displayApplicantMenu(); break;
                case "3": return;
                default: System.out.println("Invalid option. Try again.");
            }
        }
    }
    
    /**
     * Displays the officer menu and handles user input.
     */
    private void displayOfficerMenu() {
        while (true) {
            System.out.println("\n--- Officer Functionalities ---");
            System.out.println("1. View Assigned Projects");
            System.out.println("2. Update Flat Availability");
            System.out.println("3. Register for a Project");
            System.out.println("4. View Registration Status");
            System.out.println("5. Process Applicant Applications");
            System.out.println("6. Respond to Enquiries");
            System.out.println("7. Generate Receipt");
            System.out.println("8. Change Password");
            System.out.println("9. Return to Main Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": viewAssignedProjects(); break;
                case "2": updateFlatAvailability(); break;
                case "3": registerForProject(); break;
                case "4": viewRegistrationStatus(); break;
                case "5": processApplicantApplications(); break;
                case "6": respondToEnquiries(); break;
                case "7": generateReceipt(); break;
                case "8": if (changePassword()) return; break;
                case "9": return;
                default: System.out.println("Invalid option. Try again.");
            }
        }
    }
    
    /**
     * Displays the applicant menu for the officer.
     * Checks for potential conflicts between officer assignments and applicant applications.
     */
    private void displayApplicantMenu() {
        // Check if the officer has any assigned projects
        List<Project> assignedProjects = ProjectRepository.getAll().stream()
            .filter(p -> p.isOfficerAssigned(officer.getNric()) || p.isOfficerAssigned(officer.getName()))
            .collect(Collectors.toList());
            
        // Check if the officer has any pending registrations
        List<Integer> pendingRegistrations = officer.getPendingRegistrations();
        
        // Create an OfficerAsApplicant instance to check for existing applications
        OfficerAsApplicant officerAsApplicant = new OfficerAsApplicant(officer);
        int appliedProjectId = officerAsApplicant.getAppliedProjectId();
        
        // Check if the officer has an existing application
        if (appliedProjectId != -1) {
            // Get the project the officer has applied for
            Project appliedProject = ProjectRepository.getAll().stream()
                .filter(p -> p.getProjectId() == appliedProjectId)
                .findFirst()
                .orElse(null);
                
            if (appliedProject != null) {
                // Check for time period conflicts with assigned projects
                boolean hasConflict = false;
                
                for (Project assignedProject : assignedProjects) {
                    // Check if application periods overlap
                    if (!(appliedProject.getApplicationClosingDate().isBefore(assignedProject.getApplicationOpeningDate()) || 
                          appliedProject.getApplicationOpeningDate().isAfter(assignedProject.getApplicationClosingDate()))) {
                        hasConflict = true;
                        System.out.println("\nERROR: Your house application for project '" + appliedProject.getProjectName() + 
                                          "' has a time period conflict with your officer assignment to project '" + 
                                          assignedProject.getProjectName() + "'.");
                        System.out.println("You cannot access the applicant menu until this conflict is resolved.");
                        return;
                    }
                }
            }
        }
        
        // If the officer has assigned projects or pending registrations, show a warning
        if (!assignedProjects.isEmpty() || !pendingRegistrations.isEmpty()) {
            System.out.println("\nWARNING: You are currently assigned to projects or have pending registrations.");
            System.out.println("Using applicant functionalities may create conflicts with your officer role.");
            System.out.println("Are you sure you want to continue? (Y/N)");
            
            String response = scanner.nextLine().trim();
            if (!"Y".equalsIgnoreCase(response)) {
                System.out.println("Returning to main menu.");
                return;
            }
        }
        
        // Create and display the officer applicant menu
        new OfficerApplicantMenu(scanner, officer).displayMenu();
    }
    
    private void viewAssignedProjects() {
        System.out.println("\n--- Your Assigned Projects ---");
        List<Project> assigned = ProjectRepository.getAll().stream().filter(p -> p.isOfficerAssigned(officer.getNric()) || p.isOfficerAssigned(officer.getName())).collect(Collectors.toList());
        
        if (assigned.isEmpty()) {
            System.out.println("You are not assigned to any projects.");
            return;
        }

        for (Project p : assigned) {
            System.out.println("Project ID: " + p.getProjectId());
            System.out.println("  Name          : " + p.getProjectName());
            System.out.println("  Neighbourhood : " + p.getNeighborhood());
            System.out.println("  Type1         : " + p.getType1Desc() + " | Units: " + p.getType1Units() + " | Price: " + p.getType1Price());
            System.out.println("  Type2         : " + p.getType2Desc() + " | Units: " + p.getType2Units() + " | Price: " + p.getType2Price());
            System.out.println("  Period        : " + p.getApplicationOpeningDate() + " to " + p.getApplicationClosingDate());
            System.out.println("  Manager       : " + p.getManager());
            System.out.println("  Officer Slots : " + p.getOfficerSlots());
            System.out.println("-------------------------------------");
        }
    }

    private void updateFlatAvailability() {
        System.out.println("\n--- Update Flat Availability ---");
        List<Project> projects = ProjectRepository.getAll().stream()
        	.filter(p -> p.isOfficerAssigned(officer.getNric()) || p.isOfficerAssigned(officer.getName()))
            .collect(Collectors.toList());

        if (projects.isEmpty()) {
            System.out.println("You are not assigned to any projects.");
            return;
        }

        for (Project p : projects) {
            System.out.printf("ID: %d | %s: %d units | %s: %d units\n",
                p.getProjectId(), p.getType1Desc(), p.getType1Units(),
                p.getType2Desc(), p.getType2Units());
        }

        System.out.print("Enter Project ID to update: ");
        int pid = Integer.parseInt(scanner.nextLine());
        Project selected = projects.stream()
            .filter(p -> p.getProjectId() == pid)
            .findFirst().orElse(null);

        if (selected == null) {
            System.out.println("You are not assigned to this project.");
            return;
        }

        System.out.print("Flat type (1 for " + selected.getType1Desc() + ", 2 for " + selected.getType2Desc() + "): ");
        String flatType = scanner.nextLine().trim();
        System.out.print("Enter new unit count: ");
        int units = Integer.parseInt(scanner.nextLine());

        if ("1".equals(flatType)) selected.setType1Units(units);
        else if ("2".equals(flatType)) selected.setType2Units(units);
        else {
            System.out.println("Invalid flat type.");
            return;
        }

        FileManager.saveProjects(ProjectRepository.getAll());
        System.out.println("Availability updated.");
    }

    /**
     * Helper method to check if two projects have overlapping application periods.
     * 
     * @param project1 The first project
     * @param project2 The second project
     * @return true if the projects have overlapping application periods, false otherwise
     */
    private boolean hasDateConflict(Project project1, Project project2) {
        // Projects have overlapping periods if neither project's end is before the other's start
        return !(project1.getApplicationClosingDate().isBefore(project2.getApplicationOpeningDate()) || 
                project1.getApplicationOpeningDate().isAfter(project2.getApplicationClosingDate()));
    }
    
    /**
     * Registers an officer for a project.
     * Ensures that officers can only register for projects that don't conflict with their
     * existing assignments or pending registrations.
     */
    private void registerForProject() {
        System.out.println("\n--- Register for a Project ---");
        List<Project> allProjects = ProjectRepository.getAll();
        
        // Check if officer has applied for any project as an applicant
        List<Applicant> applicants = FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant").stream()
            .filter(u -> u instanceof Applicant)
            .map(u -> (Applicant) u)
            .collect(Collectors.toList());
            
        Applicant officerAsApplicant = applicants.stream()
            .filter(a -> a.getNric().equalsIgnoreCase(officer.getNric()))
            .findFirst().orElse(null);
        
        // Get projects the officer is already assigned to
        List<Project> assignedProjects = allProjects.stream()
            .filter(p -> p.isOfficerAssigned(officer.getNric()) || p.isOfficerAssigned(officer.getName()))
            .collect(Collectors.toList());
            
        // Check if officer has applied for any project as an applicant and has more than 1 assigned project
        Project appliedProject = null;
        if (officerAsApplicant != null && officerAsApplicant.getAppliedProjectId() != -1) {
            // If officer is handling more than 1 project, don't allow registration
            if (assignedProjects.size() > 1) {
                System.out.println("You cannot register for more projects as you are already handling multiple projects and have applied for a project as an applicant.");
                return;
            }
            
            // Get the project the officer has applied for as an applicant
            int appliedProjectId = officerAsApplicant.getAppliedProjectId();
            appliedProject = allProjects.stream()
                .filter(p -> p.getProjectId() == appliedProjectId)
                .findFirst()
                .orElse(null);
                
            if (appliedProject != null) {
                System.out.println("Note: You have applied for project '" + appliedProject.getProjectName() + 
                                  "' as an applicant. You can only register for projects that don't clash with this project's time period.");
            }
        }
            
        // Get pending registrations
        List<Integer> pendingProjectIds = new ArrayList<>();
        List<Project> pendingProjects = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(Constants.REGISTRATION_CSV))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 2) continue;
                
                String nric = tokens[0].trim();
                if (nric.equalsIgnoreCase(officer.getNric())) {
                    int projectId = Integer.parseInt(tokens[1].trim());
                    pendingProjectIds.add(projectId);
                    
                    // Find the project object for this pending registration
                    allProjects.stream()
                        .filter(p -> p.getProjectId() == projectId)
                        .findFirst()
                        .ifPresent(pendingProjects::add);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading registration file: " + e.getMessage());
        }
        
        // For registration, show all visible projects with available slots
        List<Project> availableProjects = allProjects.stream()
            .filter(p -> p.isVisible() && p.getOfficerSlots() > 0)
            .collect(Collectors.toList());

        if (availableProjects.isEmpty()) {
            System.out.println("No projects available for registration.");
            return;
        }
        
        // Filter out projects that have date conflicts with assigned or pending projects
        List<Project> projectsWithoutConflicts = new ArrayList<>();
        List<Project> projectsWithConflicts = new ArrayList<>();
        
        for (Project availableProject : availableProjects) {
            boolean hasConflict = false;
            
            // Check for conflicts with assigned projects
            for (Project assignedProject : assignedProjects) {
                if (hasDateConflict(availableProject, assignedProject)) {
                    projectsWithConflicts.add(availableProject);
                    hasConflict = true;
                    break;
                }
            }
            
            // If no conflicts with assigned projects, check for conflicts with pending projects
            if (!hasConflict) {
                for (Project pendingProject : pendingProjects) {
                    if (hasDateConflict(availableProject, pendingProject)) {
                        projectsWithConflicts.add(availableProject);
                        hasConflict = true;
                        break;
                    }
                }
            }
            
            // If no conflicts with assigned or pending projects, check for conflicts with applied project
            if (!hasConflict && appliedProject != null) {
                if (hasDateConflict(availableProject, appliedProject)) {
                    projectsWithConflicts.add(availableProject);
                    hasConflict = true;
                }
            }
            
            // If no conflicts at all, add to the list of projects without conflicts
            if (!hasConflict && !pendingProjectIds.contains(availableProject.getProjectId())) {
                projectsWithoutConflicts.add(availableProject);
            }
        }
        
        // Display projects without conflicts
        if (projectsWithoutConflicts.isEmpty()) {
            System.out.println("No projects available for registration without date conflicts.");
            
            // Show projects with conflicts for information
            if (!projectsWithConflicts.isEmpty()) {
                System.out.println("\nThe following projects have date conflicts with your current assignments or pending registrations:");
                for (Project p : projectsWithConflicts) {
                    System.out.printf("  ID: %d | %s @ %s | Period: %s to %s\n",
                        p.getProjectId(), p.getProjectName(), p.getNeighborhood(),
                        p.getApplicationOpeningDate(), p.getApplicationClosingDate());
                }
            }
            return;
        }
        
        System.out.println("Available Projects for Registration:");
        for (Project p : projectsWithoutConflicts) {
            System.out.printf("  ID: %d | %s @ %s | Slots: %d | Period: %s to %s\n",
                p.getProjectId(), p.getProjectName(), p.getNeighborhood(), p.getOfficerSlots(),
                p.getApplicationOpeningDate(), p.getApplicationClosingDate());
        }

        System.out.print("Enter Project ID to register for: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }
        
        // Check if officer has already applied for this project
        if (pendingProjectIds.contains(id)) {
            System.out.println("You have already submitted a registration request for this project.");
            return;
        }
        
        // Find the selected project from the list of projects without conflicts
        Project selected = projectsWithoutConflicts.stream()
            .filter(p -> p.getProjectId() == id)
            .findFirst().orElse(null);

        if (selected == null) {
            System.out.println("Invalid project ID or the project has date conflicts with your current assignments.");
            return;
        }

        boolean success = FileManager.addOfficerRegistration(officer.getNric(), selected.getProjectId());
        if (success) {
            System.out.println("Registration request submitted for approval.");
        } else {
            System.out.println("Registration failed. Please try again later.");
        }
    }

    private void processApplicantApplications() {
        System.out.println("\n--- Process Applicant Applications ---");
        List<Project> assignedProjects = ProjectRepository.getAll().stream()
        		.filter(p -> p.isOfficerAssigned(officer.getNric()) || p.isOfficerAssigned(officer.getName()))
                .collect(Collectors.toList());

        if (assignedProjects.isEmpty()) {
            System.out.println("No assigned projects."); return;
        }

        List<Integer> assignedIds = assignedProjects.stream().map(Project::getProjectId).toList();

        List<Applicant> applicants = FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant").stream()
            .filter(u -> u instanceof Applicant)
            .map(u -> (Applicant) u)
            .filter(a -> assignedIds.contains(a.getAppliedProjectId()) &&
                         a.getApplicationStatus() == ApplicationStatus.PENDING)
            .collect(Collectors.toList());

        if (applicants.isEmpty()) {
            System.out.println("No applicants to process."); return;
        }

        for (Applicant a : applicants) {
            // Find the project name for this application
            Project applicantProject = assignedProjects.stream()
                .filter(p -> p.getProjectId() == a.getAppliedProjectId())
                .findFirst()
                .orElse(null);
            
            String projectName = applicantProject != null ? applicantProject.getProjectName() : "Unknown";
            
            System.out.printf("Applicant: %s (%s)\n", a.getName(), a.getNric());
            System.out.printf("Project ID: %d | Project Name: %s | Flat Type: %s | Status: %s\n",
                a.getAppliedProjectId(), projectName, a.getAppliedFlatType(), a.getApplicationStatus());
            System.out.print("Decision (A=Book, R=Reject, S=Skip): ");
            String decision = scanner.nextLine().trim();

            if ("A".equalsIgnoreCase(decision)) {
                ProjectController projectController = new ProjectController();
                boolean booked = projectController.bookFlatUnit(a.getAppliedProjectId(), a.getAppliedFlatType());
                if (!booked) {
                    System.out.println("Booking failed. No units left.");
                    continue;
                }
                a.setApplicationStatus(ApplicationStatus.BOOKED);
            } else if ("R".equalsIgnoreCase(decision)) {
                a.setApplicationStatus(ApplicationStatus.UNSUCCESSFUL);
                a.setAppliedProjectId(-1);
                a.setAppliedFlatType("");
            } else {
                System.out.println("Skipped."); continue;
            }

            boolean success = FileManager.updateApplicantApplication(a);
            System.out.println(success ? "Status updated." : "Failed to update.");
        }
    }

    private void respondToEnquiries() {
        System.out.println("\n--- Respond to Enquiries ---");
        
        // Get the list of projects the officer is assigned to
        List<Project> assignedProjects = ProjectRepository.getAll().stream()
            .filter(p -> p.isOfficerAssigned(officer.getNric()) || p.isOfficerAssigned(officer.getName()))
            .collect(Collectors.toList());
            
        if (assignedProjects.isEmpty()) {
            System.out.println("You are not assigned to any projects."); 
            return;
        }
        
        // Get the project IDs the officer is assigned to
        List<Integer> assignedProjectIds = assignedProjects.stream()
            .map(Project::getProjectId)
            .collect(Collectors.toList());
            
        // Load all enquiries and filter to only those for the officer's assigned projects
        List<Enquiry> allEnquiries = FileManager.loadAllEnquiries(Constants.ENQUIRY_CSV);
        List<Enquiry> relevantEnquiries = allEnquiries.stream()
            .filter(e -> assignedProjectIds.contains(e.getProjectId()))
            .collect(Collectors.toList());
            
        if (relevantEnquiries.isEmpty()) {
            System.out.println("No enquiries available for your assigned projects."); 
            return;
        }
        
        System.out.println("Enquiries for your assigned projects:");
        relevantEnquiries.forEach(e -> {
            // Find the project name for better context
            String projectName = assignedProjects.stream()
                .filter(p -> p.getProjectId() == e.getProjectId())
                .map(Project::getProjectName)
                .findFirst()
                .orElse("Unknown Project");
                
            System.out.printf("ID:%d | Project:%d (%s) | From:%s | %s | Response:%s\n",
                e.getEnquiryId(), e.getProjectId(), projectName, e.getUserNric(), 
                e.getEnquiryText(), e.hasResponse() ? e.getResponse() : "(No response yet)");
        });

        System.out.print("\nEnter Enquiry ID to respond: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }
        
        // Find the selected enquiry, but only from the relevant ones
        Enquiry selected = relevantEnquiries.stream()
            .filter(e -> e.getEnquiryId() == id)
            .findFirst()
            .orElse(null);
            
        if (selected == null) {
            System.out.println("Invalid ID or you are not assigned to the project this enquiry is about."); 
            return;
        }
        
        System.out.print("Enter response: ");
        selected.setResponse(scanner.nextLine());
        boolean ok = FileManager.updateEnquiryResponse(selected);
        System.out.println(ok ? "Response saved." : "Failed to save response.");
    }

    private void viewRegistrationStatus() {
        System.out.println("\n--- Your Registration Status ---");
        
        // Get all projects
        List<Project> allProjects = ProjectRepository.getAll();
        
        // Check for pending registrations in OfficerRegistration.csv
        List<Integer> pendingProjectIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(Constants.REGISTRATION_CSV))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 2) continue;
                
                String nric = tokens[0].trim();
                if (nric.equalsIgnoreCase(officer.getNric())) {
                    int projectId = Integer.parseInt(tokens[1].trim());
                    pendingProjectIds.add(projectId);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading registration file: " + e.getMessage());
        }
        
        // Check for approved registrations (officer is in project's officers list)
        List<Project> approvedProjects = allProjects.stream()
            .filter(p -> p.isOfficerAssigned(officer.getNric()) || p.isOfficerAssigned(officer.getName()))
            .collect(Collectors.toList());
        
        // Display pending registrations
        if (!pendingProjectIds.isEmpty()) {
            System.out.println("\nPending Registrations:");
            for (Integer projectId : pendingProjectIds) {
                Project p = allProjects.stream()
                    .filter(proj -> proj.getProjectId() == projectId)
                    .findFirst().orElse(null);
                
                if (p != null) {
                    System.out.printf("  Project ID: %d | %s @ %s | Status: PENDING APPROVAL\n",
                        p.getProjectId(), p.getProjectName(), p.getNeighborhood());
                }
            }
        }
        
        // Display approved registrations
        if (!approvedProjects.isEmpty()) {
            System.out.println("\nApproved Registrations:");
            for (Project p : approvedProjects) {
                System.out.printf("  Project ID: %d | %s @ %s | Status: APPROVED\n",
                    p.getProjectId(), p.getProjectName(), p.getNeighborhood());
                System.out.printf("  Period: %s to %s\n", 
                    p.getApplicationOpeningDate(), p.getApplicationClosingDate());
                System.out.println("  --------------------------------------");
            }
        }
        
        // If no registrations found
        if (pendingProjectIds.isEmpty() && approvedProjects.isEmpty()) {
            System.out.println("You have not registered for any projects.");
        }
    }
    
    private void generateReceipt() {
        System.out.println("\n--- Generate Receipt ---");
        System.out.print("Enter Applicant NRIC: ");
        String nric = scanner.nextLine().trim();

        List<Applicant> applicants = FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant").stream()
            .filter(u -> u instanceof Applicant)
            .map(u -> (Applicant) u)
            .collect(Collectors.toList());

        Applicant a = applicants.stream()
            .filter(app -> app.getNric().equalsIgnoreCase(nric) && app.getApplicationStatus() == ApplicationStatus.BOOKED)
            .findFirst().orElse(null);

        if (a == null) {
            System.out.println("No booking found for applicant.");
            return;
        }

        Project p = ProjectRepository.getAll().stream()
            .filter(proj -> proj.getProjectId() == a.getAppliedProjectId())
            .findFirst().orElse(null);

        if (p == null || !(p.isOfficerAssigned(officer.getNric()) || p.isOfficerAssigned(officer.getName()))) {
            System.out.println("You are not assigned to this project.");
            return;
        }

        System.out.println("\n--- Receipt ---");
        System.out.printf("Applicant: %s (%s)\n", a.getName(), a.getNric());
        System.out.printf("Age: %d | Marital: %s\n", a.getAge(), a.getMaritalStatus());
        System.out.printf("Project: %s @ %s\n", p.getProjectName(), p.getNeighborhood());
        String type = a.getAppliedFlatType();
        int price = "2-Room".equalsIgnoreCase(type) ? p.getType1Price() : p.getType2Price();
        System.out.printf("Flat Type: %s | Price: $%d\n", type, price);
    }
    
    /**
     * Handles the password change process for officers.
     * 
     * @return true if password was changed successfully and user should be logged out, false otherwise
     */
    private boolean changePassword() {
        System.out.print("Current password: ");
        String currentPassword = scanner.nextLine();
        
        System.out.print("New password: ");
        String newPassword = scanner.nextLine();
        
        if (newPassword.trim().isEmpty()) {
            System.out.println("Password cannot be empty.");
            return false;
        }
        
        if (!officer.getPassword().equals(currentPassword)) {
            System.out.println("Incorrect current password.");
            return false;
        }
        
        if (!InputValidator.validatePassword(newPassword)) {
            System.out.println("Password must be at least 8 characters. Change password failed");
            return false;
        }
        
        officer.setPassword(newPassword);
        boolean updated = FileManager.updatePasswordInCSV(Constants.OFFICER_CSV, officer.getNric(), newPassword);
        
        if (updated) {
            System.out.println("Password updated successfully.");
            System.out.println("For security reasons, you will be logged out and need to re-login with your new password.");
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            return true;
        } else {
            System.out.println("Error updating password.");
        }
        
        if ("password".equalsIgnoreCase(newPassword)) {
            System.out.println("Warning: 'password' is not a secure password.");
        }
        
        return false;
    }

}
