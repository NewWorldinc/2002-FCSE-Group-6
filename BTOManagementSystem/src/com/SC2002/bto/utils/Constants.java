package com.SC2002.bto.utils;

/**
 * Constants used throughout the application.
 * Follows the Single Responsibility Principle by centralizing all constants.
 */
public class Constants {
    
    // File paths - using relative paths from where the application is run
    /** Directory where all data files are stored */
    public static final String DATA_DIR = "data";
    /** Path to the CSV file containing applicant data */
    public static final String APPLICANT_CSV = DATA_DIR + "/ApplicantList.csv";
    /** Path to the CSV file containing HDB officer data */
    public static final String OFFICER_CSV = DATA_DIR + "/OfficerList.csv";
    /** Path to the CSV file containing HDB manager data */
    public static final String MANAGER_CSV = DATA_DIR + "/ManagerList.csv";
    /** Path to the CSV file containing project data */
    public static final String PROJECT_CSV = DATA_DIR + "/ProjectList.csv";
    /** Path to the CSV file containing enquiry data */
    public static final String ENQUIRY_CSV = DATA_DIR + "/EnquiryList.csv";
    /** Path to the CSV file containing officer registration data */
    public static final String REGISTRATION_CSV = DATA_DIR + "/RegistrationList.csv";
    
    // Default values
    /** Default password for new users */
    public static final String DEFAULT_PASSWORD = "password123";
    
    // Validation constants
    /** Minimum length required for a valid password */
    public static final int MIN_PASSWORD_LENGTH = 8;
    /** Minimum age required for a single applicant to be eligible */
    public static final int MIN_SINGLE_AGE = 35;
    /** Minimum age required for a married applicant to be eligible */
    public static final int MIN_MARRIED_AGE = 21;
    
    // Application status constants
    /** Status indicating an application is pending processing */
    public static final String STATUS_PENDING = "PENDING";
    /** Status indicating an application was successful */
    public static final String STATUS_SUCCESSFUL = "SUCCESSFUL";
    /** Status indicating an application was unsuccessful */
    public static final String STATUS_UNSUCCESSFUL = "UNSUCCESSFUL";
    /** Status indicating a flat has been booked */
    public static final String STATUS_BOOKED = "BOOKED";
    /** Status indicating no application has been made */
    public static final String STATUS_NOT_APPLIED = "NOT_APPLIED";
    /** Status indicating a withdrawal request is pending */
    public static final String STATUS_PENDING_WITHDRAWAL = "PENDING_WITHDRAWAL";
    
    // Flat types
    /** Constant for 2-Room flat type */
    public static final String FLAT_TYPE_2_ROOM = "2-Room";
    /** Constant for 3-Room flat type */
    public static final String FLAT_TYPE_3_ROOM = "3-Room";
    
    // Marital status
    /** Constant for single marital status */
    public static final String MARITAL_STATUS_SINGLE = "Single";
    /** Constant for married marital status */
    public static final String MARITAL_STATUS_MARRIED = "Married";
    
    // User types
    /** Constant for applicant user type */
    public static final String USER_TYPE_APPLICANT = "Applicant";
    /** Constant for HDB officer user type */
    public static final String USER_TYPE_OFFICER = "HDBOfficer";
    /** Constant for HDB manager user type */
    public static final String USER_TYPE_MANAGER = "HDBManager";
    
    // Menu options
    /** Constant for the back menu option */
    public static final String OPTION_BACK = "Back";
    /** Constant for the exit menu option */
    public static final String OPTION_EXIT = "Exit";
    
    // Date format
    /** Format used for dates throughout the application (day/month/year) */
    public static final String DATE_FORMAT = "d/M/yy";
    
    private Constants() {
        // Private constructor to prevent instantiation
    }
}
