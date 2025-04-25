package com.SC2002.bto.repository;

import com.SC2002.bto.entities.Enquiry;
import java.util.List;

/**
 * Repository interface for Enquiry entities.
 * Follows Interface Segregation Principle by extending the generic repository
 * and adding enquiry-specific operations.
 */
public interface IEnquiryRepository extends IRepository<Enquiry, Integer> {
    
    /**
     * Finds enquiries submitted by a specific user.
     * 
     * @param userNric The NRIC of the user
     * @return A list of enquiries submitted by the specified user
     */
    List<Enquiry> findByUser(String userNric);
    
    /**
     * Finds enquiries related to a specific project.
     * 
     * @param projectId The ID of the project
     * @return A list of enquiries related to the specified project
     */
    List<Enquiry> findByProject(int projectId);
    
    /**
     * Finds enquiries that have been responded to.
     * 
     * @return A list of enquiries that have responses
     */
    List<Enquiry> findWithResponses();
    
    /**
     * Finds enquiries that have not been responded to.
     * 
     * @return A list of enquiries without responses
     */
    List<Enquiry> findWithoutResponses();
    
    /**
     * Updates the response for an enquiry.
     * 
     * @param enquiryId The ID of the enquiry
     * @param response The response text
     * @return true if the response was updated successfully, false otherwise
     */
    boolean updateResponse(int enquiryId, String response);
    
    /**
     * Updates the text of an enquiry.
     * 
     * @param enquiryId The ID of the enquiry
     * @param enquiryText The new enquiry text
     * @return true if the enquiry text was updated successfully, false otherwise
     */
    boolean updateEnquiryText(int enquiryId, String enquiryText);
}
