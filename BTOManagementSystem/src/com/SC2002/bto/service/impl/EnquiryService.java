package com.SC2002.bto.service.impl;

import com.SC2002.bto.entities.Enquiry;
import com.SC2002.bto.repository.IEnquiryRepository;
import com.SC2002.bto.service.IEnquiryService;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the enquiry service.
 * Follows the Dependency Inversion Principle by depending on the repository interface.
 */
public class EnquiryService implements IEnquiryService {
    
    private final IEnquiryRepository enquiryRepository;
    
    /**
     * Constructs an EnquiryService with the specified repository.
     * 
     * @param enquiryRepository The enquiry repository
     */
    public EnquiryService(IEnquiryRepository enquiryRepository) {
        this.enquiryRepository = enquiryRepository;
    }
    
    @Override
    public List<Enquiry> getAllEnquiries() {
        return enquiryRepository.findAll();
    }
    
    @Override
    public Optional<Enquiry> getEnquiryById(int enquiryId) {
        return enquiryRepository.findById(enquiryId);
    }
    
    @Override
    public List<Enquiry> getEnquiriesByUser(String userNric) {
        return enquiryRepository.findByUser(userNric);
    }
    
    @Override
    public List<Enquiry> getEnquiriesByProject(int projectId) {
        return enquiryRepository.findByProject(projectId);
    }
    
    @Override
    public List<Enquiry> getEnquiriesByOfficer(String officerNric) {
        // This would require a more complex implementation that checks which projects
        // the officer is assigned to and then finds enquiries for those projects
        // For now, we'll return an empty list
        return List.of();
    }
    
    @Override
    public List<Enquiry> getEnquiriesByManager(String managerNric) {
        // This would require a more complex implementation that checks which projects
        // the manager is managing and then finds enquiries for those projects
        // For now, we'll return an empty list
        return List.of();
    }
    
    @Override
    public List<Enquiry> getEnquiriesWithResponses() {
        return enquiryRepository.findWithResponses();
    }
    
    @Override
    public List<Enquiry> getEnquiriesWithoutResponses() {
        return enquiryRepository.findWithoutResponses();
    }
    
    @Override
    public int submitEnquiry(String userNric, int projectId, String enquiryText) {
        // Generate a new enquiry ID
        int newId = generateNewEnquiryId();
        
        // Create a new enquiry
        Enquiry enquiry = new Enquiry(newId, userNric, enquiryText, projectId);
        
        // Save the enquiry
        enquiryRepository.save(enquiry);
        
        return newId;
    }
    
    @Override
    public boolean updateEnquiryText(int enquiryId, String userNric, String newText) {
        Optional<Enquiry> enquiryOpt = enquiryRepository.findById(enquiryId);
        if (enquiryOpt.isPresent()) {
            Enquiry enquiry = enquiryOpt.get();
            
            // Check if the user is authorized to update the enquiry
            if (!enquiry.getUserNric().equalsIgnoreCase(userNric)) {
                return false;
            }
            
            return enquiryRepository.updateEnquiryText(enquiryId, newText);
        }
        return false;
    }
    
    @Override
    public boolean respondToEnquiry(int enquiryId, String officerNric, String response) {
        // In a more complex implementation, we would check if the officer is assigned to the project
        // For now, we'll just update the response
        return enquiryRepository.updateResponse(enquiryId, response);
    }
    
    @Override
    public boolean deleteEnquiry(int enquiryId, String userNric) {
        Optional<Enquiry> enquiryOpt = enquiryRepository.findById(enquiryId);
        if (enquiryOpt.isPresent()) {
            Enquiry enquiry = enquiryOpt.get();
            
            // Check if the user is authorized to delete the enquiry
            if (!enquiry.getUserNric().equalsIgnoreCase(userNric)) {
                return false;
            }
            
            enquiryRepository.delete(enquiry);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean hasResponse(int enquiryId) {
        Optional<Enquiry> enquiryOpt = enquiryRepository.findById(enquiryId);
        return enquiryOpt.map(Enquiry::hasResponse).orElse(false);
    }
    
    @Override
    public boolean isAuthorized(int enquiryId, String userNric) {
        Optional<Enquiry> enquiryOpt = enquiryRepository.findById(enquiryId);
        return enquiryOpt.map(e -> e.getUserNric().equalsIgnoreCase(userNric)).orElse(false);
    }
    
    /**
     * Generates a new enquiry ID.
     * 
     * @return A new enquiry ID
     */
    private int generateNewEnquiryId() {
        // Find the maximum enquiry ID and add 1
        return enquiryRepository.findAll().stream()
            .mapToInt(Enquiry::getEnquiryId)
            .max()
            .orElse(0) + 1;
    }
}
