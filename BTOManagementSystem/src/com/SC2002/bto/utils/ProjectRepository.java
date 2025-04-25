// File: src/com/SC2002/bto/utils/ProjectRepository.java
package com.SC2002.bto.utils;

import com.SC2002.bto.entities.Project;
import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {
    private static List<Project> projects = new ArrayList<>();

    /** Load all projects from CSV */
    public static void init(String csvPath) {
        projects = FileManager.loadProjectsFromCSV(csvPath);
        // Visibility is now loaded from CSV, no need to set it here
    }

    /** Load using default path */
    public static void init() {
        init(Constants.PROJECT_CSV);
    }

    /** Reload from default path */
    public static void refresh() {
        init(Constants.PROJECT_CSV);
    }

    /** Returns all projects, regardless of visibility */
    public static List<Project> getAll() {
        // Always refresh from file to ensure we have the latest data
        refresh();
        return projects;
    }

    /** Returns only visible projects */
    public static List<Project> getVisibleProjects() {
        // Always refresh from file to ensure we have the latest data
        refresh();
        List<Project> visible = new ArrayList<>();
        for (Project p : projects) {
            if (p.isVisible()) visible.add(p);
        }
        return visible;
    }

    /**
     * Toggle visibility flag for a project by ID
     * @return the new visibility state or null if not found
     */
    public static Boolean toggleVisibility(int projectId) {
        // Always refresh from file to ensure we have the latest data
        refresh();
        for (Project p : projects) {
            if (p.getProjectId() == projectId) {
                p.setVisible(!p.isVisible());
                // Save changes back to file
                FileManager.saveProjects(projects);
                return p.isVisible();
            }
        }
        return null;
    }
}
