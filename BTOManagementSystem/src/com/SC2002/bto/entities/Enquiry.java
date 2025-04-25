// File: Enquiry.java
package com.SC2002.bto.entities;

/**
 * Enquiry represents a query submitted by a user regarding a specific project.
 * It includes details such as a unique enquiry ID, the NRIC of the user who submitted the enquiry,
 * the enquiry text, an optional response, and the associated project ID.
 */
public class Enquiry {
    private int enquiryId;
    private String userNric;
    private String enquiryText;
    private String response;
    private int projectId;  // The project associated with this enquiry

    /**
     * Constructs an Enquiry with the specified details.
     * 
     * @param enquiryId   the unique ID for this enquiry.
     * @param userNric    the NRIC of the user who submitted the enquiry.
     * @param enquiryText the content of the enquiry.
     * @param projectId   the ID of the project related to this enquiry.
     */
    public Enquiry(int enquiryId, String userNric, String enquiryText, int projectId) {
        this.enquiryId = enquiryId;
        this.userNric = userNric;
        this.enquiryText = enquiryText;
        this.projectId = projectId;
        this.response = ""; // Default: no response yet
    }
    
    // Getters and Setters
    
    /**
     * Gets the unique ID of this enquiry.
     * 
     * @return the enquiry ID
     */
    public int getEnquiryId() {
        return enquiryId;
    }
    
    /**
     * Gets the NRIC of the user who submitted this enquiry.
     * 
     * @return the user's NRIC
     */
    public String getUserNric() {
        return userNric;
    }
    
    /**
     * Gets the content of this enquiry.
     * 
     * @return the enquiry text
     */
    public String getEnquiryText() {
        return enquiryText;
    }
    
    /**
     * Sets the content of this enquiry.
     * 
     * @param enquiryText the new enquiry text
     */
    public void setEnquiryText(String enquiryText) {
        this.enquiryText = enquiryText;
    }
    
    /**
     * Gets the response to this enquiry.
     * 
     * @return the response text, or an empty string if no response has been provided
     */
    public String getResponse() {
        return response;
    }
    
    /**
     * Sets the response to this enquiry.
     * 
     * @param response the response text
     */
    public void setResponse(String response) {
        this.response = response;
    }
    
    /**
     * Gets the ID of the project associated with this enquiry.
     * 
     * @return the project ID
     */
    public int getProjectId() {
        return projectId;
    }
    
    /**
     * Sets the ID of the project associated with this enquiry.
     * 
     * @param projectId the project ID
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    /**
     * Checks if this enquiry has received a response.
     * 
     * @return true if a non-empty response has been provided, false otherwise
     */
    public boolean hasResponse() {
        return response != null && !response.trim().isEmpty();
    }

    /**
     * Returns a string representation of this enquiry.
     * 
     * @return a formatted string containing the enquiry details
     */
    @Override
    public String toString() {
        return String.format(
            "Enquiry #%d | Project ID: %d | By: %s\nText: %s\nResponse: %s\n",
            enquiryId, projectId, userNric, enquiryText,
            hasResponse() ? response : "No response yet"
        );
    }
}
