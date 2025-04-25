package com.SC2002.bto.service;

import com.SC2002.bto.entities.Enquiry;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for enquiry-related operations.
 * Follows Interface Segregation Principle by providing a focused contract.
 */
public interface IEnquiryService {
    
    /**
     * Gets all enquiries.
     * 
     * @return A list of all enquiries
     */
    List<Enquiry> getAllEnquiries();
    
    /**
     * Gets an enquiry by its ID.
     * 
     * @param enquiryId The ID of the enquiry
     * @return An Optional containing the enquiry if found, empty otherwise
     */
    Optional<Enquiry> getEnquiryById(int enquiryId);
    
    /**
     * Gets enquiries submitted by a specific user.
     * 
     * @param userNric The NRIC of the user
     * @return A list of enquiries submitted by the specified user
     */
    List<Enquiry> getEnquiriesByUser(String userNric);
    
    /**
     * Gets enquiries related to a specific project.
     * 
     * @param projectId The ID of the project
     * @return A list of enquiries related to the specified project
     */
    List<Enquiry> getEnquiriesByProject(int projectId);
    
    /**
     * Gets enquiries for projects that a specific HDB officer is assigned to.
     * 
     * @param officerNric The NRIC of the officer
     * @return A list of enquiries for projects the officer is assigned to
     */
    List<Enquiry> getEnquiriesByOfficer(String officerNric);
    
    /**
     * Gets enquiries for projects managed by a specific HDB manager.
     * 
     * @param managerNric The NRIC of the manager
     * @return A list of enquiries for projects managed by the specified manager
     */
    List<Enquiry> getEnquiriesByManager(String managerNric);
    
    /**
     * Gets enquiries that have been responded to.
     * 
     * @return A list of enquiries that have responses
     */
    List<Enquiry> getEnquiriesWithResponses();
    
    /**
     * Gets enquiries that have not been responded to.
     * 
     * @return A list of enquiries without responses
     */
    List<Enquiry> getEnquiriesWithoutResponses();
    
    /**
     * Submits a new enquiry.
     * 
     * @param userNric The NRIC of the user submitting the enquiry
     * @param projectId The ID of the project the enquiry is about
     * @param enquiryText The text of the enquiry
     * @return The ID of the new enquiry if submission was successful, -1 otherwise
     */
    int submitEnquiry(String userNric, int projectId, String enquiryText);
    
    /**
     * Updates the text of an existing enquiry.
     * 
     * @param enquiryId The ID of the enquiry
     * @param userNric The NRIC of the user who submitted the enquiry
     * @param newText The new text for the enquiry
     * @return true if the update was successful, false otherwise
     */
    boolean updateEnquiryText(int enquiryId, String userNric, String newText);
    
    /**
     * Responds to an enquiry.
     * 
     * @param enquiryId The ID of the enquiry
     * @param officerNric The NRIC of the officer responding to the enquiry
     * @param response The response text
     * @return true if the response was successful, false otherwise
     */
    boolean respondToEnquiry(int enquiryId, String officerNric, String response);
    
    /**
     * Deletes an enquiry.
     * 
     * @param enquiryId The ID of the enquiry
     * @param userNric The NRIC of the user who submitted the enquiry
     * @return true if the deletion was successful, false otherwise
     */
    boolean deleteEnquiry(int enquiryId, String userNric);
    
    /**
     * Checks if an enquiry has been responded to.
     * 
     * @param enquiryId The ID of the enquiry
     * @return true if the enquiry has a response, false otherwise
     */
    boolean hasResponse(int enquiryId);
    
    /**
     * Checks if a user is authorized to modify an enquiry.
     * 
     * @param enquiryId The ID of the enquiry
     * @param userNric The NRIC of the user
     * @return true if the user is authorized, false otherwise
     */
    boolean isAuthorized(int enquiryId, String userNric);
}
