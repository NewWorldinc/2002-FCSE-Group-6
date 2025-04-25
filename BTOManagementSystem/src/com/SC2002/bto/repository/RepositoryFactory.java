package com.SC2002.bto.repository;

import com.SC2002.bto.repository.csv.CSVApplicationRepository;
import com.SC2002.bto.repository.csv.CSVEnquiryRepository;
import com.SC2002.bto.repository.csv.CSVOfficerRegistrationRepository;
import com.SC2002.bto.repository.csv.CSVProjectRepository;
import com.SC2002.bto.repository.csv.CSVUserRepository;
import com.SC2002.bto.utils.Constants;

/**
 * Factory for creating repository instances.
 * Follows the Factory Pattern for object creation.
 */
public class RepositoryFactory {
    
    private static final IProjectRepository projectRepository = new CSVProjectRepository(Constants.PROJECT_CSV);
    private static final IEnquiryRepository enquiryRepository = new CSVEnquiryRepository(Constants.ENQUIRY_CSV);
    private static final IApplicationRepository applicationRepository = new CSVApplicationRepository();
    private static final IOfficerRegistrationRepository officerRegistrationRepository = new CSVOfficerRegistrationRepository(Constants.REGISTRATION_CSV);
    private static final IUserRepository userRepository = new CSVUserRepository();
    
    /**
     * Gets a project repository instance.
     * 
     * @return The project repository
     */
    public static IProjectRepository getProjectRepository() {
        return projectRepository;
    }
    
    /**
     * Gets an enquiry repository instance.
     * 
     * @return The enquiry repository
     */
    public static IEnquiryRepository getEnquiryRepository() {
        return enquiryRepository;
    }
    
    /**
     * Gets an application repository instance.
     * 
     * @return The application repository
     */
    public static IApplicationRepository getApplicationRepository() {
        return applicationRepository;
    }
    
    /**
     * Gets an officer registration repository instance.
     * 
     * @return The officer registration repository
     */
    public static IOfficerRegistrationRepository getOfficerRegistrationRepository() {
        return officerRegistrationRepository;
    }
    
    /**
     * Gets a user repository instance.
     * 
     * @return The user repository
     */
    public static IUserRepository getUserRepository() {
        return userRepository;
    }
    
    private RepositoryFactory() {
        // Private constructor to prevent instantiation
    }
}
