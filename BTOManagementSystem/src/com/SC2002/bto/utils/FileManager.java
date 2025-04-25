// File: src/com/SC2002/bto/utils/FileManager.java
package com.SC2002.bto.utils;

import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.ApplicationStatus;
import com.SC2002.bto.entities.Enquiry;
import com.SC2002.bto.entities.HDBManager;
import com.SC2002.bto.entities.HDBOfficer;
import com.SC2002.bto.entities.Project;
import com.SC2002.bto.entities.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for file operations in the BTO Management System.
 * This class handles reading from and writing to CSV files for various entities,
 * including users, projects, enquiries, and applications.
 */
public class FileManager {
    /** Date formatter for parsing and formatting dates in CSV files */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d/M/yy");
    
    /**
     * Ensures that the data directory exists, creating it if necessary.
     * This method should be called before any file operations.
     * 
     * @return true if the directory exists or was created successfully, false otherwise
     */
    public static boolean ensureDataDirectoryExists() {
        Path dataDir = Paths.get(Constants.DATA_DIR);
        if (!Files.exists(dataDir)) {
            try {
                Files.createDirectories(dataDir);
                System.out.println("Created data directory: " + dataDir.toAbsolutePath());
                return true;
            } catch (IOException e) {
                System.out.println("Error creating data directory: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * Loads users (Applicant, Officer, Manager) from their respective CSV files.
     * 
     * @param filePath the path to the CSV file
     * @param role the role of the users to load (e.g., "Applicant", "HDBOfficer", "HDBManager")
     * @return a list of users of the specified role
     */
    public static List<User> loadUsersFromCSV(String filePath, String role) {
        List<User> users = new ArrayList<>();
        File file = new File(filePath);
        
        // Check if file exists
        if (!file.exists()) {
            System.out.println("Warning: User file not found (" + filePath + "). Creating empty list.");
            return users;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine();  // skip header
            if (header == null) {
                System.out.println("Warning: User file is empty (" + filePath + ").");
                return users;
            }
            
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] tokens = line.split(",");
                    if (tokens.length < 5) {
                        System.out.println("Warning: Skipping invalid user record (insufficient fields): " + line);
                        continue;
                    }
                    
                    String name = tokens[0].trim();
                    String nric = tokens[1].trim();
                    int age;
                    try {
                        age = Integer.parseInt(tokens[2].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Invalid age format in user record: " + line);
                        continue;
                    }
                    
                    String ms = tokens[3].trim();
                    String pwd = tokens[4].trim();

                    User user = createUser(role, name, nric, pwd, age, ms);

                    if (user instanceof Applicant) {
                        Applicant a = (Applicant) user;
                        int appliedId = -1;
                        ApplicationStatus status = ApplicationStatus.NOT_APPLIED;
                        String flatType = "";

                        if (tokens.length >= 7) {
                            try { 
                                appliedId = Integer.parseInt(tokens[5].trim()); 
                            } catch (NumberFormatException e) {
                                System.out.println("Warning: Invalid project ID format in applicant record: " + line);
                                appliedId = -1;
                            }
                            
                            try { 
                                String statusStr = tokens[6].trim();
                                // Handle "Not Applied" case specifically
                                if ("Not Applied".equalsIgnoreCase(statusStr)) {
                                    status = ApplicationStatus.NOT_APPLIED;
                                } else {
                                    status = ApplicationStatus.valueOf(statusStr.toUpperCase()); 
                                }
                            } catch (IllegalArgumentException e) {
                                System.out.println("Warning: Invalid application status in applicant record: " + line);
                                status = ApplicationStatus.NOT_APPLIED;
                            }
                        }
                        
                        if (tokens.length >= 8) {
                            flatType = tokens[7].trim();
                        }

                        a.setAppliedProjectId(appliedId);
                        a.setApplicationStatus(status);
                        a.setAppliedFlatType(flatType);
                    }

                    users.add(user);
                } catch (Exception e) {
                    System.out.println("Warning: Error processing user record: " + line + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading user CSV (" + filePath + "): " + e.getMessage());
        }
        
        return users;
    }

    /**
     * Creates a user object of the specified role with the given details.
     * 
     * @param role the role of the user to create
     * @param name the user's name
     * @param nric the user's NRIC
     * @param password the user's password
     * @param age the user's age
     * @param maritalStatus the user's marital status
     * @return a user object of the appropriate type
     */
    private static User createUser(String role, String name, String nric, String password, int age, String maritalStatus) {
	    	if ("HDBOfficer".equalsIgnoreCase(role)) {
			return new HDBOfficer(name, nric, password, age, maritalStatus);} 
	    	else if ("HDBManager".equalsIgnoreCase(role)) {return new HDBManager(name, nric, password, age, maritalStatus);} 
	    	else {return new Applicant(name, nric, password, age, maritalStatus);
}
}

    /**
     * Loads all projects from the specified CSV file.
     * 
     * @param filePath the path to the CSV file
     * @return a list of projects
     */
    public static List<Project> loadProjectsFromCSV(String filePath) {
        List<Project> projects = new ArrayList<>();
        File file = new File(filePath);
        
        // Check if file exists
        if (!file.exists()) {
            System.out.println("Warning: Project file not found (" + filePath + "). Creating empty list.");
            return projects;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine(); // skip header
            if (header == null) {
                System.out.println("Warning: Project file is empty (" + filePath + ").");
                return projects;
            }
            
            String line;
            int autoId = 1;
            while ((line = br.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty() || line.trim().equals(",,,,,,,,,,,,,")) {
                    continue;
                }
                
                try {
                    // Handle quoted fields properly
                    List<String> tokens = parseCSVLine(line);
                    if (tokens.size() < 13) {
                        System.out.println("Warning: Skipping invalid project record (insufficient fields): " + line);
                        continue;
                    }
                    
                    // Skip if all fields are empty
                    boolean allEmpty = true;
                    for (String token : tokens) {
                        if (!token.trim().isEmpty()) {
                            allEmpty = false;
                            break;
                        }
                    }
                    
                    if (allEmpty) {
                        continue;
                    }

                    String name = tokens.get(0).trim();
                    String nb = tokens.get(1).trim();
                    String t1d = tokens.get(2).trim();
                    int t1u, t1p, t2u, t2p, slots;
                    LocalDate open, close;
                    
                    try {
                        t1u = Integer.parseInt(tokens.get(3).trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Invalid Type1 units format in project record: " + line);
                        continue;
                    }
                    
                    try {
                        t1p = Integer.parseInt(tokens.get(4).trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Invalid Type1 price format in project record: " + line);
                        continue;
                    }
                    
                    String t2d = tokens.get(5).trim();
                    
                    try {
                        t2u = Integer.parseInt(tokens.get(6).trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Invalid Type2 units format in project record: " + line);
                        continue;
                    }
                    
                    try {
                        t2p = Integer.parseInt(tokens.get(7).trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Invalid Type2 price format in project record: " + line);
                        continue;
                    }
                    
                    try {
                        open = LocalDate.parse(tokens.get(8).trim(), DATE_FMT);
                    } catch (Exception e) {
                        System.out.println("Warning: Invalid opening date format in project record: " + line);
                        continue;
                    }
                    
                    try {
                        close = LocalDate.parse(tokens.get(9).trim(), DATE_FMT);
                    } catch (Exception e) {
                        System.out.println("Warning: Invalid closing date format in project record: " + line);
                        continue;
                    }
                    
                    String mgr = tokens.get(10).trim();
                    
                    try {
                        slots = Integer.parseInt(tokens.get(11).trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Invalid officer slots format in project record: " + line);
                        continue;
                    }

                    String officerField = tokens.get(12).trim();
                    // Remove quotes if present
                    if (officerField.startsWith("\"") && officerField.endsWith("\"")) {
                        officerField = officerField.substring(1, officerField.length() - 1);
                    }
                    
                    List<String> officers = Arrays.stream(officerField.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();

                    Project p = new Project(autoId++, name, nb,
                                            t1d, t1u, t1p,
                                            t2d, t2u, t2p,
                                            open, close,
                                            mgr, slots, officers);
                    
                    // Set visibility if it exists in the CSV
                    if (tokens.size() > 13) {
                        try {
                            boolean visible = Boolean.parseBoolean(tokens.get(13).trim());
                            p.setVisible(visible);
                        } catch (Exception e) {
                            // Default to visible if there's an error parsing
                            p.setVisible(true);
                        }
                    }
                    
                    projects.add(p);
                } catch (Exception e) {
                    System.out.println("Warning: Error processing project record: " + line + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading projects: " + e.getMessage());
        }
        
        return projects;
    }


    /**
     * Parses a CSV line, handling quoted fields properly.
     * 
     * @param line the CSV line to parse
     * @return a list of tokens from the CSV line
     */
    private static List<String> parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentToken = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(currentToken.toString());
                currentToken = new StringBuilder();
            } else {
                currentToken.append(c);
            }
        }
        
        // Add the last token
        tokens.add(currentToken.toString());
        
        return tokens;
    }
    
    /**
     * Saves a list of projects to the project CSV file.
     * 
     * @param projects the list of projects to save
     * @return true if the projects were saved successfully, false otherwise
     */
    public static boolean saveProjects(List<Project> projects) {
        if (projects == null) {
            System.out.println("Warning: Null project list provided.");
            return false;
        }
        
        String filePath = Constants.PROJECT_CSV;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write("Project Name,Neighborhood,Type 1,Number of units for Type1,"
                   + "Selling price for Type1,Type 2,Number of units for Type2,"
                   + "Selling price for Type2,Application opening date,Application closing date,"
                   + "Manager,Officer Slot,Officer,Visible");
            bw.newLine();
            
            for (Project p : projects) {
                try {
                    // Validate project data
                    if (p.getProjectName() == null || p.getProjectName().trim().isEmpty()) {
                        System.out.println("Warning: Skipping project with empty name: " + p.getProjectId());
                        continue;
                    }
                    
                    if (p.getNeighborhood() == null || p.getNeighborhood().trim().isEmpty()) {
                        System.out.println("Warning: Skipping project with empty neighborhood: " + p.getProjectId());
                        continue;
                    }
                    
                    if (p.getApplicationOpeningDate() == null || p.getApplicationClosingDate() == null) {
                        System.out.println("Warning: Skipping project with invalid dates: " + p.getProjectId());
                        continue;
                    }
                    
                    // Build the CSV line manually to properly handle the officers list
                    StringBuilder sb = new StringBuilder();
                    sb.append(p.getProjectName()).append(",");
                    sb.append(p.getNeighborhood()).append(",");
                    sb.append(p.getType1Desc()).append(",");
                    sb.append(p.getType1Units()).append(",");
                    sb.append(p.getType1Price()).append(",");
                    sb.append(p.getType2Desc()).append(",");
                    sb.append(p.getType2Units()).append(",");
                    sb.append(p.getType2Price()).append(",");
                    sb.append(p.getApplicationOpeningDate().format(DATE_FMT)).append(",");
                    sb.append(p.getApplicationClosingDate().format(DATE_FMT)).append(",");
                    sb.append(p.getManager()).append(",");
                    sb.append(p.getOfficerSlots()).append(",");
                    
                    // Properly quote the officers list to handle commas
                    String officerList = p.getOfficers() != null && !p.getOfficers().isEmpty() 
                        ? "\"" + String.join(",", p.getOfficers()) + "\"" 
                        : "";
                    sb.append(officerList).append(",");
                    
                    // Add visibility as a separate column
                    sb.append(p.isVisible());
                    
                    bw.write(sb.toString());
                    bw.newLine();
                } catch (Exception ex) {
                    System.out.println("Warning: Error processing project: " + p.getProjectId() + " - " + ex.getMessage());
                }
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error saving projects: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds an officer registration for a project.
     * 
     * @param officerNric the NRIC of the officer
     * @param projectId the ID of the project
     * @return true if the registration was added successfully, false otherwise
     */
    public static boolean addOfficerRegistration(String officerNric, int projectId) {
        String path = Constants.REGISTRATION_CSV;
        File file = new File(path);
        boolean headerNeeded = !file.exists();

        // Load existing registrations
        int currentCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            if (!headerNeeded) br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 2) continue;
                int pid = Integer.parseInt(tokens[1].trim());
                if (pid == projectId) currentCount++;
            }
        } catch (IOException e) {
            // If file doesn't exist yet, that's fine — no registrations yet
        }

        if (currentCount >= 10) {
            System.out.println("❌ Registration limit reached: 10 officers already registered.");
            return false;
        }

        // Append new registration
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
            if (headerNeeded) {
                bw.write("OfficerNRIC,ProjectID"); bw.newLine();
            }
            bw.write(officerNric + "," + projectId);
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error saving officer registration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads all enquiries from the specified CSV file.
     * 
     * @param filePath the path to the CSV file
     * @return a list of enquiries
     */
    public static List<Enquiry> loadAllEnquiries(String filePath) {
        List<Enquiry> list = new ArrayList<>();
        File file = new File(filePath);
        
        // Check if file exists
        if (!file.exists()) {
            System.out.println("Warning: Enquiry file not found (" + filePath + "). Creating empty list.");
            return list;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine();
            if (header == null) {
                System.out.println("Warning: Enquiry file is empty (" + filePath + ").");
                return list;
            }
            
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] t = line.split(",", 5);
                    if (t.length < 4) {
                        System.out.println("Warning: Skipping invalid enquiry record (insufficient fields): " + line);
                        continue;
                    }
                    
                    int id;
                    try {
                        id = Integer.parseInt(t[0].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Invalid enquiry ID format in record: " + line);
                        continue;
                    }
                    
                    String user = t[1].trim();
                    
                    int pid;
                    try {
                        pid = Integer.parseInt(t[2].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Invalid project ID format in enquiry record: " + line);
                        continue;
                    }
                    
                    String txt = t[3].trim();
                    String resp = t.length > 4 ? t[4].trim() : "";
                    
                    Enquiry e = new Enquiry(id, user, txt, pid);
                    e.setResponse(resp);
                    list.add(e);
                } catch (Exception e) {
                    System.out.println("Warning: Error processing enquiry record: " + line + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading enquiries: " + e.getMessage());
        }
        
        return list;
    }

    /**
     * Updates the response for an enquiry in the CSV file.
     * 
     * @param enquiry the enquiry with the updated response
     * @return true if the response was updated successfully, false otherwise
     */
    public static boolean updateEnquiryResponse(Enquiry enquiry) {
        String path = Constants.ENQUIRY_CSV;
        File file = new File(path);
        
        // Check if file exists
        if (!file.exists()) {
            System.out.println("Warning: Enquiry file not found (" + path + ").");
            return false;
        }
        
        List<String> lines = new ArrayList<>();
        boolean found = false;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine();
            if (header == null) {
                System.out.println("Warning: Enquiry file is empty (" + path + ").");
                return false;
            }
            
            lines.add(header);
            String line;
            
            while ((line = br.readLine()) != null) {
                try {
                    String[] t = line.split(",", 5);
                    if (t.length < 4) {
                        System.out.println("Warning: Skipping invalid enquiry record (insufficient fields): " + line);
                        lines.add(line);
                        continue;
                    }
                    
                    int id;
                    try {
                        id = Integer.parseInt(t[0].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Invalid enquiry ID format in record: " + line);
                        lines.add(line);
                        continue;
                    }
                    
                    if (id == enquiry.getEnquiryId()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(id).append(",");
                        sb.append(enquiry.getUserNric()).append(",");
                        sb.append(enquiry.getProjectId()).append(",");
                        sb.append(enquiry.getEnquiryText()).append(",");
                        sb.append(enquiry.getResponse());
                        lines.add(sb.toString());
                        found = true;
                    } else {
                        lines.add(line);
                    }
                } catch (Exception e) {
                    System.out.println("Warning: Error processing enquiry record: " + line + " - " + e.getMessage());
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading enquiries: " + e.getMessage());
            return false;
        }
        
        if (!found) {
            System.out.println("Enquiry with ID " + enquiry.getEnquiryId() + " not found.");
            return false;
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (String l : lines) {
                bw.write(l); bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error writing enquiries: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates a user's password in the specified CSV file.
     * 
     * @param filePath the path to the CSV file
     * @param nric the NRIC of the user
     * @param newPassword the new password
     * @return true if the password was updated successfully, false otherwise
     */
    public static boolean updatePasswordInCSV(String filePath, String nric, String newPassword) {
        File file = new File(filePath);
        
        // Check if file exists
        if (!file.exists()) {
            System.out.println("Warning: User file not found (" + filePath + ").");
            return false;
        }
        
        List<String> lines = new ArrayList<>();
        boolean found = false;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine();
            if (header == null) {
                System.out.println("Warning: User file is empty (" + filePath + ").");
                return false;
            }
            
            lines.add(header);
            String line;
            
            while ((line = br.readLine()) != null) {
                try {
                    String[] tokens = line.split(",");
                    if (tokens.length < 5) {
                        System.out.println("Warning: Skipping invalid user record (insufficient fields): " + line);
                        lines.add(line);
                        continue;
                    }
                    
                    if (tokens[1].trim().equalsIgnoreCase(nric)) {
                        tokens[4] = newPassword;
                        found = true;
                    }
                    
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < tokens.length; i++) {
                        sb.append(tokens[i].trim());
                        if (i < tokens.length - 1) {
                            sb.append(",");
                        }
                    }
                    lines.add(sb.toString());
                } catch (Exception e) {
                    System.out.println("Warning: Error processing user record: " + line + " - " + e.getMessage());
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file (" + filePath + "): " + e.getMessage());
            return false;
        }
        
        if (!found) {
            System.out.println("User with NRIC " + nric + " not found in file " + filePath);
            return false;
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String outputLine : lines) {
                bw.write(outputLine);
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error writing CSV file (" + filePath + "): " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if an applicant with the specified NRIC exists in the applicant CSV file.
     * 
     * @param nric the NRIC to check
     * @return true if an applicant with the specified NRIC exists, false otherwise
     */
    public static boolean applicantNricExists(String nric) {
        List<User> applicants = loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant");
        for (User applicant : applicants) {
            if (applicant.getNric().equalsIgnoreCase(nric)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Updates an applicant's application details (applied project, status, and flat type) in the CSV file.
     * 
     * @param applicant the applicant with the updated application details
     * @return true if the application details were updated successfully, false otherwise
     */
    public static boolean updateApplicantApplication(Applicant applicant) {
        String filePath = Constants.APPLICANT_CSV;
        File file = new File(filePath);
        
        // Check if file exists
        if (!file.exists()) {
            System.out.println("Warning: Applicant file not found (" + filePath + ").");
            // Create the file with header
            try {
                ensureDataDirectoryExists();
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
                    bw.write("Name,NRIC,Age,MaritalStatus,Password,AppliedID,Status,FlatType");
                    bw.newLine();
                }
            } catch (IOException e) {
                System.out.println("Error creating ApplicantList.csv: " + e.getMessage());
                return false;
            }
        }
        
        // Check if the applicant is an OfficerAsApplicant
        boolean isOfficerAsApplicant = applicant instanceof com.SC2002.bto.entities.OfficerAsApplicant;
        
        // Check if the applicant already exists in the CSV
        boolean applicantExists = applicantNricExists(applicant.getNric());
        
        // If it's an officer acting as an applicant and they don't exist in the CSV yet, add them
        if (isOfficerAsApplicant && !applicantExists) {
            return createApplicantRecordForOfficer(applicant);
        }
        
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            if (header == null) {
                System.out.println("Warning: Applicant file is empty (" + filePath + ").");
                // Create header
                header = "Name,NRIC,Age,MaritalStatus,Password,AppliedID,Status,FlatType";
            }
            
            String[] hdrTokens = header.split(",");
            // Ensure header has columns: ...,AppliedID,Status,FlatType
            if (hdrTokens.length < 7) header += ",AppliedID,Status";
            if (header.split(",").length < 8) header += ",FlatType";
            lines.add(header);

            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] tokens = line.split(",");
                    if (tokens.length < 5) {
                        System.out.println("Warning: Skipping invalid applicant record (insufficient fields): " + line);
                        lines.add(line);
                        continue;
                    }
                    
                    if (tokens[1].trim().equalsIgnoreCase(applicant.getNric())) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(tokens[0].trim()).append(",");
                        sb.append(tokens[1].trim()).append(",");
                        sb.append(tokens[2].trim()).append(",");
                        sb.append(tokens[3].trim()).append(",");
                        sb.append(tokens[4].trim()).append(",");
                        sb.append(applicant.getAppliedProjectId()).append(",");
                        // Use NOT_APPLIED for consistency in the CSV file
                        String statusStr = applicant.getApplicationStatus() == ApplicationStatus.NOT_APPLIED ? 
                                          "NOT_APPLIED" : applicant.getApplicationStatus().name();
                        sb.append(statusStr).append(",");
                        sb.append(applicant.getAppliedFlatType());
                        lines.add(sb.toString());
                        found = true;
                    } else {
                        // Preserve other lines; pad if missing columns
                        if (tokens.length >= 8) {
                            lines.add(line);
                        } else {
                            // add defaults
                            String pad = (tokens.length >= 7 ? "" : ",-1,NOT_APPLIED") + ",\"\"";
                            lines.add(line + pad);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Warning: Error processing applicant record: " + line + " - " + e.getMessage());
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading ApplicantList.csv: " + e.getMessage());
            return false;
        }

        if (!found) {
            // If the applicant wasn't found in the file but we know they exist in the system,
            // it might be a new applicant or an officer acting as an applicant for the first time
            if (isOfficerAsApplicant) {
                return createApplicantRecordForOfficer(applicant);
            } else {
                System.out.println("Applicant NRIC not found: " + applicant.getNric());
                return false;
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String out : lines) {
                bw.write(out);
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error writing ApplicantList.csv: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Creates a new applicant record for an officer in the ApplicantList.csv file.
     * This is used when an officer applies for a project as an applicant for the first time.
     * 
     * @param applicant the applicant (which is actually an OfficerAsApplicant)
     * @return true if the record was created successfully, false otherwise
     */
    private static boolean createApplicantRecordForOfficer(Applicant applicant) {
        String filePath = Constants.APPLICANT_CSV;
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append(applicant.getName()).append(",");
            sb.append(applicant.getNric()).append(",");
            sb.append(applicant.getAge()).append(",");
            sb.append(applicant.getMaritalStatus()).append(",");
            sb.append(applicant.getPassword()).append(",");
            sb.append(applicant.getAppliedProjectId()).append(",");
            
            // Use NOT_APPLIED for consistency in the CSV file
            String statusStr = applicant.getApplicationStatus() == ApplicationStatus.NOT_APPLIED ? 
                              "NOT_APPLIED" : applicant.getApplicationStatus().name();
            sb.append(statusStr).append(",");
            sb.append(applicant.getAppliedFlatType());
            
            bw.write(sb.toString());
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error creating applicant record for officer: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Saves a list of enquiries to the enquiry CSV file.
     * 
     * @param list the list of enquiries to save
     * @return true if the enquiries were saved successfully, false otherwise
     */
    public static boolean saveAllEnquiries(List<Enquiry> list) {
        if (list == null) {
            System.out.println("Warning: Null enquiry list provided.");
            return false;
        }
        
        String path = Constants.ENQUIRY_CSV;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write("ID,UserNRIC,ProjectID,EnquiryText,Response");
            bw.newLine();
            
            for (Enquiry e : list) {
                try {
                    // Validate enquiry data
                    if (e.getEnquiryId() <= 0) {
                        System.out.println("Warning: Skipping enquiry with invalid ID: " + e.getEnquiryId());
                        continue;
                    }
                    
                    if (e.getUserNric() == null || e.getUserNric().trim().isEmpty()) {
                        System.out.println("Warning: Skipping enquiry with empty user NRIC: " + e.getEnquiryId());
                        continue;
                    }
                    
                    if (e.getProjectId() <= 0) {
                        System.out.println("Warning: Skipping enquiry with invalid project ID: " + e.getEnquiryId());
                        continue;
                    }
                    
                    // Write enquiry to file with proper escaping of commas
                    bw.write(e.getEnquiryId() + "," +
                             e.getUserNric() + "," +
                             e.getProjectId() + "," +
                             e.getEnquiryText().replace(",", ";") + "," +
                             e.getResponse().replace(",", ";"));
                    bw.newLine();
                } catch (Exception ex) {
                    System.out.println("Warning: Error processing enquiry: " + e.getEnquiryId() + " - " + ex.getMessage());
                }
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error saving enquiries: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the number of booked units for a specific project and flat type.
     * 
     * @param projectId the ID of the project
     * @param flatType the flat type
     * @return the number of booked units
     */
    public static int getBookedUnits(int projectId, String flatType) {
        List<User> users = FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant");
        return (int) users.stream()
            .filter(u -> u instanceof Applicant)
            .map(u -> (Applicant) u)
            .filter(a -> a.getAppliedProjectId() == projectId &&
                         flatType.equalsIgnoreCase(a.getAppliedFlatType()) &&
                         a.getApplicationStatus() == ApplicationStatus.BOOKED)
            .count();
    }

}
