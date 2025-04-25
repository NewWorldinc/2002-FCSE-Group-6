package com.SC2002.bto.repository.csv;

import com.SC2002.bto.entities.Project;
import com.SC2002.bto.repository.IProjectRepository;
import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CSV-based implementation of the project repository.
 * Follows the Single Responsibility Principle by focusing only on project data access.
 */
public class CSVProjectRepository implements IProjectRepository {
    
    private final String filePath;
    private List<Project> projects;
    
    /**
     * Constructs a CSVProjectRepository with the default file path.
     */
    public CSVProjectRepository() {
        this(Constants.PROJECT_CSV);
    }
    
    /**
     * Constructs a CSVProjectRepository with the specified file path.
     * 
     * @param filePath The path of the CSV file
     */
    public CSVProjectRepository(String filePath) {
        this.filePath = filePath;
        this.projects = FileManager.loadProjectsFromCSV(filePath);
    }
    
    /**
     * Refreshes the projects from the CSV file.
     */
    public void refresh() {
        this.projects = FileManager.loadProjectsFromCSV(filePath);
    }
    
    @Override
    public List<Project> findAll() {
        // Always refresh from file to ensure we have the latest data
        refresh();
        return new ArrayList<>(projects);
    }
    
    @Override
    public Optional<Project> findById(Integer id) {
        // Always refresh from file to ensure we have the latest data
        refresh();
        return projects.stream()
            .filter(p -> p.getProjectId() == id)
            .findFirst();
    }
    
    @Override
    public Project save(Project project) {
        // Check if project already exists
        Optional<Project> existingProject = findById(project.getProjectId());
        
        if (existingProject.isPresent()) {
            // Update existing project
            projects.remove(existingProject.get());
        }
        
        // Add new project
        projects.add(project);
        
        // Save all projects
        FileManager.saveProjects(projects);
        
        return project;
    }
    
    @Override
    public List<Project> saveAll(List<Project> entities) {
        // Replace all projects
        this.projects = new ArrayList<>(entities);
        
        // Save all projects
        FileManager.saveProjects(projects);
        
        return entities;
    }
    
    @Override
    public void delete(Project project) {
        projects.removeIf(p -> p.getProjectId() == project.getProjectId());
        FileManager.saveProjects(projects);
    }
    
    @Override
    public boolean existsById(Integer id) {
        // Always refresh from file to ensure we have the latest data
        refresh();
        return projects.stream().anyMatch(p -> p.getProjectId() == id);
    }
    
    @Override
    public List<Project> findAllVisible() {
        // Always refresh from file to ensure we have the latest data
        refresh();
        return projects.stream()
            .filter(Project::isVisible)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Project> findByNeighborhood(String neighborhood) {
        // Always refresh from file to ensure we have the latest data
        refresh();
        return projects.stream()
            .filter(p -> p.getNeighborhood().equalsIgnoreCase(neighborhood))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Project> findByFlatType(String flatType) {
        // Always refresh from file to ensure we have the latest data
        refresh();
        return projects.stream()
            .filter(p -> p.getType1Desc().equalsIgnoreCase(flatType) || 
                         p.getType2Desc().equalsIgnoreCase(flatType))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Project> findByManager(String managerNric) {
        // Always refresh from file to ensure we have the latest data
        refresh();
        return projects.stream()
            .filter(p -> p.getManager().equalsIgnoreCase(managerNric))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Project> findByOfficer(String officerNric) {
        // Always refresh from file to ensure we have the latest data
        refresh();
        return projects.stream()
            .filter(p -> p.getOfficers().stream()
                .anyMatch(o -> o.equalsIgnoreCase(officerNric)))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Project> findByApplicationPeriod(LocalDate date) {
        // Always refresh from file to ensure we have the latest data
        refresh();
        return projects.stream()
            .filter(p -> !date.isBefore(p.getApplicationOpeningDate()) && 
                         !date.isAfter(p.getApplicationClosingDate()))
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean updateVisibility(int projectId, boolean isVisible) {
        Optional<Project> projectOpt = findById(projectId);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            project.setVisible(isVisible);
            save(project);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean updateAvailableUnits(int projectId, String flatType, int units) {
        Optional<Project> projectOpt = findById(projectId);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            if ("2-Room".equalsIgnoreCase(flatType)) {
                project.setType1Units(units);
                save(project);
                return true;
            } else if ("3-Room".equalsIgnoreCase(flatType)) {
                project.setType2Units(units);
                save(project);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean bookUnit(int projectId, String flatType) {
        Optional<Project> projectOpt = findById(projectId);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            if ("2-Room".equalsIgnoreCase(flatType)) {
                if (project.getType1Units() <= 0) return false;
                project.setType1Units(project.getType1Units() - 1);
                save(project);
                return true;
            } else if ("3-Room".equalsIgnoreCase(flatType)) {
                if (project.getType2Units() <= 0) return false;
                project.setType2Units(project.getType2Units() - 1);
                save(project);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Finds projects that are currently open for application.
     * 
     * @return A list of projects that are currently open for application
     */
    public List<Project> findCurrentlyOpen() {
        // Always refresh from file to ensure we have the latest data
        refresh();
        LocalDate today = LocalDate.now();
        return projects.stream()
            .filter(Project::isVisible)
            .filter(p -> !today.isBefore(p.getApplicationOpeningDate()) && 
                         !today.isAfter(p.getApplicationClosingDate()))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds projects by keyword in name or neighborhood.
     * 
     * @param keyword The keyword to search for
     * @return A list of projects matching the keyword
     */
    public List<Project> findByKeyword(String keyword) {
        // Always refresh from file to ensure we have the latest data
        refresh();
        return projects.stream()
            .filter(p -> p.getProjectName().toLowerCase().contains(keyword.toLowerCase()) || 
                         p.getNeighborhood().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
    }
}
