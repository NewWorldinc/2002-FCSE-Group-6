// File: src/com/SC2002/bto/boundary/CLI.java
package com.SC2002.bto.boundary;

import com.SC2002.bto.control.LoginController;
import com.SC2002.bto.entities.User;
import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.HDBOfficer;
import com.SC2002.bto.entities.HDBManager;

import java.util.Scanner;

/**
 * CLI is the main boundary class that handles user interaction.
 * Follows the Single Responsibility Principle by focusing only on user interaction.
 */
public class CLI {
    /** Scanner for reading user input */
    private Scanner scanner = new Scanner(System.in);

    /**
     * Starts the CLI application.
     * This method displays the main menu and handles user input until the user exits.
     */
    public void start() {
        // The initialization of repositories and services is now done in Main.java
        
        while (true) {
            System.out.println("====================================");
            System.out.println(" Welcome to the BTO Management System ");
            System.out.println("====================================");
            System.out.println("Main Menu:");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();
            switch(choice) {
                case "1":
                    login();
                    break;
                case "2":
                    System.out.println("Exiting system. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Handles the login process.
     * This method prompts the user to select a role, enter their NRIC and password,
     * and then authenticates the user. If authentication is successful, the appropriate
     * menu is displayed based on the user's role.
     */
    private void login() {
        System.out.println("\nSelect your role:");
        System.out.println("1. Applicant");
        System.out.println("2. HDB Officer");
        System.out.println("3. HDB Manager");
        System.out.print("Enter your role choice: ");
        String roleChoice = scanner.nextLine();

        String role;
        if ("1".equals(roleChoice)) {
            role = "Applicant";
        } else if ("2".equals(roleChoice)) {
            role = "HDBOfficer";
        } else if ("3".equals(roleChoice)) {
            role = "HDBManager";
        } else {
            System.out.println("Invalid role selected. Returning to main menu.");
            return;
        }

        System.out.print("Enter your NRIC: ");
        String nric = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        LoginController loginController = new LoginController();
        User user = loginController.authenticate(role, nric, password);
        if (user != null) {
            System.out.println("Login successful as " + role + " with NRIC: " + nric);
            switch (role) {
                case "Applicant":
                    new ApplicantMenu(scanner, (Applicant) user).displayMenu();
                    break;
                case "HDBOfficer":
                    new HDBOfficerMenu(scanner, (HDBOfficer) user).displayMenu();
                    break;
                case "HDBManager":
                    new HDBManagerMenu(scanner, (HDBManager) user).displayMenu();
                    break;
            }
        } else {
            System.out.println("Login failed. Please check your details and try again.");
        }
    }
}
