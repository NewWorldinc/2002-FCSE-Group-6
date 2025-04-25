package com.SC2002.bto.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a BTO (Build-To-Order) housing project.
 * This class encapsulates all the details of a housing project including
 * project information, flat types, pricing, application dates, and management details.
 */
public class Project {
    /** Unique identifier for the project */
    private int projectId;
    /** Name of the BTO project */
    private String projectName;
    /** Neighborhood/location of the project */
    private String neighborhood;
    /** Description of the first flat type (typically 2-Room) */
    private String type1Desc;
    /** Number of available units for the first flat type */
    private int type1Units;
    /** Price of the first flat type in SGD */
    private int type1Price;
    /** Description of the second flat type (typically 3-Room) */
    private String type2Desc;
    /** Number of available units for the second flat type */
    private int type2Units;
    /** Price of the second flat type in SGD */
    private int type2Price;
    /** Date when applications for this project open */
    private LocalDate applicationOpeningDate;
    /** Date when applications for this project close */
    private LocalDate applicationClosingDate;
    /** NRIC of the HDB manager responsible for this project */
    private String manager;
    /** Number of HDB officer positions available for this project */
    private int officerSlots;
    /** List of HDB officers (by NRIC) assigned to this project */
    private List<String> officers = new ArrayList<>(); // Replaces old 'officer'

    /** Flag indicating whether the project is visible to applicants */
    private boolean visible = true;
    /** Original number of units for the first flat type (for tracking allocations) */
    private int originalType1Units;
    /** Original number of units for the second flat type (for tracking allocations) */
    private int originalType2Units;

    /**
     * Constructs a new Project with all required details.
     *
     * @param projectId Unique identifier for the project
     * @param projectName Name of the BTO project
     * @param neighborhood Neighborhood/location of the project
     * @param type1Desc Description of the first flat type
     * @param type1Units Number of available units for the first flat type
     * @param type1Price Price of the first flat type in SGD
     * @param type2Desc Description of the second flat type
     * @param type2Units Number of available units for the second flat type
     * @param type2Price Price of the second flat type in SGD
     * @param applicationOpeningDate Date when applications for this project open
     * @param applicationClosingDate Date when applications for this project close
     * @param manager NRIC of the HDB manager responsible for this project
     * @param officerSlots Number of HDB officer positions available for this project
     * @param officers List of HDB officers (by NRIC) assigned to this project
     */
    public Project(int projectId, String projectName, String neighborhood,
                   String type1Desc, int type1Units, int type1Price,
                   String type2Desc, int type2Units, int type2Price,
                   LocalDate applicationOpeningDate, LocalDate applicationClosingDate,
                   String manager, int officerSlots, List<String> officers) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.neighborhood = neighborhood;
        this.type1Desc = type1Desc;
        this.type1Units = type1Units;
        this.type1Price = type1Price;
        this.type2Desc = type2Desc;
        this.type2Units = type2Units;
        this.type2Price = type2Price;
        this.applicationOpeningDate = applicationOpeningDate;
        this.applicationClosingDate = applicationClosingDate;
        this.manager = manager;
        this.officerSlots = officerSlots;
        this.officers = officers != null ? officers : new ArrayList<>();
        this.originalType1Units = type1Units;
        this.originalType2Units = type2Units;
    }

    // Getters
    /**
     * @return The unique identifier for the project
     */
    public int getProjectId() { return projectId; }
    
    /**
     * @return The name of the BTO project
     */
    public String getProjectName() { return projectName; }
    
    /**
     * @return The neighborhood/location of the project
     */
    public String getNeighborhood() { return neighborhood; }
    
    /**
     * @return The description of the first flat type
     */
    public String getType1Desc() { return type1Desc; }
    
    /**
     * @return The number of available units for the first flat type
     */
    public int getType1Units() { return type1Units; }
    
    /**
     * @return The price of the first flat type in SGD
     */
    public int getType1Price() { return type1Price; }
    
    /**
     * @return The description of the second flat type
     */
    public String getType2Desc() { return type2Desc; }
    
    /**
     * @return The number of available units for the second flat type
     */
    public int getType2Units() { return type2Units; }
    
    /**
     * @return The price of the second flat type in SGD
     */
    public int getType2Price() { return type2Price; }
    
    /**
     * @return The date when applications for this project open
     */
    public LocalDate getApplicationOpeningDate() { return applicationOpeningDate; }
    
    /**
     * @return The date when applications for this project close
     */
    public LocalDate getApplicationClosingDate() { return applicationClosingDate; }
    
    /**
     * @return The NRIC of the HDB manager responsible for this project
     */
    public String getManager() { return manager; }
    
    /**
     * @return The number of HDB officer positions available for this project
     */
    public int getOfficerSlots() { return officerSlots; }
    
    /**
     * @return The list of HDB officers (by NRIC) assigned to this project
     */
    public List<String> getOfficers() { return officers; }
    
    /**
     * @return True if the project is visible to applicants, false otherwise
     */
    public boolean isVisible() { return visible; }
    
    /**
     * @return The original number of units for the first flat type
     */
    public int getOriginalType1Units() { return originalType1Units; }
    
    /**
     * @return The original number of units for the second flat type
     */
    public int getOriginalType2Units() { return originalType2Units; }

    // Setters
    /**
     * @param projectId The unique identifier to set for the project
     */
    public void setProjectId(int projectId) { this.projectId = projectId; }
    
    /**
     * @param projectName The name to set for the BTO project
     */
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    /**
     * @param neighborhood The neighborhood/location to set for the project
     */
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }
    
    /**
     * @param type1Desc The description to set for the first flat type
     */
    public void setType1Desc(String type1Desc) { this.type1Desc = type1Desc; }
    
    /**
     * @param type1Units The number of available units to set for the first flat type
     */
    public void setType1Units(int type1Units) { this.type1Units = type1Units; }
    
    /**
     * @param type1Price The price to set for the first flat type in SGD
     */
    public void setType1Price(int type1Price) { this.type1Price = type1Price; }
    
    /**
     * @param type2Desc The description to set for the second flat type
     */
    public void setType2Desc(String type2Desc) { this.type2Desc = type2Desc; }
    
    /**
     * @param type2Units The number of available units to set for the second flat type
     */
    public void setType2Units(int type2Units) { this.type2Units = type2Units; }
    
    /**
     * @param type2Price The price to set for the second flat type in SGD
     */
    public void setType2Price(int type2Price) { this.type2Price = type2Price; }
    
    /**
     * @param applicationOpeningDate The date to set for when applications for this project open
     */
    public void setApplicationOpeningDate(LocalDate applicationOpeningDate) { this.applicationOpeningDate = applicationOpeningDate; }
    
    /**
     * @param applicationClosingDate The date to set for when applications for this project close
     */
    public void setApplicationClosingDate(LocalDate applicationClosingDate) { this.applicationClosingDate = applicationClosingDate; }
    
    /**
     * @param visible True to make the project visible to applicants, false to hide it
     */
    public void setVisible(boolean visible) { this.visible = visible; }
    
    /**
     * @param officerSlots The number of HDB officer positions to set for this project
     */
    public void setOfficerSlots(int officerSlots) { this.officerSlots = officerSlots; }
    
    /**
     * @param officers The list of HDB officers (by NRIC) to assign to this project
     */
    public void setOfficers(List<String> officers) { this.officers = officers; }

    /**
     * Checks if an officer is assigned to this project.
     * The method performs a case-insensitive partial match on the officer's name or NRIC.
     *
     * @param nameOrNric The name or NRIC of the officer to check
     * @return True if the officer is assigned to this project, false otherwise
     */
    public boolean isOfficerAssigned(String nameOrNric) {
        return officers.stream().anyMatch(o -> {
            String norm = o.trim().toLowerCase();
            return nameOrNric.toLowerCase().contains(norm) || norm.contains(nameOrNric.toLowerCase());
        });
    }

    /**
     * Checks if the project is currently open for application.
     * 
     * @return true if current date is within application period, false otherwise.
     */
    public boolean isCurrentlyOpen() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(applicationOpeningDate) && 
               !today.isAfter(applicationClosingDate);
    }


    /**
     * Returns a string representation of this project.
     * 
     * @return A string containing the project ID, name, and neighborhood
     */
    @Override
    public String toString() {
        return String.format("Project ID: %d | Name: %s | Neighbourhood: %s", projectId, projectName, neighborhood);
    }
}
