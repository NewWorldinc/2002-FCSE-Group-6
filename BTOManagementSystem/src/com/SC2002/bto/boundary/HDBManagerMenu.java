package com.SC2002.bto.boundary;

import com.SC2002.bto.entities.HDBManager;
import com.SC2002.bto.entities.HDBOfficer;
import com.SC2002.bto.entities.Project;
import com.SC2002.bto.entities.User;
import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.ApplicationStatus;
import com.SC2002.bto.entities.Enquiry;
import com.SC2002.bto.control.ProjectController;
import com.SC2002.bto.control.ReportController;
import com.SC2002.bto.utils.Constants;
import com.SC2002.bto.utils.FileManager;
import com.SC2002.bto.utils.InputValidator;
import com.SC2002.bto.utils.ProjectRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class HDBManagerMenu {
    private Scanner scanner;
    private HDBManager manager;
    private ProjectController projectController;
    private static final String CSV_PATH = Constants.PROJECT_CSV;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d/M/yy");

    /**
     * Helper method to check if an officer is available during a specified time period.
     * This checks both projects the officer is assigned to as an officer and
     * projects the officer has applied for as an applicant.
     * 
     * @param officerName The name of the officer to check
     * @param projectId The ID of the project to exclude from the check (use -1 for new projects)
     * @param openingDate The opening date to check
     * @param closingDate The closing date to check
     * @return true if the officer is available, false if there's a conflict
     */
    private boolean isOfficerAvailable(String officerName, int projectId, LocalDate openingDate, LocalDate closingDate) {
        // Get all projects
        List<Project> allProjects = projectController.getAllProjects();
        
        // Find the officer's NRIC
        String officerNric = null;
        HDBOfficer officerObj = null;
        List<User> officers = FileManager.loadUsersFromCSV(Constants.OFFICER_CSV, "HDBOfficer");
        for (User user : officers) {
            if (user instanceof HDBOfficer && user.getName().equalsIgnoreCase(officerName)) {
                officerNric = user.getNric();
                officerObj = (HDBOfficer) user;
                break;
            }
        }
        
        if (officerNric == null) {
            // Officer not found in the system
            return false;
        }
        
        // Find all projects the officer is assigned to (excluding the current project)
        List<Project> officerProjects = allProjects.stream()
            .filter(p -> p.getProjectId() != projectId && p.isOfficerAssigned(officerName))
            .collect(Collectors.toList());
            
        // Check for date overlaps with assigned projects
        for (Project p : officerProjects) {
            LocalDate existingOpen = p.getApplicationOpeningDate();
            LocalDate existingClose = p.getApplicationClosingDate();
            
            // Check for overlap: 
            // If the new project's closing date is after the existing project's opening date
            // AND the new project's opening date is before the existing project's closing date
            if (!(closingDate.isBefore(existingOpen) || openingDate.isAfter(existingClose))) {
                return false;
            }
        }
        
        // Check if the officer has applied for any projects as an applicant
        List<User> applicants = FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant");
        for (User user : applicants) {
            if (user instanceof Applicant && user.getNric().equalsIgnoreCase(officerNric)) {
                Applicant applicant = (Applicant) user;
                int appliedProjectId = applicant.getAppliedProjectId();
                
                // Skip if the officer hasn't applied for any project or if the application was unsuccessful
                if (appliedProjectId == -1 || 
                    applicant.getApplicationStatus() == ApplicationStatus.NOT_APPLIED || 
                    applicant.getApplicationStatus() == ApplicationStatus.UNSUCCESSFUL) {
                    continue;
                }
                
                // Find the project the officer has applied for
                Project appliedProject = allProjects.stream()
                    .filter(p -> p.getProjectId() == appliedProjectId)
                    .findFirst()
                    .orElse(null);
                    
                if (appliedProject != null) {
                    LocalDate appliedOpen = appliedProject.getApplicationOpeningDate();
                    LocalDate appliedClose = appliedProject.getApplicationClosingDate();
                    
                    // Check for overlap with the project the officer has applied for
                    if (!(closingDate.isBefore(appliedOpen) || openingDate.isAfter(appliedClose))) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }

    public HDBManagerMenu(Scanner scanner, HDBManager manager) {
        this.scanner = scanner;
        this.manager = manager;
        this.projectController = new ProjectController();
        ProjectRepository.init(CSV_PATH);
    }

    public void displayMenu() {
        while (true) {
            System.out.println("\n--- HDB Manager Menu ---");
            System.out.println("1. Create a New Project Listing");
            System.out.println("2. Edit an Existing Project");
            System.out.println("3. Delete a Project");
            System.out.println("4. Toggle Project Visibility");
            System.out.println("5. View Projects");
            System.out.println("6. Approve/Reject HDB Officer Registrations");
            System.out.println("7. Approve/Reject Applicant Applications");
            System.out.println("8. Process Withdrawal Requests");
            System.out.println("9. Generate Reports");
            System.out.println("10. View All Enquiries");
            System.out.println("11. Change Password");
            System.out.println("12. Logout");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1": createNewProjectListing(); break;
                case "2": editExistingProject(); break;
                case "3": deleteProject(); break;
                case "4": toggleProjectVisibility(); break;
                case "5": viewProjects(); break;
                case "6": processOfficerRegistrations(); break;
                case "7": processApplicantApplications(); break;
                case "8": processWithdrawalRequests(); break;
                case "9": generateReports(); break;
                case "10": viewAllEnquiries(); break;
                case "11": if (changePassword()) return; break;
                case "12": System.out.println("Logging out..."); return;
                default: System.out.println("Invalid option, please try again.");
            }
        }
    }

    /**
     * Checks if a project's date range overlaps with any existing projects managed by the current manager.
     * 
     * @param projectId The ID of the project to exclude from the check (use -1 for new projects)
     * @param openingDate The opening date to check
     * @param closingDate The closing date to check
     * @return true if there's an overlap, false otherwise
     */
    private boolean hasDateOverlap(int projectId, LocalDate openingDate, LocalDate closingDate) {
        List<Project> managerProjects = projectController.getAllProjects().stream()
            .filter(p -> p.getManager().equalsIgnoreCase(manager.getName()) && p.getProjectId() != projectId)
            .collect(Collectors.toList());
            
        for (Project existingProject : managerProjects) {
            LocalDate existingOpen = existingProject.getApplicationOpeningDate();
            LocalDate existingClose = existingProject.getApplicationClosingDate();
            
            // Check for overlap: 
            // If the new project's closing date is after the existing project's opening date
            // AND the new project's opening date is before the existing project's closing date
            if (!(closingDate.isBefore(existingOpen) || openingDate.isAfter(existingClose))) {
                System.out.println("\nDate overlap detected with existing project:");
                System.out.printf("Project ID: %d | Name: %s\n", 
                    existingProject.getProjectId(), existingProject.getProjectName());
                System.out.printf("Date Range: %s to %s\n", 
                    existingOpen.format(DATE_FMT), existingClose.format(DATE_FMT));
                return true;
            }
        }
        
        return false;
    }
    
    private void createNewProjectListing() {
        System.out.println("\n--- Create New Project Listing ---");
        try {
            List<Project> projects = projectController.getAllProjects();
            int newId = projects.size() + 1;

            String projName = promptNonEmpty("Enter Project Name: ");
            String neighbourhood = promptNonEmpty("Enter Neighbourhood: ");
            String t1d = promptNonEmpty("Enter Type1 Description (e.g., '2-Room'): ");
            int t1u = promptInt("Enter Number of Units for Type1 (int): ");
            int t1p = promptInt("Enter Selling Price for Type1 (int): ");
            String t2d = promptNonEmpty("Enter Type2 Description (e.g., '3-Room'): ");
            int t2u = promptInt("Enter Number of Units for Type2 (int): ");
            int t2p = promptInt("Enter Selling Price for Type2 (int): ");
            
            LocalDate open, close;
            boolean dateOverlap = false;
            do {
                open = promptDate("Enter Application Opening Date (d/M/yy): ");
                close = promptDate("Enter Application Closing Date (d/M/yy): ");
                
                // Validate that closing date is after opening date
                if (close.isBefore(open) || close.isEqual(open)) {
                    System.out.println("Error: Closing date must be after opening date.");
                    continue;
                }
                
                // Check for date overlap with existing projects managed by this manager
                dateOverlap = hasDateOverlap(-1, open, close);
                if (dateOverlap) {
                    System.out.println("Error: You already have a project during this time period.");
                    System.out.println("Managers can only handle one project at a time.");
                    System.out.println("Please choose different dates or try again later.");
                }
            } while (close.isBefore(open) || close.isEqual(open) || dateOverlap);
            
            int slots = promptInt("Enter Officer Slots (int): ");
            String officerInput = promptNonEmpty("Enter Officer Name(s), comma-separated: ");
            List<String> officerNames = Arrays.stream(officerInput.split(","))
                                          .map(String::trim)
                                          .filter(s -> !s.isEmpty())
                                          .toList();
            
            // Check if each officer is available during the project's time period
            List<String> availableOfficers = new ArrayList<>();
            List<String> unavailableOfficers = new ArrayList<>();
            
            for (String officerName : officerNames) {
                if (isOfficerAvailable(officerName, -1, open, close)) {
                    availableOfficers.add(officerName);
                } else {
                    unavailableOfficers.add(officerName);
                }
            }
            
            // Notify about unavailable officers
            if (!unavailableOfficers.isEmpty()) {
                System.out.println("\nThe following officers are not available during the specified time period:");
                for (String officer : unavailableOfficers) {
                    System.out.println("- " + officer);
                }
                System.out.println("These officers will not be added to the project.");
            }

            Project newProj = new Project(
                newId, projName, neighbourhood,
                t1d, t1u, t1p,
                t2d, t2u, t2p,
                open, close,
                manager.getName(), slots, availableOfficers
            );
            projects.add(newProj);
            FileManager.saveProjects(projects);
            ProjectRepository.init(CSV_PATH);
            System.out.println("New project listing created.");
        } catch (Exception e) {
            System.out.println("Error creating project: " + e.getMessage());
        }
    }

    private void editExistingProject() {
        System.out.println("\n--- Edit Existing Project ---");
        List<Project> projects = projectController.getAllProjects();
        if (projects.isEmpty()) {
            System.out.println("No projects available to edit.");
            return;
        }

        for (Project p : projects) {
            System.out.printf("ID: %d | Name: %s | Neighbourhood: %s%n", p.getProjectId(), p.getProjectName(), p.getNeighborhood());
        }

        int id = promptInt("Enter the Project ID to edit: ");
        Project projectToEdit = projects.stream()
            .filter(p -> p.getProjectId() == id)
            .findFirst()
            .orElse(null);

        if (projectToEdit == null) {
            System.out.println("Project ID not found.");
            return;
        }

        System.out.println("Leave input blank to retain existing value.");

        String name = promptOptional("New Project Name (Current: " + projectToEdit.getProjectName() + "): ");
        if (!name.isEmpty()) projectToEdit.setProjectName(name);

        String neighbourhood = promptOptional("New Neighbourhood (Current: " + projectToEdit.getNeighborhood() + "): ");
        if (!neighbourhood.isEmpty()) projectToEdit.setNeighborhood(neighbourhood);

        String type1 = promptOptional("New Type1 Description (Current: " + projectToEdit.getType1Desc() + "): ");
        if (!type1.isEmpty()) projectToEdit.setType1Desc(type1);

        String type2 = promptOptional("New Type2 Description (Current: " + projectToEdit.getType2Desc() + "): ");
        if (!type2.isEmpty()) projectToEdit.setType2Desc(type2);

        String input;
        input = promptOptional("New Type1 Units (Current: " + projectToEdit.getType1Units() + "): ");
        if (!input.isEmpty()) projectToEdit.setType1Units(Integer.parseInt(input));

        input = promptOptional("New Type1 Price (Current: " + projectToEdit.getType1Price() + "): ");
        if (!input.isEmpty()) projectToEdit.setType1Price(Integer.parseInt(input));

        input = promptOptional("New Type2 Units (Current: " + projectToEdit.getType2Units() + "): ");
        if (!input.isEmpty()) projectToEdit.setType2Units(Integer.parseInt(input));

        input = promptOptional("New Type2 Price (Current: " + projectToEdit.getType2Price() + "): ");
        if (!input.isEmpty()) projectToEdit.setType2Price(Integer.parseInt(input));

        // Handle date changes with overlap checking
        String openDateInput = promptOptional("New Opening Date (d/M/yy, Current: " + projectToEdit.getApplicationOpeningDate().format(DATE_FMT) + "): ");
        String closeDateInput = promptOptional("New Closing Date (d/M/yy, Current: " + projectToEdit.getApplicationClosingDate().format(DATE_FMT) + "): ");
        
        if (!openDateInput.isEmpty() || !closeDateInput.isEmpty()) {
            LocalDate newOpenDate = openDateInput.isEmpty() ? 
                projectToEdit.getApplicationOpeningDate() : 
                LocalDate.parse(openDateInput, DATE_FMT);
                
            LocalDate newCloseDate = closeDateInput.isEmpty() ? 
                projectToEdit.getApplicationClosingDate() : 
                LocalDate.parse(closeDateInput, DATE_FMT);
            
            // Validate that closing date is after opening date
            if (newCloseDate.isBefore(newOpenDate) || newCloseDate.isEqual(newOpenDate)) {
                System.out.println("Error: Closing date must be after opening date. Date changes not applied.");
            } else {
                // Check for date overlap with other projects managed by this manager
                boolean dateOverlap = hasDateOverlap(projectToEdit.getProjectId(), newOpenDate, newCloseDate);
                
                if (dateOverlap) {
                    System.out.println("Error: These dates overlap with another project you are managing.");
                    System.out.println("Managers can only handle one project at a time. Date changes not applied.");
                } else {
                    // Apply the date changes if there's no overlap
                    projectToEdit.setApplicationOpeningDate(newOpenDate);
                    projectToEdit.setApplicationClosingDate(newCloseDate);
                    System.out.println("Project dates updated successfully.");
                }
            }
        }

        input = promptOptional("New Officer Slots (Current: " + projectToEdit.getOfficerSlots() + "): ");
        if (!input.isEmpty()) projectToEdit.setOfficerSlots(Integer.parseInt(input));

        String officerInput = promptOptional("New Officer Name(s), comma-separated (Current: " + String.join(", ", projectToEdit.getOfficers()) + "): ");
        if (!officerInput.isEmpty()) {
            List<String> officerNames = Arrays.stream(officerInput.split(","))
                                             .map(String::trim)
                                             .filter(s -> !s.isEmpty())
                                             .toList();
            
            // Keep existing officers
            List<String> existingOfficers = new ArrayList<>(projectToEdit.getOfficers());
            
            // Check if each new officer is available during the project's time period
            List<String> availableOfficers = new ArrayList<>();
            List<String> unavailableOfficers = new ArrayList<>();
            
            for (String officerName : officerNames) {
                // Skip officers that are already assigned to this project
                if (existingOfficers.contains(officerName)) {
                    availableOfficers.add(officerName);
                    continue;
                }
                
                if (isOfficerAvailable(officerName, projectToEdit.getProjectId(), 
                                      projectToEdit.getApplicationOpeningDate(), 
                                      projectToEdit.getApplicationClosingDate())) {
                    availableOfficers.add(officerName);
                } else {
                    unavailableOfficers.add(officerName);
                }
            }
            
            // Notify about unavailable officers
            if (!unavailableOfficers.isEmpty()) {
                System.out.println("\nThe following officers are not available during the specified time period:");
                for (String officer : unavailableOfficers) {
                    System.out.println("- " + officer);
                }
                System.out.println("These officers will not be added to the project.");
            }
            
            projectToEdit.setOfficers(availableOfficers);
        }

        FileManager.saveProjects(projects);
        ProjectRepository.init(CSV_PATH);
        System.out.println("Project updated successfully.");
    }

    private String promptNonEmpty(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("This field cannot be empty.");
            }
        } while (input.isEmpty());
        return input;
    }

    private String promptOptional(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int promptInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private LocalDate promptDate(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return LocalDate.parse(scanner.nextLine().trim(), DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use d/M/yy.");
            }
        }
    }

    private void deleteProject() {
        System.out.println("\n--- Delete Project ---");
        List<Project> projects = projectController.getAllProjects();
        if (projects.isEmpty()) {
            System.out.println("No projects available to delete.");
            return;
        }

        for (Project p : projects) {
            System.out.printf("ID: %d | Name: %s | Neighbourhood: %s%n", p.getProjectId(), p.getProjectName(), p.getNeighborhood());
        }

        int idToDelete = promptInt("Enter the Project ID to delete: ");
        Project projectToDelete = null;
        for (Project p : projects) {
            if (p.getProjectId() == idToDelete) {
                projectToDelete = p;
                break;
            }
        }

        if (projectToDelete == null) {
            System.out.println("Project ID not found.");
            return;
        }

        projects.remove(projectToDelete);

        // Reassign project IDs to keep them in sequence starting from 1
        for (int i = 0; i < projects.size(); i++) {
            Project p = projects.get(i);
            try {
                java.lang.reflect.Field idField = Project.class.getDeclaredField("projectId");
                idField.setAccessible(true);
                idField.setInt(p, i + 1);
            } catch (Exception e) {
                System.out.println("Failed to reset project IDs: " + e.getMessage());
                return;
            }
        }

        boolean saved = FileManager.saveProjects(projects);
        if (saved) {
            ProjectRepository.init(CSV_PATH);
            
            // Force a refresh of the project controller's data
            this.projectController = new ProjectController();
            System.out.println("Project deleted and IDs updated successfully.");
        } else {
            System.out.println("Error: Failed to save projects after deletion.");
        }
    }

    private void toggleProjectVisibility() {
        System.out.println("\n--- Toggle Project Visibility ---");
        List<Project> projects = projectController.getAllProjects();

        List<Project> managedProjects = new ArrayList<>();
        for (Project p : projects) {
            if (p.getManager().equalsIgnoreCase(manager.getName())) {
                managedProjects.add(p);
            }
        }

        if (managedProjects.isEmpty()) {
            System.out.println("You have no projects to manage.");
            return;
        }

        System.out.println("Your Managed Projects:");
        for (Project p : managedProjects) {
            String status = p.isVisible() ? "Visible to Applicants" : "Hidden from Applicants";
            System.out.printf("ID: %d | Name: %-20s | Status: %s%n",
                    p.getProjectId(), p.getProjectName(), status);
        }

        int idToToggle = promptInt("Enter the Project ID to toggle visibility: ");
        Project projectToToggle = null;
        for (Project p : managedProjects) {
            if (p.getProjectId() == idToToggle) {
                projectToToggle = p;
                break;
            }
        }

        if (projectToToggle == null) {
            System.out.println("Project ID not found among your managed projects.");
            return;
        }

        projectToToggle.setVisible(!projectToToggle.isVisible());
        FileManager.saveProjects(projects);
        ProjectRepository.init(CSV_PATH);
        System.out.println("Project visibility is now set to: " + (projectToToggle.isVisible() ? "Visible to Applicants" : "Hidden from Applicants"));
    }

    private void processOfficerRegistrations() {
        System.out.println("\n--- Approve/Reject Officer Registrations ---");
        String path = Constants.REGISTRATION_CSV;
        List<String> linesToRetain = new ArrayList<>();
        List<Project> projects = projectController.getAllProjects();
        boolean updated = false;

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String header = br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 2) continue;

                String nric = tokens[0].trim();
                int projectId = Integer.parseInt(tokens[1].trim());

                Project project = projects.stream()
                        .filter(p -> p.getProjectId() == projectId && p.getManager().equalsIgnoreCase(manager.getName()))
                        .findFirst().orElse(null);

                if (project == null) {
                    System.out.println("Skipped: Project ID " + projectId + " not found or not under your management.");
                    linesToRetain.add(line);
                    continue;
                }

                System.out.printf("Officer NRIC: %s | Project ID: %d (%s)%n", nric, projectId, project.getProjectName());
                System.out.print("Approve (A) / Reject (R) / Skip (S): ");
                String choice = scanner.nextLine().trim();

                if ("A".equalsIgnoreCase(choice)) {
                    if (project.getOfficerSlots() <= 0) {
                        System.out.println("No officer slots remaining for this project.");
                        linesToRetain.add(line);
                        continue;
                    }
                    
                    // Get officer name from NRIC
                    String officerName = getOfficerNameFromNRIC(nric);
                    
                    if (officerName != null && !project.getOfficers().contains(officerName)) {
                        // Create a new mutable list from the existing officers list
                        List<String> updatedOfficers = new ArrayList<>(project.getOfficers());
                        // Add the new officer to the mutable list
                        updatedOfficers.add(officerName);
                        // Set the new list back to the project
                        project.setOfficers(updatedOfficers);
                        
                        // Update officer slots
                        project.setOfficerSlots(project.getOfficerSlots() - 1);
                        
                        // Update the HDBOfficer entity's registeredProjectIds list
                        List<User> officers = FileManager.loadUsersFromCSV(Constants.OFFICER_CSV, "HDBOfficer");
                        for (User user : officers) {
                            if (user instanceof HDBOfficer && user.getNric().equalsIgnoreCase(nric)) {
                                HDBOfficer officer = (HDBOfficer) user;
                                officer.addRegisteredProjectId(project.getProjectId());
                                break;
                            }
                        }
                        
                        updated = true;
                        System.out.println("Approved and officer added to project.");
                    } else if (officerName == null) {
                        System.out.println("Officer NRIC not found in the system.");
                        linesToRetain.add(line);
                    } else {
                        System.out.println("Officer already registered for this project.");
                    }
                } else if ("R".equalsIgnoreCase(choice)) {
                    System.out.println("Registration rejected.");
                } else {
                    System.out.println("Skipped.");
                    linesToRetain.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading OfficerRegistration.csv: " + e.getMessage());
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write("OfficerNRIC,ProjectID\n");
            for (String l : linesToRetain) {
                bw.write(l); bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing OfficerRegistration.csv: " + e.getMessage());
        }

        if (updated) {
            FileManager.saveProjects(projects);
            ProjectRepository.init(CSV_PATH);
        }
    }
    
    // Helper method to get officer name from NRIC
    private String getOfficerNameFromNRIC(String nric) {
        List<User> officers = FileManager.loadUsersFromCSV(Constants.OFFICER_CSV, "HDBOfficer");
        for (User user : officers) {
            if (user.getNric().equalsIgnoreCase(nric)) {
                return user.getName();
            }
        }
        return null;
    }
    
    private void processApplicantApplications() {
        System.out.println("\n--- Approve/Reject Applicant Applications ---");

        List<User> users = FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant");
        List<Project> managedProjects = projectController.getAllProjects().stream()
            .filter(p -> p.getManager().equalsIgnoreCase(manager.getName()))
            .collect(Collectors.toList());

        List<Applicant> applicants = users.stream()
            .filter(u -> u instanceof Applicant)
            .map(u -> (Applicant) u)
            .filter(a -> {
                int pid = a.getAppliedProjectId();
                return pid >= 0 && a.getApplicationStatus() != ApplicationStatus.SUCCESSFUL &&
                       managedProjects.stream().anyMatch(p -> p.getProjectId() == pid);
            }).collect(Collectors.toList());

        if (applicants.isEmpty()) {
            System.out.println("No applicants to process.");
            return;
        }

        for (Applicant a : applicants) {
            System.out.printf("Applicant: %s (%s)\n", a.getName(), a.getNric());
            System.out.printf("  Age: %d | Marital: %s | Applied Project ID: %d | Flat Type: %s\n",
                a.getAge(), a.getMaritalStatus(), a.getAppliedProjectId(), a.getAppliedFlatType());
            System.out.printf("  Current Status: %s\n", a.getApplicationStatus());
            System.out.print("Approve (A) / Reject (R) / Skip (S): ");
            String choice = scanner.nextLine().trim();

            if ("A".equalsIgnoreCase(choice)) {
                a.setApplicationStatus(ApplicationStatus.SUCCESSFUL);
                FileManager.updateApplicantApplication(a);
                System.out.println("Application marked as SUCCESSFUL.");
            } else if ("R".equalsIgnoreCase(choice)) {
                a.setAppliedProjectId(-1);
                a.setAppliedFlatType("");
                a.setApplicationStatus(ApplicationStatus.UNSUCCESSFUL);
                FileManager.updateApplicantApplication(a);
                System.out.println("Application marked as UNSUCCESSFUL.");
            } else {
                System.out.println("Skipped.");
            }
            System.out.println("------------------------------------------------");
        }
    }

    private void generateReports() {
        System.out.println("\n--- Report Generation ---");
        System.out.println("1. All Projects Summary");
        System.out.println("2. Filter by Project");
        System.out.println("3. Filter by Marital Status");
        System.out.println("4. Filter by Age Group");
        System.out.println("5. Filter by Flat Type");
        System.out.println("6. Filter by Application Status");
        System.out.println("7. Back to Main Menu");
        System.out.print("Enter your choice: ");
        
        String choice = scanner.nextLine().trim();
        
        List<Project> managerProjects = projectController.getAllProjects().stream()
            .filter(p -> p.getManager().equalsIgnoreCase(manager.getName()))
            .collect(Collectors.toList());
            
        if (managerProjects.isEmpty()) {
            System.out.println("You are not managing any projects.");
            return;
        }
        
        List<User> users = FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant");
        List<Applicant> allApplicants = users.stream()
            .filter(u -> u instanceof Applicant)
            .map(u -> (Applicant) u)
            .collect(Collectors.toList());
            
        // Filter applicants to only those who applied to manager's projects
        List<Applicant> relevantApplicants = allApplicants.stream()
            .filter(a -> a.getAppliedProjectId() > 0 && 
                   managerProjects.stream().anyMatch(p -> p.getProjectId() == a.getAppliedProjectId()))
            .collect(Collectors.toList());
            
        if (relevantApplicants.isEmpty()) {
            System.out.println("No applicants found for your projects.");
            return;
        }
        
        switch (choice) {
            case "1":
                generateAllProjectsSummary(managerProjects, relevantApplicants);
                break;
            case "2":
                generateProjectFilteredReport(managerProjects, relevantApplicants);
                break;
            case "3":
                generateMaritalStatusFilteredReport(managerProjects, relevantApplicants);
                break;
            case "4":
                generateAgeGroupFilteredReport(managerProjects, relevantApplicants);
                break;
            case "5":
                generateFlatTypeFilteredReport(managerProjects, relevantApplicants);
                break;
            case "6":
                generateStatusFilteredReport(managerProjects, relevantApplicants);
                break;
            case "7":
                return;
            default:
                System.out.println("Invalid option. Returning to main menu.");
                return;
        }
    }
    
    private void generateAllProjectsSummary(List<Project> projects, List<Applicant> applicants) {
        System.out.println("\n--- All Projects Summary ---");
        
        for (Project p : projects) {
            System.out.printf("\nProject ID: %d | %s\n", p.getProjectId(), p.getProjectName());
            System.out.println("Applicants:");

            List<Applicant> projectApplicants = applicants.stream()
                .filter(a -> a.getAppliedProjectId() == p.getProjectId())
                .collect(Collectors.toList());

            if (projectApplicants.isEmpty()) {
                System.out.println("  No applicants for this project.");
            } else {
                for (Applicant a : projectApplicants) {
                    System.out.printf("  - %s (%s) | Age: %d | Marital: %s | Flat: %s | Status: %s\n",
                        a.getName(), a.getNric(), a.getAge(), a.getMaritalStatus(), 
                        a.getAppliedFlatType(), a.getApplicationStatus());
                }
            }

            long count2Room = projectApplicants.stream()
                .filter(a -> "2-Room".equalsIgnoreCase(a.getAppliedFlatType()))
                .count();

            long count3Room = projectApplicants.stream()
                .filter(a -> "3-Room".equalsIgnoreCase(a.getAppliedFlatType()))
                .count();

            long countPending = projectApplicants.stream()
                .filter(a -> a.getApplicationStatus() == ApplicationStatus.PENDING)
                .count();

            long countSuccessful = projectApplicants.stream()
                .filter(a -> a.getApplicationStatus() == ApplicationStatus.SUCCESSFUL)
                .count();
                
            long countBooked = projectApplicants.stream()
                .filter(a -> a.getApplicationStatus() == ApplicationStatus.BOOKED)
                .count();

            long countUnsuccessful = projectApplicants.stream()
                .filter(a -> a.getApplicationStatus() == ApplicationStatus.UNSUCCESSFUL)
                .count();

            System.out.println("Flat Application Summary:");
            System.out.println("  2-Room Applications: " + count2Room);
            System.out.println("  3-Room Applications: " + count3Room);
            System.out.println("Application Status Summary:");
            System.out.println("  PENDING     : " + countPending);
            System.out.println("  SUCCESSFUL  : " + countSuccessful);
            System.out.println("  BOOKED      : " + countBooked);
            System.out.println("  UNSUCCESSFUL: " + countUnsuccessful);
        }
        
        // Display all successful applications at the end of the report
        displaySuccessfulApplications(projects, applicants);
    }
    
    private void generateProjectFilteredReport(List<Project> projects, List<Applicant> applicants) {
        System.out.println("\n--- Filter by Project ---");
        
        for (Project p : projects) {
            System.out.printf("%d: %s @ %s\n", p.getProjectId(), p.getProjectName(), p.getNeighborhood());
        }
        
        System.out.print("Enter Project ID to filter by: ");
        int projectId;
        try {
            projectId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }
        
        Project selectedProject = projects.stream()
            .filter(p -> p.getProjectId() == projectId)
            .findFirst().orElse(null);
            
        if (selectedProject == null) {
            System.out.println("Project not found.");
            return;
        }
        
        List<Applicant> filteredApplicants = applicants.stream()
            .filter(a -> a.getAppliedProjectId() == projectId)
            .collect(Collectors.toList());
            
        if (filteredApplicants.isEmpty()) {
            System.out.println("No applicants found for this project.");
            return;
        }
        
        System.out.printf("\nProject: %s @ %s\n", selectedProject.getProjectName(), selectedProject.getNeighborhood());
        System.out.println("Applicants:");
        
        for (Applicant a : filteredApplicants) {
            System.out.printf("  - %s (%s) | Age: %d | Marital: %s | Flat: %s | Status: %s\n",
                a.getName(), a.getNric(), a.getAge(), a.getMaritalStatus(), 
                a.getAppliedFlatType(), a.getApplicationStatus());
        }
        
        printSummaryStatistics(filteredApplicants);
        
        // Display all successful applications at the end of the report
        displaySuccessfulApplications(projects, applicants);
    }
    
    private void generateMaritalStatusFilteredReport(List<Project> projects, List<Applicant> applicants) {
        System.out.println("\n--- Filter by Marital Status ---");
        System.out.println("1. Single");
        System.out.println("2. Married");
        System.out.print("Enter your choice: ");
        
        String choice = scanner.nextLine().trim();
        String maritalStatus;
        
        if ("1".equals(choice)) {
            maritalStatus = "Single";
        } else if ("2".equals(choice)) {
            maritalStatus = "Married";
        } else {
            System.out.println("Invalid choice.");
            return;
        }
        
        List<Applicant> filteredApplicants = applicants.stream()
            .filter(a -> maritalStatus.equalsIgnoreCase(a.getMaritalStatus()))
            .collect(Collectors.toList());
            
        if (filteredApplicants.isEmpty()) {
            System.out.println("No " + maritalStatus.toLowerCase() + " applicants found.");
            return;
        }
        
        System.out.println("\nApplicants with Marital Status: " + maritalStatus);
        
        for (Applicant a : filteredApplicants) {
            Project p = projects.stream()
                .filter(proj -> proj.getProjectId() == a.getAppliedProjectId())
                .findFirst().orElse(null);
                
            String projectName = p != null ? p.getProjectName() : "Unknown";
            
            System.out.printf("  - %s (%s) | Age: %d | Project: %s | Flat: %s | Status: %s\n",
                a.getName(), a.getNric(), a.getAge(), projectName, 
                a.getAppliedFlatType(), a.getApplicationStatus());
        }
        
        printSummaryStatistics(filteredApplicants);
        
        // Display all successful applications at the end of the report
        displaySuccessfulApplications(projects, applicants);
    }
    
    private void generateAgeGroupFilteredReport(List<Project> projects, List<Applicant> applicants) {
        System.out.println("\n--- Filter by Age Group ---");
        System.out.println("1. Below 30");
        System.out.println("2. 30-40");
        System.out.println("3. 41-50");
        System.out.println("4. Above 50");
        System.out.print("Enter your choice: ");
        
        String choice = scanner.nextLine().trim();
        int minAge = 0, maxAge = 0;
        String ageGroupDesc = "";
        
        switch (choice) {
            case "1":
                maxAge = 29;
                ageGroupDesc = "Below 30";
                break;
            case "2":
                minAge = 30;
                maxAge = 40;
                ageGroupDesc = "30-40";
                break;
            case "3":
                minAge = 41;
                maxAge = 50;
                ageGroupDesc = "41-50";
                break;
            case "4":
                minAge = 51;
                maxAge = Integer.MAX_VALUE;
                ageGroupDesc = "Above 50";
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }
        
        final int finalMinAge = minAge;
        final int finalMaxAge = maxAge;
        
        List<Applicant> filteredApplicants = applicants.stream()
            .filter(a -> a.getAge() >= finalMinAge && a.getAge() <= finalMaxAge)
            .collect(Collectors.toList());
            
        if (filteredApplicants.isEmpty()) {
            System.out.println("No applicants found in age group: " + ageGroupDesc);
            return;
        }
        
        System.out.println("\nApplicants in Age Group: " + ageGroupDesc);
        
        for (Applicant a : filteredApplicants) {
            Project p = projects.stream()
                .filter(proj -> proj.getProjectId() == a.getAppliedProjectId())
                .findFirst().orElse(null);
                
            String projectName = p != null ? p.getProjectName() : "Unknown";
            
            System.out.printf("  - %s (%s) | Age: %d | Marital: %s | Project: %s | Flat: %s | Status: %s\n",
                a.getName(), a.getNric(), a.getAge(), a.getMaritalStatus(), projectName, 
                a.getAppliedFlatType(), a.getApplicationStatus());
        }
        
        printSummaryStatistics(filteredApplicants);
        
        // Display all successful applications at the end of the report
        displaySuccessfulApplications(projects, applicants);
    }
    
    private void generateFlatTypeFilteredReport(List<Project> projects, List<Applicant> applicants) {
        System.out.println("\n--- Filter by Flat Type ---");
        System.out.println("1. 2-Room");
        System.out.println("2. 3-Room");
        System.out.print("Enter your choice: ");
        
        String choice = scanner.nextLine().trim();
        String flatType;
        
        if ("1".equals(choice)) {
            flatType = "2-Room";
        } else if ("2".equals(choice)) {
            flatType = "3-Room";
        } else {
            System.out.println("Invalid choice.");
            return;
        }
        
        List<Applicant> filteredApplicants = applicants.stream()
            .filter(a -> flatType.equalsIgnoreCase(a.getAppliedFlatType()))
            .collect(Collectors.toList());
            
        if (filteredApplicants.isEmpty()) {
            System.out.println("No applicants found for flat type: " + flatType);
            return;
        }
        
        System.out.println("\nApplicants for Flat Type: " + flatType);
        
        for (Applicant a : filteredApplicants) {
            Project p = projects.stream()
                .filter(proj -> proj.getProjectId() == a.getAppliedProjectId())
                .findFirst().orElse(null);
                
            String projectName = p != null ? p.getProjectName() : "Unknown";
            
            System.out.printf("  - %s (%s) | Age: %d | Marital: %s | Project: %s | Status: %s\n",
                a.getName(), a.getNric(), a.getAge(), a.getMaritalStatus(), projectName, 
                a.getApplicationStatus());
        }
        
        // Additional statistics for flat type report
        long singleCount = filteredApplicants.stream()
            .filter(a -> "Single".equalsIgnoreCase(a.getMaritalStatus()))
            .count();
            
        long marriedCount = filteredApplicants.stream()
            .filter(a -> "Married".equalsIgnoreCase(a.getMaritalStatus()))
            .count();
            
        System.out.println("\nMarital Status Distribution:");
        System.out.println("  Single  : " + singleCount);
        System.out.println("  Married : " + marriedCount);
        
        printSummaryStatistics(filteredApplicants);
        
        // Display all successful applications at the end of the report
        displaySuccessfulApplications(projects, applicants);
    }
    
    private void generateStatusFilteredReport(List<Project> projects, List<Applicant> applicants) {
        System.out.println("\n--- Filter by Application Status ---");
        System.out.println("1. Pending");
        System.out.println("2. Successful");
        System.out.println("3. Booked");
        System.out.println("4. Unsuccessful");
        System.out.print("Enter your choice: ");
        
        String choice = scanner.nextLine().trim();
        ApplicationStatus status;
        
        switch (choice) {
            case "1":
                status = ApplicationStatus.PENDING;
                break;
            case "2":
                status = ApplicationStatus.SUCCESSFUL;
                break;
            case "3":
                status = ApplicationStatus.BOOKED;
                break;
            case "4":
                status = ApplicationStatus.UNSUCCESSFUL;
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }
        
        List<Applicant> filteredApplicants = applicants.stream()
            .filter(a -> a.getApplicationStatus() == status)
            .collect(Collectors.toList());
            
        if (filteredApplicants.isEmpty()) {
            System.out.println("No applicants found with status: " + status);
            return;
        }
        
        System.out.println("\nApplicants with Status: " + status);
        
        for (Applicant a : filteredApplicants) {
            Project p = projects.stream()
                .filter(proj -> proj.getProjectId() == a.getAppliedProjectId())
                .findFirst().orElse(null);
                
            String projectName = p != null ? p.getProjectName() : "Unknown";
            
            System.out.printf("  - %s (%s) | Age: %d | Marital: %s | Project: %s | Flat: %s\n",
                a.getName(), a.getNric(), a.getAge(), a.getMaritalStatus(), projectName, 
                a.getAppliedFlatType());
        }
        
        // Additional statistics for status report
        long type2RoomCount = filteredApplicants.stream()
            .filter(a -> "2-Room".equalsIgnoreCase(a.getAppliedFlatType()))
            .count();
            
        long type3RoomCount = filteredApplicants.stream()
            .filter(a -> "3-Room".equalsIgnoreCase(a.getAppliedFlatType()))
            .count();
            
        System.out.println("\nFlat Type Distribution:");
        System.out.println("  2-Room : " + type2RoomCount);
        System.out.println("  3-Room : " + type3RoomCount);
        
        long singleCount = filteredApplicants.stream()
            .filter(a -> "Single".equalsIgnoreCase(a.getMaritalStatus()))
            .count();
            
        long marriedCount = filteredApplicants.stream()
            .filter(a -> "Married".equalsIgnoreCase(a.getMaritalStatus()))
            .count();
            
        System.out.println("\nMarital Status Distribution:");
        System.out.println("  Single  : " + singleCount);
        System.out.println("  Married : " + marriedCount);
        
        // Display all successful applications at the end of the report
        displaySuccessfulApplications(projects, applicants);
    }
    
    /**
     * Displays all successful applications from the provided list of applicants.
     * This method is called at the end of each report to ensure managers can always see
     * successful applications regardless of the filter being applied.
     */
    private void displaySuccessfulApplications(List<Project> projects, List<Applicant> allApplicants) {
        List<Applicant> successfulApplicants = allApplicants.stream()
            .filter(a -> a.getApplicationStatus() == ApplicationStatus.SUCCESSFUL)
            .collect(Collectors.toList());
            
        if (successfulApplicants.isEmpty()) {
            System.out.println("\n--- No Successful Applications Found ---");
            return;
        }
        
        System.out.println("\n==============================================");
        System.out.println("SUCCESSFUL APPLICATIONS SUMMARY");
        System.out.println("==============================================");
        
        for (Applicant a : successfulApplicants) {
            Project p = projects.stream()
                .filter(proj -> proj.getProjectId() == a.getAppliedProjectId())
                .findFirst().orElse(null);
                
            String projectName = p != null ? p.getProjectName() : "Unknown";
            String neighborhood = p != null ? p.getNeighborhood() : "Unknown";
            
            System.out.printf("Applicant: %s (%s)\n", a.getName(), a.getNric());
            System.out.printf("  Age: %d | Marital Status: %s\n", a.getAge(), a.getMaritalStatus());
            System.out.printf("  Project: %s @ %s\n", projectName, neighborhood);
            System.out.printf("  Flat Type: %s\n", a.getAppliedFlatType());
            System.out.println("----------------------------------------------");
        }
    }
    
    private void printSummaryStatistics(List<Applicant> applicants) {
        long count2Room = applicants.stream()
            .filter(a -> "2-Room".equalsIgnoreCase(a.getAppliedFlatType()))
            .count();

        long count3Room = applicants.stream()
            .filter(a -> "3-Room".equalsIgnoreCase(a.getAppliedFlatType()))
            .count();

        long countPending = applicants.stream()
            .filter(a -> a.getApplicationStatus() == ApplicationStatus.PENDING)
            .count();

        long countSuccessful = applicants.stream()
            .filter(a -> a.getApplicationStatus() == ApplicationStatus.SUCCESSFUL)
            .count();
            
        long countBooked = applicants.stream()
            .filter(a -> a.getApplicationStatus() == ApplicationStatus.BOOKED)
            .count();

        long countUnsuccessful = applicants.stream()
            .filter(a -> a.getApplicationStatus() == ApplicationStatus.UNSUCCESSFUL)
            .count();

        System.out.println("\nSummary Statistics:");
        System.out.println("  Total Applicants: " + applicants.size());
        System.out.println("  Flat Type Distribution:");
        System.out.println("    2-Room: " + count2Room);
        System.out.println("    3-Room: " + count3Room);
        System.out.println("  Status Distribution:");
        System.out.println("    PENDING     : " + countPending);
        System.out.println("    SUCCESSFUL  : " + countSuccessful);
        System.out.println("    BOOKED      : " + countBooked);
        System.out.println("    UNSUCCESSFUL: " + countUnsuccessful);
    }

    private void processWithdrawalRequests() {
        System.out.println("\n--- Process Withdrawal Requests ---");
        
        List<User> users = FileManager.loadUsersFromCSV(Constants.APPLICANT_CSV, "Applicant");
        List<Project> managedProjects = projectController.getAllProjects().stream()
            .filter(p -> p.getManager().equalsIgnoreCase(manager.getName()))
            .collect(Collectors.toList());
            
        if (managedProjects.isEmpty()) {
            System.out.println("You are not managing any projects.");
            return;
        }
        
        List<Integer> managedProjectIds = managedProjects.stream()
            .map(Project::getProjectId)
            .collect(Collectors.toList());
            
        List<Applicant> pendingWithdrawals = users.stream()
            .filter(u -> u instanceof Applicant)
            .map(u -> (Applicant) u)
            .filter(a -> {
                int pid = a.getAppliedProjectId();
                return pid >= 0 && 
                       a.getApplicationStatus() == ApplicationStatus.PENDING_WITHDRAWAL &&
                       managedProjectIds.contains(pid);
            })
            .collect(Collectors.toList());
            
        if (pendingWithdrawals.isEmpty()) {
            System.out.println("No pending withdrawal requests for your projects.");
            return;
        }
        
        for (Applicant a : pendingWithdrawals) {
            Project p = managedProjects.stream()
                .filter(proj -> proj.getProjectId() == a.getAppliedProjectId())
                .findFirst().orElse(null);
                
            String projectName = p != null ? p.getProjectName() : "Unknown";
            
            System.out.printf("Applicant: %s (%s)\n", a.getName(), a.getNric());
            System.out.printf("  Age: %d | Marital: %s | Project: %s | Flat Type: %s\n",
                a.getAge(), a.getMaritalStatus(), projectName, a.getAppliedFlatType());
            System.out.print("Approve withdrawal (A) / Reject withdrawal (R) / Skip (S): ");
            
            String choice = scanner.nextLine().trim();
            
            if ("A".equalsIgnoreCase(choice)) {
                // If the applicant has a successful or booked application, restore inventory
                if (a.getApplicationStatus() == ApplicationStatus.PENDING_WITHDRAWAL) {
                    if (p != null) {
                        String flatType = a.getAppliedFlatType();
                        if ("2-Room".equalsIgnoreCase(flatType)) {
                            p.setType1Units(p.getType1Units() + 1);
                        } else if ("3-Room".equalsIgnoreCase(flatType)) {
                            p.setType2Units(p.getType2Units() + 1);
                        }
                        FileManager.saveProjects(projectController.getAllProjects());
                        ProjectRepository.init(Constants.PROJECT_CSV);
                    }
                }
                
                boolean success = new com.SC2002.bto.control.ApplicationController().approveWithdrawal(a);
                if (success) {
                    System.out.println("Withdrawal approved. Application has been reset.");
                } else {
                    System.out.println("Error approving withdrawal.");
                }
            } else if ("R".equalsIgnoreCase(choice)) {
                boolean success = new com.SC2002.bto.control.ApplicationController().rejectWithdrawal(a);
                if (success) {
                    System.out.println("Withdrawal rejected. Application status reverted to PENDING.");
                } else {
                    System.out.println("Error rejecting withdrawal.");
                }
            } else {
                System.out.println("Skipped.");
            }
            System.out.println("------------------------------------------------");
        }
    }
    
    private void viewProjects() {
        System.out.println("\n--- View Projects ---");
        System.out.println("1. View All Projects");
        System.out.println("2. View Your Projects");
        System.out.print("Enter your choice: ");
        
        String choice = scanner.nextLine().trim();
        List<Project> projectsToView;
        
        if ("1".equals(choice)) {
            projectsToView = projectController.getAllProjects();
            if (projectsToView.isEmpty()) {
                System.out.println("No projects available in the system.");
                return;
            }
            System.out.println("\nViewing All Projects:");
        } else if ("2".equals(choice)) {
            projectsToView = projectController.getAllProjects().stream()
                .filter(p -> p.getManager().equalsIgnoreCase(manager.getName()))
                .collect(Collectors.toList());
            if (projectsToView.isEmpty()) {
                System.out.println("You are not managing any projects.");
                return;
            }
            System.out.println("\nViewing Your Projects:");
        } else {
            System.out.println("Invalid choice.");
            return;
        }
        
        // Apply filters if requested
        projectsToView = applyFilters(projectsToView);
        
        if (projectsToView.isEmpty()) {
            System.out.println("No projects match the selected filters.");
            return;
        }
        
        // Display projects with detailed information
        displayProjectDetails(projectsToView);
    }
    
    private List<Project> applyFilters(List<Project> projects) {
        System.out.println("\nFilter Options:");
        System.out.println("1. Filter by Location (Neighborhood)");
        System.out.println("2. Filter by Flat Type");
        System.out.println("3. Filter by Price Range");
        System.out.println("4. Filter by Application Period");
        System.out.println("5. Filter by Visibility");
        System.out.println("6. No Filter (Show All)");
        System.out.print("Enter your choice: ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                return filterByLocation(projects);
            case "2":
                return filterByFlatType(projects);
            case "3":
                return filterByPriceRange(projects);
            case "4":
                return filterByApplicationPeriod(projects);
            case "5":
                return filterByVisibility(projects);
            case "6":
                return projects;
            default:
                System.out.println("Invalid choice. Showing all projects.");
                return projects;
        }
    }
    
    private List<Project> filterByLocation(List<Project> projects) {
        // Get unique neighborhoods
        List<String> neighborhoods = projects.stream()
            .map(Project::getNeighborhood)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
            
        System.out.println("\nAvailable Neighborhoods:");
        for (int i = 0; i < neighborhoods.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, neighborhoods.get(i));
        }
        
        System.out.print("Enter neighborhood number: ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (index >= 0 && index < neighborhoods.size()) {
                String selectedNeighborhood = neighborhoods.get(index);
                return projects.stream()
                    .filter(p -> p.getNeighborhood().equalsIgnoreCase(selectedNeighborhood))
                    .collect(Collectors.toList());
            } else {
                System.out.println("Invalid selection. Showing all projects.");
                return projects;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Showing all projects.");
            return projects;
        }
    }
    
    private List<Project> filterByFlatType(List<Project> projects) {
        System.out.println("\nSelect Flat Type:");
        System.out.println("1. 2-Room");
        System.out.println("2. 3-Room");
        System.out.print("Enter your choice: ");
        
        String choice = scanner.nextLine().trim();
        
        if ("1".equals(choice)) {
            return projects.stream()
                .filter(p -> p.getType1Desc().contains("2-Room"))
                .collect(Collectors.toList());
        } else if ("2".equals(choice)) {
            return projects.stream()
                .filter(p -> p.getType2Desc().contains("3-Room"))
                .collect(Collectors.toList());
        } else {
            System.out.println("Invalid choice. Showing all projects.");
            return projects;
        }
    }
    
    private List<Project> filterByPriceRange(List<Project> projects) {
        System.out.println("\nEnter Price Range:");
        System.out.print("Minimum Price: ");
        int minPrice;
        try {
            minPrice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            minPrice = 0;
        }
        
        System.out.print("Maximum Price: ");
        int maxPrice;
        try {
            maxPrice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            maxPrice = Integer.MAX_VALUE;
        }
        
        final int finalMinPrice = minPrice;
        final int finalMaxPrice = maxPrice;
        
        return projects.stream()
            .filter(p -> 
                (p.getType1Price() >= finalMinPrice && p.getType1Price() <= finalMaxPrice) ||
                (p.getType2Price() >= finalMinPrice && p.getType2Price() <= finalMaxPrice))
            .collect(Collectors.toList());
    }
    
    private List<Project> filterByApplicationPeriod(List<Project> projects) {
        System.out.println("\nSelect Application Period:");
        System.out.println("1. Currently Open");
        System.out.println("2. Upcoming (Not Yet Open)");
        System.out.println("3. Closed (Past)");
        System.out.print("Enter your choice: ");
        
        String choice = scanner.nextLine().trim();
        LocalDate today = LocalDate.now();
        
        switch (choice) {
            case "1":
                return projects.stream()
                    .filter(p -> !today.isBefore(p.getApplicationOpeningDate()) && 
                                 !today.isAfter(p.getApplicationClosingDate()))
                    .collect(Collectors.toList());
            case "2":
                return projects.stream()
                    .filter(p -> today.isBefore(p.getApplicationOpeningDate()))
                    .collect(Collectors.toList());
            case "3":
                return projects.stream()
                    .filter(p -> today.isAfter(p.getApplicationClosingDate()))
                    .collect(Collectors.toList());
            default:
                System.out.println("Invalid choice. Showing all projects.");
                return projects;
        }
    }
    
    private List<Project> filterByVisibility(List<Project> projects) {
        System.out.println("\nSelect Visibility:");
        System.out.println("1. Visible Projects");
        System.out.println("2. Hidden Projects");
        System.out.print("Enter your choice: ");
        
        String choice = scanner.nextLine().trim();
        
        if ("1".equals(choice)) {
            return projects.stream()
                .filter(Project::isVisible)
                .collect(Collectors.toList());
        } else if ("2".equals(choice)) {
            return projects.stream()
                .filter(p -> !p.isVisible())
                .collect(Collectors.toList());
        } else {
            System.out.println("Invalid choice. Showing all projects.");
            return projects;
        }
    }
    
    private void displayProjectDetails(List<Project> projects) {
        for (Project p : projects) {
            System.out.println("\n==============================================");
            System.out.printf("Project ID: %d | Name: %s\n", p.getProjectId(), p.getProjectName());
            System.out.printf("Neighborhood: %s\n", p.getNeighborhood());
            System.out.printf("Manager: %s\n", p.getManager());
            System.out.printf("Visibility: %s\n", p.isVisible() ? "Visible to Applicants" : "Hidden from Applicants");
            System.out.printf("Application Period: %s to %s\n", 
                p.getApplicationOpeningDate().format(DATE_FMT),
                p.getApplicationClosingDate().format(DATE_FMT));
            
            // Flat type details
            System.out.println("\nFlat Types:");
            System.out.printf("1. %s: %d units available, $%d\n", 
                p.getType1Desc(), p.getType1Units(), p.getType1Price());
            System.out.printf("2. %s: %d units available, $%d\n", 
                p.getType2Desc(), p.getType2Units(), p.getType2Price());
            
            // Officer details
            System.out.println("\nOfficers:");
            if (p.getOfficers().isEmpty()) {
                System.out.println("No officers assigned");
            } else {
                for (String officer : p.getOfficers()) {
                    System.out.println("- " + officer);
                }
            }
            System.out.printf("Officer Slots: %d\n", p.getOfficerSlots());
            
            // Application statistics
            int type1Booked = FileManager.getBookedUnits(p.getProjectId(), p.getType1Desc());
            int type2Booked = FileManager.getBookedUnits(p.getProjectId(), p.getType2Desc());
            
            System.out.println("\nApplication Statistics:");
            System.out.printf("%s: %d/%d units booked\n", 
                p.getType1Desc(), type1Booked, p.getType1Units() + type1Booked);
            System.out.printf("%s: %d/%d units booked\n", 
                p.getType2Desc(), type2Booked, p.getType2Units() + type2Booked);
            
            System.out.println("==============================================");
        }
    }
    
    private void viewAllEnquiries() {
        System.out.println("\n--- All Enquiries for Your Projects ---");

        List<Enquiry> allEnquiries = FileManager.loadAllEnquiries(Constants.ENQUIRY_CSV);
        List<Project> managerProjects = projectController.getAllProjects().stream()
            .filter(p -> p.getManager().equalsIgnoreCase(manager.getName()))
            .collect(Collectors.toList());

        if (managerProjects.isEmpty()) {
            System.out.println("You are not managing any projects.");
            return;
        }

        List<Integer> managedProjectIds = managerProjects.stream()
            .map(Project::getProjectId)
            .collect(Collectors.toList());

        List<Enquiry> filteredEnquiries = allEnquiries.stream()
            .filter(e -> managedProjectIds.contains(e.getProjectId()))
            .sorted((e1, e2) -> {
                boolean r1 = e1.getResponse() == null || e1.getResponse().isEmpty();
                boolean r2 = e2.getResponse() == null || e2.getResponse().isEmpty();
                return Boolean.compare(r1, r2); // Pending first
            })
            .collect(Collectors.toList());

        if (filteredEnquiries.isEmpty()) {
            System.out.println("No enquiries submitted for your projects.");
            return;
        }

        for (Enquiry e : filteredEnquiries) {
            System.out.printf("Enquiry ID: %d | Project ID: %d | From: %s\n",
                e.getEnquiryId(), e.getProjectId(), e.getUserNric());
            System.out.println("  Message : " + e.getEnquiryText());
            System.out.println("  Response: " + (e.getResponse().isEmpty() ? "(Pending)" : e.getResponse()));
            System.out.println("------------------------------------------------");
        }
    }
    
    /**
     * Handles the password change process for managers.
     * 
     * @return true if password was changed successfully and user should be logged out, false otherwise
     */
    private boolean changePassword() {
        System.out.print("Current password: ");
        String currentPassword = scanner.nextLine();
        
        System.out.print("New password: ");
        String newPassword = scanner.nextLine();
        
        if (newPassword.trim().isEmpty()) {
            System.out.println("Password cannot be empty.");
            return false;
        }
        
        if (!manager.getPassword().equals(currentPassword)) {
            System.out.println("Incorrect current password.");
            return false;
        }
        
        if (!InputValidator.validatePassword(newPassword)) {
            System.out.println("Password must be at least 8 characters. Change password failed");
            return false;
        }
        
        manager.setPassword(newPassword);
        boolean updated = FileManager.updatePasswordInCSV(Constants.MANAGER_CSV, manager.getNric(), newPassword);
        
        if (updated) {
            System.out.println("Password updated successfully.");
            System.out.println("For security reasons, you will be logged out and need to re-login with your new password.");
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            return true;
        } else {
            System.out.println("Error updating password.");
        }
        
        if ("password".equalsIgnoreCase(newPassword)) {
            System.out.println("Warning: 'password' is not a secure password.");
        }
        
        return false;
    }
}
