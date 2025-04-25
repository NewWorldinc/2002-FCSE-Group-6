package com.SC2002.bto.repository.csv;

import com.SC2002.bto.repository.IOfficerRegistrationRepository;
import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CSV-based implementation of the officer registration repository.
 * Follows the Single Responsibility Principle by focusing only on officer registration data access.
 */
public class CSVOfficerRegistrationRepository implements IOfficerRegistrationRepository {
    
    private final String filePath;
    
    /**
     * Constructs a CSVOfficerRegistrationRepository with the default file path.
     */
    public CSVOfficerRegistrationRepository() {
        this(Constants.REGISTRATION_CSV);
    }
    
    /**
     * Constructs a CSVOfficerRegistrationRepository with the specified file path.
     * 
     * @param filePath The path of the CSV file
     */
    public CSVOfficerRegistrationRepository(String filePath) {
        this.filePath = filePath;
    }
    
    @Override
    public boolean register(String officerNric, int projectId) {
        return FileManager.addOfficerRegistration(officerNric, projectId);
    }
    
    @Override
    public boolean approve(String officerNric, int projectId) {
        // This would require a more complex implementation that updates the project's officers list
        // For now, we'll just return false
        return false;
    }
    
    @Override
    public boolean reject(String officerNric, int projectId) {
        // This would require a more complex implementation that removes the registration
        // For now, we'll just return false
        return false;
    }
    
    @Override
    public List<Integer> findPendingByOfficer(String officerNric) {
        List<OfficerProjectPair> registrations = loadRegistrations();
        return registrations.stream()
            .filter(r -> r.getOfficerNric().equalsIgnoreCase(officerNric))
            .map(OfficerProjectPair::getProjectId)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<String> findPendingByProject(int projectId) {
        List<OfficerProjectPair> registrations = loadRegistrations();
        return registrations.stream()
            .filter(r -> r.getProjectId() == projectId)
            .map(OfficerProjectPair::getOfficerNric)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean hasPendingRegistration(String officerNric, int projectId) {
        List<OfficerProjectPair> registrations = loadRegistrations();
        return registrations.stream()
            .anyMatch(r -> r.getOfficerNric().equalsIgnoreCase(officerNric) && r.getProjectId() == projectId);
    }
    
    @Override
    public int getPendingRegistrationCount(int projectId) {
        List<OfficerProjectPair> registrations = loadRegistrations();
        return (int) registrations.stream()
            .filter(r -> r.getProjectId() == projectId)
            .count();
    }
    
    /**
     * Loads all officer registrations from the CSV file.
     * 
     * @return A list of officer-project registration pairs
     */
    private List<OfficerProjectPair> loadRegistrations() {
        List<OfficerProjectPair> registrations = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header
            reader.readLine();
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 2) continue;
                
                String officerNric = tokens[0].trim();
                int projectId = Integer.parseInt(tokens[1].trim());
                
                registrations.add(new OfficerProjectPair(officerNric, projectId));
            }
        } catch (IOException e) {
            // If file doesn't exist yet, that's fine — no registrations yet
        }
        
        return registrations;
    }
    
    /**
     * A simple class to represent an officer-project registration pair.
     */
    private static class OfficerProjectPair {
        private final String officerNric;
        private final int projectId;
        
        public OfficerProjectPair(String officerNric, int projectId) {
            this.officerNric = officerNric;
            this.projectId = projectId;
        }
        
        public String getOfficerNric() {
            return officerNric;
        }
        
        public int getProjectId() {
            return projectId;
        }
    }
}
