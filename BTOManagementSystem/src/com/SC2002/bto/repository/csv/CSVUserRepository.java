package com.SC2002.bto.repository.csv;

import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.HDBManager;
import com.SC2002.bto.entities.HDBOfficer;
import com.SC2002.bto.entities.User;
import com.SC2002.bto.repository.IUserRepository;
import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CSV-based implementation of the user repository.
 * Follows the Single Responsibility Principle by focusing only on user data access.
 */
public class CSVUserRepository implements IUserRepository {
    
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        
        // Load applicants
        users.addAll(FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant"));
        
        // Load officers
        users.addAll(FileManager.loadUsersFromCSV(Constants.OFFICER_CSV, "HDBOfficer"));
        
        // Load managers
        users.addAll(FileManager.loadUsersFromCSV(Constants.MANAGER_CSV, "HDBManager"));
        
        return users;
    }
    
    @Override
    public Optional<User> findById(String nric) {
        return findByNric(nric);
    }
    
    @Override
    public Optional<User> findByNric(String nric) {
        // Try to find the user in each CSV file
        List<User> applicants = FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant");
        for (User user : applicants) {
            if (user.getNric().equalsIgnoreCase(nric)) {
                return Optional.of(user);
            }
        }
        
        List<User> officers = FileManager.loadUsersFromCSV(Constants.OFFICER_CSV, "HDBOfficer");
        for (User user : officers) {
            if (user.getNric().equalsIgnoreCase(nric)) {
                return Optional.of(user);
            }
        }
        
        List<User> managers = FileManager.loadUsersFromCSV(Constants.MANAGER_CSV, "HDBManager");
        for (User user : managers) {
            if (user.getNric().equalsIgnoreCase(nric)) {
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public User save(User user) {
        // Determine which CSV file to save to based on the user type
        String filePath;
        if (user instanceof Applicant) {
            filePath = Constants.APPLICANT_CSV;
            
            // For applicants, we need to update the application details
            if (user instanceof Applicant) {
                FileManager.updateApplicantApplication((Applicant) user);
            }
        } else if (user instanceof HDBOfficer) {
            filePath = Constants.OFFICER_CSV;
        } else if (user instanceof HDBManager) {
            filePath = Constants.MANAGER_CSV;
        } else {
            throw new IllegalArgumentException("Unknown user type: " + user.getClass().getName());
        }
        
        // Update the user's password
        FileManager.updatePasswordInCSV(filePath, user.getNric(), user.getPassword());
        
        return user;
    }
    
    @Override
    public List<User> saveAll(List<User> entities) {
        // Group users by type and save each group to the appropriate CSV file
        List<User> applicants = entities.stream()
            .filter(user -> user instanceof Applicant)
            .collect(Collectors.toList());
        
        List<User> officers = entities.stream()
            .filter(user -> user instanceof HDBOfficer)
            .collect(Collectors.toList());
        
        List<User> managers = entities.stream()
            .filter(user -> user instanceof HDBManager)
            .collect(Collectors.toList());
        
        // Save each group
        for (User applicant : applicants) {
            save(applicant);
        }
        
        for (User officer : officers) {
            save(officer);
        }
        
        for (User manager : managers) {
            save(manager);
        }
        
        return entities;
    }
    
    @Override
    public void delete(User user) {
        // Not implemented - deletion is not supported in the current system
    }
    
    @Override
    public boolean existsById(String nric) {
        return findByNric(nric).isPresent();
    }
    
    @Override
    public Optional<User> authenticate(String nric, String password) {
        // Try to find the user in each CSV file
        Optional<User> userOpt = findByNric(nric);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public boolean updatePassword(String nric, String newPassword) {
        // Try to update the password in each CSV file
        boolean updated = false;
        
        if (FileManager.applicantNricExists(nric)) {
            updated = FileManager.updatePasswordInCSV(Constants.APPLICANT_CSV, nric, newPassword);
        } else {
            // Try officer file
            updated = FileManager.updatePasswordInCSV(Constants.OFFICER_CSV, nric, newPassword);
            
            if (!updated) {
                // Try manager file
                updated = FileManager.updatePasswordInCSV(Constants.MANAGER_CSV, nric, newPassword);
            }
        }
        
        return updated;
    }
    
    @Override
    public List<User> findByUserType(String userType) {
        if ("Applicant".equalsIgnoreCase(userType)) {
            return FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant");
        } else if ("HDBOfficer".equalsIgnoreCase(userType)) {
            return FileManager.loadUsersFromCSV(Constants.OFFICER_CSV, "HDBOfficer");
        } else if ("HDBManager".equalsIgnoreCase(userType)) {
            return FileManager.loadUsersFromCSV(Constants.MANAGER_CSV, "HDBManager");
        } else {
            return new ArrayList<>();
        }
    }
}
