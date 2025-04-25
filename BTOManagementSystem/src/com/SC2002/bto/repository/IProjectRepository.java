package com.SC2002.bto.repository;

import com.SC2002.bto.entities.Project;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Project entities.
 * Follows Interface Segregation Principle by extending the generic repository
 * and adding project-specific operations.
 */
public interface IProjectRepository extends IRepository<Project, Integer> {
    
    /**
     * Finds all visible projects.
     * 
     * @return A list of all visible projects
     */
    List<Project> findAllVisible();
    
    /**
     * Finds projects by neighborhood.
     * 
     * @param neighborhood The neighborhood to search for
     * @return A list of projects in the specified neighborhood
     */
    List<Project> findByNeighborhood(String neighborhood);
    
    /**
     * Finds projects that have a specific flat type.
     * 
     * @param flatType The flat type to search for (e.g., "2-Room", "3-Room")
     * @return A list of projects offering the specified flat type
     */
    List<Project> findByFlatType(String flatType);
    
    /**
     * Finds projects managed by a specific HDB manager.
     * 
     * @param managerNric The NRIC of the manager
     * @return A list of projects managed by the specified manager
     */
    List<Project> findByManager(String managerNric);
    
    /**
     * Finds projects that a specific HDB officer is assigned to.
     * 
     * @param officerNric The NRIC of the officer
     * @return A list of projects the officer is assigned to
     */
    List<Project> findByOfficer(String officerNric);
    
    /**
     * Finds projects with application periods that include the specified date.
     * 
     * @param date The date to check
     * @return A list of projects with application periods that include the specified date
     */
    List<Project> findByApplicationPeriod(LocalDate date);
    
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
}
