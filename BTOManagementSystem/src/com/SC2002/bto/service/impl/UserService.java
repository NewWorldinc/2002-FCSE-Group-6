package com.SC2002.bto.service.impl;

import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.HDBManager;
import com.SC2002.bto.entities.HDBOfficer;
import com.SC2002.bto.entities.User;
import com.SC2002.bto.repository.IUserRepository;
import com.SC2002.bto.service.IUserService;
import com.SC2002.bto.utils.InputValidator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the user service.
 * Follows the Dependency Inversion Principle by depending on the repository interface.
 */
public class UserService implements IUserService {
    
    private final IUserRepository userRepository;
    
    /**
     * Constructs a UserService with the specified repository.
     * 
     * @param userRepository The user repository
     */
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public Optional<User> authenticate(String role, String nric, String password) {
        // Validate input
        if (!validateNric(nric) || !validatePassword(password)) {
            return Optional.empty();
        }
        
        // For officers, check specifically in the officer repository first
        if ("HDBOfficer".equalsIgnoreCase(role)) {
            // Check if the user exists in the officer list
            List<User> officers = userRepository.findByUserType("HDBOfficer");
            for (User officer : officers) {
                if (officer.getNric().equalsIgnoreCase(nric) && officer.getPassword().equals(password)) {
                    return Optional.of(officer);
                }
            }
            return Optional.empty();
        }
        
        // For other roles, use the standard authentication
        Optional<User> userOpt = userRepository.authenticate(nric, password);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Check if the user's role matches the requested role
            boolean roleMatches = false;
            if ("Applicant".equalsIgnoreCase(role) && user instanceof Applicant) {
                roleMatches = true;
            } else if ("HDBManager".equalsIgnoreCase(role) && user instanceof HDBManager) {
                roleMatches = true;
            }
            
            if (!roleMatches) {
                return Optional.empty();
            }
            
            return Optional.of(user);
        }
        
        return Optional.empty();
    }
    
    @Override
    public boolean changePassword(String nric, String currentPassword, String newPassword) {
        // Validate input
        if (!validateNric(nric) || !validatePassword(newPassword)) {
            return false;
        }
        
        // Authenticate with current password
        Optional<User> userOpt = userRepository.authenticate(nric, currentPassword);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        // Update password
        return userRepository.updatePassword(nric, newPassword);
    }
    
    @Override
    public List<Applicant> getAllApplicants() {
        return userRepository.findByUserType("Applicant").stream()
            .filter(user -> user instanceof Applicant)
            .map(user -> (Applicant) user)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<HDBOfficer> getAllOfficers() {
        return userRepository.findByUserType("HDBOfficer").stream()
            .filter(user -> user instanceof HDBOfficer)
            .map(user -> (HDBOfficer) user)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<HDBManager> getAllManagers() {
        return userRepository.findByUserType("HDBManager").stream()
            .filter(user -> user instanceof HDBManager)
            .map(user -> (HDBManager) user)
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<User> getUserByNric(String nric) {
        return userRepository.findByNric(nric);
    }
    
    @Override
    public Optional<Applicant> getApplicantByNric(String nric) {
        Optional<User> userOpt = userRepository.findByNric(nric);
        
        if (userOpt.isPresent() && userOpt.get() instanceof Applicant) {
            return Optional.of((Applicant) userOpt.get());
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<HDBOfficer> getOfficerByNric(String nric) {
        Optional<User> userOpt = userRepository.findByNric(nric);
        
        if (userOpt.isPresent() && userOpt.get() instanceof HDBOfficer) {
            return Optional.of((HDBOfficer) userOpt.get());
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<HDBManager> getManagerByNric(String nric) {
        Optional<User> userOpt = userRepository.findByNric(nric);
        
        if (userOpt.isPresent() && userOpt.get() instanceof HDBManager) {
            return Optional.of((HDBManager) userOpt.get());
        }
        
        return Optional.empty();
    }
    
    @Override
    public boolean validateNric(String nric) {
        return InputValidator.validateNric(nric);
    }
    
    @Override
    public boolean validatePassword(String password) {
        return InputValidator.validatePassword(password);
    }
}
