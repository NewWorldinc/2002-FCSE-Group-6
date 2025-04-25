package com.SC2002.bto.boundary;

import com.SC2002.bto.control.ApplicationController;
import com.SC2002.bto.control.ProjectController;
import com.SC2002.bto.entities.ApplicationStatus;
import com.SC2002.bto.entities.Enquiry;
import com.SC2002.bto.entities.HDBOfficer;
import com.SC2002.bto.entities.OfficerAsApplicant;
import com.SC2002.bto.entities.Project;
import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;
import com.SC2002.bto.utils.InputValidator;
import com.SC2002.bto.utils.ProjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * OfficerApplicantMenu provides a UI for officers to use applicant functionalities.
 * This class allows officers to apply for BTO projects, view their applications,
 * and manage their enquiries as if they were applicants.
 */
public class OfficerApplicantMenu {
    private Scanner scanner;
    private OfficerAsApplicant officerApplicant;
    private HDBOfficer officer;

    /**
     * Constructs an OfficerApplicantMenu with the specified scanner and officer.
     * 
     * @param scanner the scanner to use for input
     * @param officer the officer to act as an applicant
     */
    public OfficerApplicantMenu(Scanner scanner, HDBOfficer officer) {
        this.scanner = scanner;
        this.officer = officer;
        this.officerApplicant = new OfficerAsApplicant(officer);
    }

    /**
     * Displays the menu and handles user input.
     */
    public void displayMenu() {
        while (true) {
            System.out.println("\n--- Officer as Applicant Menu ---");
            System.out.println("1. View Available Projects");
            System.out.println("2. Apply for a Project");
            System.out.println("3. View Application Status");
            System.out.println("4. View Enquiries & Responses");
            System.out.println("5. Submit Enquiry");
            System.out.println("6. Edit/Delete Enquiry");
            System.out.println("7. Withdraw Application");
            System.out.println("8. Return to Officer Menu");
            
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1": viewAvailableProjects(); break;
                case "2": applyForProject(); break;
                case "3": viewApplicationStatus(); break;
                case "4": viewMyEnquiriesWithResponses(); break;
                case "5": submitEnquiry(); break;
                case "6": editOrDeleteEnquiry(); break;
                case "7": withdrawApplication(); break;
                case "8": return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private List<Enquiry> loadMyEnquiries() {
        return FileManager.loadAllEnquiries(Constants.ENQUIRY_CSV).stream()
            .filter(e -> e.getUserNric().equalsIgnoreCase(officerApplicant.getNric()))
            .collect(Collectors.toList());
    }

    private void viewAvailableProjects() {
        System.out.println("\n--- Available Projects ---");
        // Use ProjectController to get currently open projects
        ProjectController projectController = new ProjectController();
        // Refresh the project repository to ensure we have the latest data
        ProjectRepository.refresh();
        
        // Get projects that the officer can view as an applicant
        List<Project> allProjects = ProjectRepository.getAll();
        if (allProjects.isEmpty()) {
            System.out.println("No projects available.");
            return;
        }
        
        List<Project> viewableProjects = new ArrayList<>();
        String ms = officerApplicant.getMaritalStatus();
        int age = officerApplicant.getAge();
        int appliedId = officerApplicant.getAppliedProjectId();
        
        // Validate marital status
        if (!InputValidator.validateMaritalStatus(ms)) {
            System.out.println("Invalid marital status in your profile. Please contact support.");
            return;
        }
        
        // Include the project the officer has already applied for, if any
        if (appliedId != -1) {
            allProjects.stream()
                .filter(p -> p.getProjectId() == appliedId)
                .findFirst()
                .ifPresent(viewableProjects::add);
        }
        
        // Filter projects by visibility and application period
        List<Project> visibleProjects = allProjects.stream()
            .filter(p -> p.isVisible() && p.isCurrentlyOpen())
            .collect(Collectors.toList());
        
        // Apply eligibility criteria based on marital status and age
        for (Project p : visibleProjects) {
            // Skip projects the officer is assigned to
            if (p.isOfficerAssigned(officer.getNric()) || p.isOfficerAssigned(officer.getName())) {
                continue;
            }
            
            if ("Single".equalsIgnoreCase(ms)) {
                if (!InputValidator.validateSingleApplicantAge(age, ms)) {
                    System.out.println("As a single applicant you must be at least 35 to view any projects.");
                    return;
                }
                boolean has2Room = "2-Room".equalsIgnoreCase(p.getType1Desc()) || 
                                  "2-Room".equalsIgnoreCase(p.getType2Desc());
                if (has2Room) {
                    viewableProjects.add(p);
                }
            } else if ("Married".equalsIgnoreCase(ms)) {
                if (!InputValidator.validateMarriedApplicantAge(age, ms)) {
                    System.out.println("As a married applicant you must be at least 21 to view any projects.");
                    return;
                }
                viewableProjects.add(p);
            }
        }

        if (viewableProjects.isEmpty()) {
            System.out.println("No available projects match your eligibility criteria.");
            return;
        }

        for (Project p : viewableProjects) {
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

    private void applyForProject() {
        // First check if the officer already has an application
        ApplicationStatus status = officerApplicant.getApplicationStatus();
        
        if (status == ApplicationStatus.SUCCESSFUL || status == ApplicationStatus.BOOKED) {
            System.out.println("You already have an approved or booked application.");
            return;
        }
        
        if (officerApplicant.getAppliedProjectId() != -1 && 
            status != ApplicationStatus.NOT_APPLIED && 
            status != ApplicationStatus.UNSUCCESSFUL) {
            System.out.println("You have a pending application.");
            return;
        }
        
        // Get the list of projects the officer can view as an applicant
        List<Project> allProjects = ProjectRepository.getAll();
        if (allProjects.isEmpty()) {
            System.out.println("No projects available.");
            return;
        }
        
        List<Project> viewableProjects = new ArrayList<>();
        String ms = officerApplicant.getMaritalStatus();
        int age = officerApplicant.getAge();
        
        // Validate marital status
        if (!InputValidator.validateMaritalStatus(ms)) {
            System.out.println("Invalid marital status in your profile. Please contact support.");
            return;
        }
        
        // Filter projects by visibility and application period
        List<Project> visibleProjects = allProjects.stream()
            .filter(p -> p.isVisible() && p.isCurrentlyOpen())
            .collect(Collectors.toList());
        
        // Apply eligibility criteria based on marital status and age
        for (Project p : visibleProjects) {
            // Skip projects the officer is assigned to
            if (p.isOfficerAssigned(officer.getNric()) || p.isOfficerAssigned(officer.getName())) {
                continue;
            }
            
            if ("Single".equalsIgnoreCase(ms)) {
                if (!InputValidator.validateSingleApplicantAge(age, ms)) {
                    System.out.println("As a single applicant, you must be at least 35 years old to apply for any project.");
                    return;
                }
                boolean has2Room = "2-Room".equalsIgnoreCase(p.getType1Desc()) || 
                                  "2-Room".equalsIgnoreCase(p.getType2Desc());
                if (has2Room) {
                    viewableProjects.add(p);
                }
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
        
        // Check if the officer can apply for this project
        if (!officerApplicant.canApplyForProject(pid)) {
            System.out.println("You cannot apply for this project as you are assigned to it as an officer or have a pending registration.");
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

        boolean ok = new ApplicationController().applyForProject(officerApplicant, pid, flatType);
        System.out.println(ok ? "Applied → Pending." : "Application failed.");
    }

    private void viewApplicationStatus() {
        int appliedProjectId = officerApplicant.getAppliedProjectId();
        ApplicationStatus status = officerApplicant.getApplicationStatus();

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
                System.out.printf("Flat Type: %s\n", officerApplicant.getAppliedFlatType());
            }
        }
    }
    
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
    
    private void submitEnquiry() {
        // Get the list of projects the officer can view as an applicant
        List<Project> allProjects = ProjectRepository.getAll();
        if (allProjects.isEmpty()) {
            System.out.println("No projects available.");
            return;
        }
        
        List<Project> viewableProjects = new ArrayList<>();
        String ms = officerApplicant.getMaritalStatus();
        int age = officerApplicant.getAge();
        int appliedId = officerApplicant.getAppliedProjectId();
        
        // Validate marital status
        if (!InputValidator.validateMaritalStatus(ms)) {
            System.out.println("Invalid marital status in your profile. Please contact support.");
            return;
        }
        
        // Include the project the officer has already applied for, if any
        if (appliedId != -1) {
            allProjects.stream()
                .filter(p -> p.getProjectId() == appliedId)
                .findFirst()
                .ifPresent(viewableProjects::add);
        }
        
        // Filter projects by visibility and application period
        List<Project> visibleProjects = allProjects.stream()
            .filter(p -> p.isVisible() && p.isCurrentlyOpen())
            .collect(Collectors.toList());
        
        // Apply eligibility criteria based on marital status and age
        for (Project p : visibleProjects) {
            // Skip projects the officer is assigned to
            if (p.isOfficerAssigned(officer.getNric()) || p.isOfficerAssigned(officer.getName())) {
                continue;
            }
            
            if ("Single".equalsIgnoreCase(ms)) {
                if (!InputValidator.validateSingleApplicantAge(age, ms)) {
                    System.out.println("As a single applicant, you must be at least 35 years old to submit an enquiry for a project.");
                    return;
                }
                boolean has2Room = "2-Room".equalsIgnoreCase(p.getType1Desc()) || 
                                  "2-Room".equalsIgnoreCase(p.getType2Desc());
                if (has2Room) {
                    viewableProjects.add(p);
                }
            } else if ("Married".equalsIgnoreCase(ms)) {
                if (!InputValidator.validateMarriedApplicantAge(age, ms)) {
                    System.out.println("As a married applicant, you must be at least 21 years old to submit an enquiry for a project.");
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
        
        // Check if the officer is assigned to this project
        boolean isAssigned = officer.getViewableProjects().stream()
            .filter(p -> p.isOfficerAssigned(officer.getNric()) || p.isOfficerAssigned(officer.getName()))
            .anyMatch(p -> p.getProjectId() == projectId);
            
        if (isAssigned) {
            System.out.println("You cannot submit enquiries for a project you are assigned to as an officer.");
            return;
        }

        System.out.print("Enquiry text: ");
        String text = scanner.nextLine();

        List<Enquiry> allEnquiries = FileManager.loadAllEnquiries(Constants.ENQUIRY_CSV);
        int enquiryId = allEnquiries.size() + 1;

        Enquiry newEnquiry = new Enquiry(enquiryId, officerApplicant.getNric(), text, projectId);
        allEnquiries.add(newEnquiry);
        FileManager.saveAllEnquiries(allEnquiries);

        System.out.println("Enquiry #" + enquiryId + " submitted.");
    }

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

    private void withdrawApplication() {
        ApplicationStatus status = officerApplicant.getApplicationStatus();

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

        boolean success = new ApplicationController().requestWithdrawal(officerApplicant);
        if (success) {
            System.out.println("Withdrawal request submitted. Pending approval from HDB Manager.");
        } else {
            System.out.println("An error occurred. Withdrawal request failed.");
        }
    }
}
