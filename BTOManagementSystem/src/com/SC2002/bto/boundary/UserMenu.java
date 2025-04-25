package com.SC2002.bto.boundary;

import com.SC2002.bto.entities.BTOSystemUser;
import com.SC2002.bto.entities.Enquiry;
import com.SC2002.bto.entities.Project;
import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;

import java.util.List;
import java.util.Scanner;

/**
 * UserMenu is an abstract base class for all user menus.
 * It provides common functionality for menus that interact with BTOSystemUser instances.
 * 
 * This class is part of a refactoring to improve code reuse between ApplicantMenu and HDBOfficerMenu.
 * 
 * Following SOLID principles:
 * - Single Responsibility: Handles common menu functionality
 * - Open/Closed: Open for extension by specific menu classes
 */
public abstract class UserMenu<T extends BTOSystemUser> {
    
    protected Scanner scanner;
    protected T user;
    
    /**
     * Constructs a UserMenu with the specified scanner and user.
     * 
     * @param scanner the scanner to use for input
     * @param user the user associated with this menu
     */
    public UserMenu(Scanner scanner, T user) {
        this.scanner = scanner;
        this.user = user;
    }
    
    /**
     * Displays the menu and handles user input.
     * This method should be implemented by subclasses.
     */
    public abstract void displayMenu();
    
    /**
     * Displays the projects the user can view.
     */
    protected void viewProjects() {
        System.out.println("\n--- Available Projects ---");
        List<Project> viewableProjects = user.getViewableProjects();
        
        if (viewableProjects.isEmpty()) {
            System.out.println("No projects available.");
            return;
        }
        
        displayProjects(viewableProjects);
    }
    
    /**
     * Displays the specified projects.
     * 
     * @param projects the projects to display
     */
    protected void displayProjects(List<Project> projects) {
        for (Project p : projects) {
            System.out.println("Project ID: " + p.getProjectId());
            System.out.println("  Name          : " + p.getProjectName());
            System.out.println("  Neighbourhood : " + p.getNeighborhood());
            System.out.println("  Type1         : " + p.getType1Desc() + " | Units: " + p.getType1Units() + " | Price: " + p.getType1Price());
            System.out.println("  Type2         : " + p.getType2Desc() + " | Units: " + p.getType2Units() + " | Price: " + p.getType2Price());
            System.out.println("  Period        : " + p.getApplicationOpeningDate() + " to " + p.getApplicationClosingDate());
            System.out.println("  Manager       : " + p.getManager());
            System.out.println("  Officers      : " + String.join(", ", p.getOfficers()) + " (Slots: " + p.getOfficerSlots() + ")");
            System.out.println("-------------------------------------");
        }
    }
    
    /**
     * Displays the enquiries relevant to the user.
     */
    protected void viewEnquiries() {
        List<Enquiry> relevantEnquiries = user.getRelevantEnquiries();
        
        if (relevantEnquiries.isEmpty()) {
            System.out.println("No enquiries found.");
            return;
        }
        
        System.out.println("\n--- Enquiries ---");
        for (Enquiry e : relevantEnquiries) {
            System.out.println("Enquiry ID   : " + e.getEnquiryId());
            System.out.println("Project ID   : " + e.getProjectId());
            System.out.println("From         : " + e.getUserNric());
            System.out.println("Message      : " + e.getEnquiryText());
            System.out.println("Response     : " + (e.getResponse().isEmpty() ? "(Pending)" : e.getResponse()));
            System.out.println("-----------------------------------------");
        }
    }
    
    /**
     * Handles the password change process.
     * 
     * @return true if password was changed successfully and user should be logged out, false otherwise
     */
    protected boolean changePassword() {
        System.out.print("Current password: ");
        String currentPassword = scanner.nextLine();
        
        System.out.print("New password: ");
        String newPassword = scanner.nextLine();
        
        if (newPassword.trim().isEmpty()) {
            System.out.println("Password cannot be empty.");
            return false;
        }
        
        boolean success = user.changePassword(currentPassword, newPassword);
        
        if (success) {
            System.out.println("Password updated successfully.");
            System.out.println("For security reasons, you will be logged out and need to re-login with your new password.");
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            return true;
        } else {
            System.out.println("Password change failed. Please check your current password and ensure the new password meets requirements.");
        }
        
        if ("password".equalsIgnoreCase(newPassword)) {
            System.out.println("Warning: 'password' is not a secure password.");
        }
        
        return false;
    }
    
    /**
     * Prompts the user for a non-empty input.
     * 
     * @param prompt the prompt to display
     * @return the user's input
     */
    protected String promptNonEmpty(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("This field cannot be empty.");
            }
        } while (input.isEmpty());
        return input;
    }
    
    /**
     * Prompts the user for an optional input.
     * 
     * @param prompt the prompt to display
     * @return the user's input, which may be empty
     */
    protected String promptOptional(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    /**
     * Prompts the user for an integer input.
     * 
     * @param prompt the prompt to display
     * @return the user's input as an integer
     */
    protected int promptInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }
}
