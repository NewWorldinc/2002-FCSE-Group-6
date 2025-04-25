package com.SC2002.bto.repository.csv;

import com.SC2002.bto.entities.Enquiry;
import com.SC2002.bto.repository.IEnquiryRepository;
import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CSV-based implementation of the enquiry repository.
 * Follows the Single Responsibility Principle by focusing only on enquiry data access.
 */
public class CSVEnquiryRepository implements IEnquiryRepository {
    
    private final String filePath;
    private List<Enquiry> enquiries;
    
    /**
     * Constructs a CSVEnquiryRepository with the default file path.
     */
    public CSVEnquiryRepository() {
        this(Constants.ENQUIRY_CSV);
    }
    
    /**
     * Constructs a CSVEnquiryRepository with the specified file path.
     * 
     * @param filePath The path of the CSV file
     */
    public CSVEnquiryRepository(String filePath) {
        this.filePath = filePath;
        this.enquiries = FileManager.loadAllEnquiries(filePath);
    }
    
    /**
     * Refreshes the enquiries from the CSV file.
     */
    public void refresh() {
        this.enquiries = FileManager.loadAllEnquiries(filePath);
    }
    
    @Override
    public List<Enquiry> findAll() {
        return new ArrayList<>(enquiries);
    }
    
    @Override
    public Optional<Enquiry> findById(Integer id) {
        return enquiries.stream()
            .filter(e -> e.getEnquiryId() == id)
            .findFirst();
    }
    
    @Override
    public Enquiry save(Enquiry enquiry) {
        // Check if enquiry already exists
        Optional<Enquiry> existingEnquiry = findById(enquiry.getEnquiryId());
        
        if (existingEnquiry.isPresent()) {
            // Update existing enquiry
            enquiries.remove(existingEnquiry.get());
        }
        
        // Add new enquiry
        enquiries.add(enquiry);
        
        // Save all enquiries
        FileManager.saveAllEnquiries(enquiries);
        
        return enquiry;
    }
    
    @Override
    public List<Enquiry> saveAll(List<Enquiry> entities) {
        // Replace all enquiries
        this.enquiries = new ArrayList<>(entities);
        
        // Save all enquiries
        FileManager.saveAllEnquiries(enquiries);
        
        return entities;
    }
    
    @Override
    public void delete(Enquiry enquiry) {
        enquiries.removeIf(e -> e.getEnquiryId() == enquiry.getEnquiryId());
        FileManager.saveAllEnquiries(enquiries);
    }
    
    @Override
    public boolean existsById(Integer id) {
        return enquiries.stream().anyMatch(e -> e.getEnquiryId() == id);
    }
    
    @Override
    public List<Enquiry> findByUser(String userNric) {
        return enquiries.stream()
            .filter(e -> e.getUserNric().equalsIgnoreCase(userNric))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Enquiry> findByProject(int projectId) {
        return enquiries.stream()
            .filter(e -> e.getProjectId() == projectId)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Enquiry> findWithResponses() {
        return enquiries.stream()
            .filter(Enquiry::hasResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Enquiry> findWithoutResponses() {
        return enquiries.stream()
            .filter(e -> !e.hasResponse())
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean updateResponse(int enquiryId, String response) {
        Optional<Enquiry> enquiryOpt = findById(enquiryId);
        if (enquiryOpt.isPresent()) {
            Enquiry enquiry = enquiryOpt.get();
            enquiry.setResponse(response);
            return FileManager.updateEnquiryResponse(enquiry);
        }
        return false;
    }
    
    @Override
    public boolean updateEnquiryText(int enquiryId, String enquiryText) {
        Optional<Enquiry> enquiryOpt = findById(enquiryId);
        if (enquiryOpt.isPresent()) {
            Enquiry enquiry = enquiryOpt.get();
            enquiry.setEnquiryText(enquiryText);
            return FileManager.updateEnquiryResponse(enquiry);
        }
        return false;
    }
}
