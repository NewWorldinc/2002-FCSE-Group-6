package com.SC2002.bto.control;

//File: ProjectController.java

import java.util.List;

import com.SC2002.bto.di.ServiceLocator;
import com.SC2002.bto.entities.Project;
import com.SC2002.bto.service.IProjectService;

/**
* ProjectController handles operations related to project management such as
* retrieving project listings, filtering, and toggling project visibility.
* Follows the Dependency Inversion Principle by depending on the service interface.
*/
public class ProjectController {
    
    private final IProjectService projectService;
    
    /**
     * Constructs a ProjectController with the default project service.
     */
    public ProjectController() {
        this.projectService = ServiceLocator.get(IProjectService.class);
    }
    
    /**
     * Constructs a ProjectController with the specified project service.
     * 
     * @param projectService The project service
     */
    public ProjectController(IProjectService projectService) {
        this.projectService = projectService;
    }
 
    /**
     * Books a flat unit in a project.
     * 
     * @param projectId The ID of the project
     * @param flatType The flat type to book (e.g., "2-Room", "3-Room")
     * @return true if a unit was successfully booked, false otherwise
     */
	public boolean bookFlatUnit(int projectId, String flatType) {
        return projectService.bookUnit(projectId, flatType);
    }
	
    /**
     * Retrieves a list of available projects.
     *
     * @return a list of projects.
     */
	public List<Project> getAvailableProjects() {
	    return projectService.getVisibleProjects();
	}

    /**
     * Toggles the visibility status of a project.
     *
     * @param projectId the identifier for the project.
     * @param isVisible the new visibility status.
     * @return true if the operation was successful, false otherwise.
     */
	public boolean toggleProjectVisibility(int projectId, boolean isVisible) {
	    return projectService.updateVisibility(projectId, isVisible);
	}

    /**
     * Filters the list of projects based on specific criteria (e.g., location, flat type).
     *
     * @param criteria a string representing the filtering criteria.
     * @return a filtered list of projects.
     */
	public List<Project> filterProjects(String keyword) {
	    return ((com.SC2002.bto.service.impl.ProjectService) projectService).findByKeyword(keyword);
	}
	
    /**
     * Retrieves a list of all projects without filtering by visibility.
     * This method is specifically for HDBManagers who need to see all projects.
     *
     * @return a complete list of all projects regardless of visibility status.
     */
	public List<Project> getAllProjects() {
	    return projectService.getAllProjects();
	}
	
    /**
     * Retrieves a list of projects that are currently open for application.
     * Projects are filtered by their application period (current date must be 
     * between opening and closing dates) and visibility.
     *
     * @return a list of currently open and visible projects.
     */
	public List<Project> getCurrentlyOpenProjects() {
	    return ((com.SC2002.bto.service.impl.ProjectService) projectService).getCurrentlyOpenProjects();
	}
}
