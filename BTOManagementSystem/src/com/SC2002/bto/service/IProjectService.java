package com.SC2002.bto.service;

import com.SC2002.bto.entities.Project;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for project-related operations.
 * Follows Interface Segregation Principle by providing a focused contract.
 */
public interface IProjectService {
    
    /**
     * Gets all projects.
     * 
     * @return A list of all projects
     */
    List<Project> getAllProjects();
    
    /**
     * Gets all visible projects.
     * 
     * @return A list of all visible projects
     */
    List<Project> getVisibleProjects();
    
    /**
     * Gets a project by its ID.
     * 
     * @param projectId The ID of the project
     * @return An Optional containing the project if found, empty otherwise
     */
    Optional<Project> getProjectById(int projectId);
    
    /**
     * Gets projects by neighborhood.
     * 
     * @param neighborhood The neighborhood to search for
     * @return A list of projects in the specified neighborhood
     */
    List<Project> getProjectsByNeighborhood(String neighborhood);
    
    /**
     * Gets projects that have a specific flat type.
     * 
     * @param flatType The flat type to search for (e.g., "2-Room", "3-Room")
     * @return A list of projects offering the specified flat type
     */
    List<Project> getProjectsByFlatType(String flatType);
    
    /**
     * Gets projects managed by a specific HDB manager.
     * 
     * @param managerNric The NRIC of the manager
     * @return A list of projects managed by the specified manager
     */
    List<Project> getProjectsByManager(String managerNric);
    
    /**
     * Gets projects that a specific HDB officer is assigned to.
     * 
     * @param officerNric The NRIC of the officer
     * @return A list of projects the officer is assigned to
     */
    List<Project> getProjectsByOfficer(String officerNric);
    
    /**
     * Gets projects with application periods that include the specified date.
     * 
     * @param date The date to check
     * @return A list of projects with application periods that include the specified date
     */
    List<Project> getProjectsByApplicationPeriod(LocalDate date);
    
    /**
     * Updates the visibility of a project.
     * 
     * @param projectId The ID of the project
     * @param isVisible The new visibility status
     * @return true if the update was successful, false otherwise
     */
    boolean updateVisibility(int projectId, boolean isVisible);
    
    /**
     * Updates the available units for a specific flat type in a project.
     * 
     * @param projectId The ID of the project
     * @param flatType The flat type to update (e.g., "2-Room", "3-Room")
     * @param units The new number of available units
     * @return true if the update was successful, false otherwise
     */
    boolean updateAvailableUnits(int projectId, String flatType, int units);
    
    /**
     * Books a unit of the specified flat type in a project.
     * 
     * @param projectId The ID of the project
     * @param flatType The flat type to book (e.g., "2-Room", "3-Room")
     * @return true if a unit was successfully booked, false otherwise
     */
    boolean bookUnit(int projectId, String flatType);
    
    /**
     * Gets projects that match the eligibility criteria for an applicant.
     * 
     * @param age The age of the applicant
     * @param maritalStatus The marital status of the applicant
     * @return A list of projects that match the eligibility criteria
     */
    List<Project> getEligibleProjects(int age, String maritalStatus);
    
    /**
     * Checks if a project has available units for a specific flat type.
     * 
     * @param projectId The ID of the project
     * @param flatType The flat type to check (e.g., "2-Room", "3-Room")
     * @return true if the project has available units for the specified flat type, false otherwise
     */
    boolean hasAvailableUnits(int projectId, String flatType);
    
    /**
     * Gets the number of booked units for a specific flat type in a project.
     * 
     * @param projectId The ID of the project
     * @param flatType The flat type to check (e.g., "2-Room", "3-Room")
     * @return The number of booked units
     */
    int getBookedUnits(int projectId, String flatType);
}
