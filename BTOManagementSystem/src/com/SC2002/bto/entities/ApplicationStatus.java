package com.SC2002.bto.entities;

/**
 * Represents the possible statuses of a BTO application.
 * This enum defines the lifecycle states of an application from not applied to final outcome.
 */
public enum ApplicationStatus {
    /**
     * The applicant has not applied for any BTO project.
     */
    NOT_APPLIED,
    
    /**
     * The application has been submitted and is awaiting processing.
     */
    PENDING,
    
    /**
     * The application has been processed and was successful.
     * The applicant is eligible to book a flat.
     */
    SUCCESSFUL,
    
    /**
     * The applicant has successfully booked a flat.
     */
    BOOKED,
    
    /**
     * The application has been processed and was unsuccessful.
     */
    UNSUCCESSFUL,
    
    /**
     * The applicant has requested to withdraw their application,
     * and the withdrawal is pending approval.
     */
    PENDING_WITHDRAWAL
}
