// File: src/com/SC2002/bto/control/LoginController.java
package com.SC2002.bto.control;

import com.SC2002.bto.di.ServiceLocator;
import com.SC2002.bto.entities.User;
import com.SC2002.bto.service.IUserService;

import java.util.Optional;
import java.util.Scanner;

/**
 * LoginController handles user authentication and password management.
 * Follows the Dependency Inversion Principle by depending on service interfaces.
 */
public class LoginController {
    
    private final IUserService userService;
    
    /**
     * Constructs a LoginController with the default user service.
     */
    public LoginController() {
        this.userService = ServiceLocator.get(IUserService.class);
    }
    
    /**
     * Constructs a LoginController with the specified user service.
     * 
     * @param userService The user service
     */
    public LoginController(IUserService userService) {
        this.userService = userService;
    }

    /**
     * Authenticates a user using their role, NRIC, and password.
     *
     * @param role     the role of the user ("Applicant", "HDBOfficer", or "HDBManager").
     * @param nric     the user's NRIC.
     * @param password the user's password.
     * @return the authenticated User object if login is successful; otherwise, returns null.
     */
    public User authenticate(String role, String nric, String password) {
        if (!userService.validateNric(nric)) {
            System.out.println("Invalid NRIC format. It should start with S or T followed by 7 digits and a letter.");
            return null;
        }
        if (!userService.validatePassword(password)) {
            System.out.println("Password must be at least 8 characters.");
            return null;
        }

        Optional<User> userOpt = userService.authenticate(role, nric, password);
        
        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
            System.out.println("Incorrect credentials for NRIC: " + nric);
            return null;
        }
    }

    /**
     * Attempts to log in a user with retry functionality.
     * 
     * @param scanner The scanner to read user input
     * @param role The role of the user
     * @return The authenticated user if successful, null otherwise
     */
    public User loginWithRetry(Scanner scanner, String role) {
        final int MAX_ATTEMPTS = 3;
        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Enter NRIC: ");
            String nric = scanner.nextLine().trim();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            User user = authenticate(role, nric, password);
            if (user != null) {
                System.out.println("Login successful!");
                return user;
            }

            attempts++;
            if (attempts < MAX_ATTEMPTS) {
                System.out.printf("Login failed. Attempts left: %d\n", MAX_ATTEMPTS - attempts);
            }
        }

        System.out.println("Too many failed attempts. Returning to main menu.");
        return null;
    }
}
