package com.SC2002.bto.service.impl;

import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.ApplicationStatus;
import com.SC2002.bto.entities.Project;
import com.SC2002.bto.entities.User;
import com.SC2002.bto.repository.IApplicationRepository;
import com.SC2002.bto.repository.IProjectRepository;
import com.SC2002.bto.repository.IUserRepository;
import com.SC2002.bto.service.IReportService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the report service.
 * Follows the Dependency Inversion Principle by depending on repository interfaces.
 */
public class ReportService implements IReportService {
    
    private final IProjectRepository projectRepository;
    private final IApplicationRepository applicationRepository;
    private final IUserRepository userRepository;
    
    /**
     * Constructs a ReportService with the specified repositories.
     * 
     * @param projectRepository The project repository
     * @param applicationRepository The application repository
     * @param userRepository The user repository
     */
    public ReportService(IProjectRepository projectRepository, IApplicationRepository applicationRepository, IUserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public String generateApplicantReport(List<Applicant> applicants) {
        StringBuilder report = new StringBuilder("=== Applicant Report ===\n");

        int total = 0, pending = 0, success = 0, booked = 0, rejected = 0;

        for (Applicant a : applicants) {
            total++;

            report.append("NRIC: ").append(a.getNric())
                  .append(" | Name: ").append(a.getName())
                  .append(" | Status: ").append(a.getApplicationStatus())
                  .append(" | Project ID: ").append(a.getAppliedProjectId())
                  .append(" | Flat Type: ").append(a.getAppliedFlatType())
                  .append("\n");

            switch (a.getApplicationStatus()) {
                case PENDING -> pending++;
                case SUCCESSFUL -> success++;
                case BOOKED -> booked++;
                case UNSUCCESSFUL -> rejected++;
            }
        }

        report.append("\nSummary:\n")
              .append("Total Applicants: ").append(total).append("\n")
              .append("Pending: ").append(pending).append("\n")
              .append("Successful: ").append(success).append("\n")
              .append("Booked: ").append(booked).append("\n")
              .append("Rejected: ").append(rejected).append("\n");

        return report.toString();
    }
    
    @Override
    public String generateFlatBookingReport(List<Project> projects) {
        StringBuilder report = new StringBuilder("=== Flat Booking Report ===\n");

        for (Project p : projects) {
            // Get booked units for each flat type
            int booked2Room = getBookedUnitsCount(p.getProjectId(), "2-Room");
            int booked3Room = getBookedUnitsCount(p.getProjectId(), "3-Room");
            
            // Calculate original units
            int original2Room = p.getType1Units() + booked2Room;
            int original3Room = p.getType2Units() + booked3Room;

            report.append("Project: ").append(p.getProjectName()).append(" @ ").append(p.getNeighborhood()).append("\n")
                  .append("2-Room: ").append(p.getType1Units()).append(" left / ").append(booked2Room).append(" booked out of ").append(original2Room).append("\n")
                  .append("3-Room: ").append(p.getType2Units()).append(" left / ").append(booked3Room).append(" booked out of ").append(original3Room).append("\n")
                  .append("Visible: ").append(p.isVisible()).append("\n")
                  .append("Application Period: ").append(p.getApplicationOpeningDate())
                  .append(" to ").append(p.getApplicationClosingDate()).append("\n")
                  .append("------------------------------------------------\n");
        }

        return report.toString();
    }
    
    @Override
    public String generateProjectReport(List<Project> projects) {
        StringBuilder report = new StringBuilder("=== Project Report ===\n");
        
        for (Project p : projects) {
            // Get applications for this project
            List<Applicant> applicants = applicationRepository.findByProject(p.getProjectId());
            
            // Count applications by status
            Map<ApplicationStatus, Long> statusCounts = applicants.stream()
                .collect(Collectors.groupingBy(Applicant::getApplicationStatus, Collectors.counting()));
            
            report.append("Project: ").append(p.getProjectName()).append(" @ ").append(p.getNeighborhood()).append("\n")
                  .append("Total Applications: ").append(applicants.size()).append("\n")
                  .append("Pending: ").append(statusCounts.getOrDefault(ApplicationStatus.PENDING, 0L)).append("\n")
                  .append("Successful: ").append(statusCounts.getOrDefault(ApplicationStatus.SUCCESSFUL, 0L)).append("\n")
                  .append("Booked: ").append(statusCounts.getOrDefault(ApplicationStatus.BOOKED, 0L)).append("\n")
                  .append("Unsuccessful: ").append(statusCounts.getOrDefault(ApplicationStatus.UNSUCCESSFUL, 0L)).append("\n")
                  .append("Pending Withdrawal: ").append(statusCounts.getOrDefault(ApplicationStatus.PENDING_WITHDRAWAL, 0L)).append("\n")
                  .append("------------------------------------------------\n");
        }
        
        return report.toString();
    }
    
    @Override
    public String generateApplicantReportByMaritalStatus(String maritalStatus) {
        // Get all applicants
        List<User> users = userRepository.findByUserType("Applicant");
        
        // Filter by marital status
        List<Applicant> filteredApplicants = users.stream()
            .filter(user -> user instanceof Applicant)
            .map(user -> (Applicant) user)
            .filter(a -> a.getMaritalStatus().equalsIgnoreCase(maritalStatus))
            .collect(Collectors.toList());
        
        StringBuilder report = new StringBuilder("=== Applicant Report by Marital Status: " + maritalStatus + " ===\n");
        report.append("Total Applicants: ").append(filteredApplicants.size()).append("\n\n");
        
        return report.toString() + generateApplicantReport(filteredApplicants);
    }
    
    @Override
    public String generateApplicantReportByAgeRange(int minAge, int maxAge) {
        // Get all applicants
        List<User> users = userRepository.findByUserType("Applicant");
        
        // Filter by age range
        List<Applicant> filteredApplicants = users.stream()
            .filter(user -> user instanceof Applicant)
            .map(user -> (Applicant) user)
            .filter(a -> a.getAge() >= minAge && a.getAge() <= maxAge)
            .collect(Collectors.toList());
        
        StringBuilder report = new StringBuilder("=== Applicant Report by Age Range: " + minAge + " to " + maxAge + " ===\n");
        report.append("Total Applicants: ").append(filteredApplicants.size()).append("\n\n");
        
        return report.toString() + generateApplicantReport(filteredApplicants);
    }
    
    @Override
    public String generateApplicantReportByFlatType(String flatType) {
        List<Applicant> filteredApplicants = applicationRepository.findByFlatType(flatType);
        
        StringBuilder report = new StringBuilder("=== Applicant Report by Flat Type: " + flatType + " ===\n");
        report.append("Total Applicants: ").append(filteredApplicants.size()).append("\n\n");
        
        return report.toString() + generateApplicantReport(filteredApplicants);
    }
    
    @Override
    public String generateApplicantReportByStatus(String status) {
        ApplicationStatus applicationStatus;
        try {
            applicationStatus = ApplicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "Invalid application status: " + status;
        }
        
        List<Applicant> filteredApplicants = applicationRepository.findByStatus(applicationStatus);
        
        StringBuilder report = new StringBuilder("=== Applicant Report by Status: " + status + " ===\n");
        report.append("Total Applicants: ").append(filteredApplicants.size()).append("\n\n");
        
        return report.toString() + generateApplicantReport(filteredApplicants);
    }
    
    @Override
    public String generateApplicantReportByProject(int projectId) {
        List<Applicant> filteredApplicants = applicationRepository.findByProject(projectId);
        
        // Get project name
        String projectName = projectRepository.findById(projectId)
            .map(Project::getProjectName)
            .orElse("Unknown Project");
        
        StringBuilder report = new StringBuilder("=== Applicant Report by Project: " + projectName + " (ID: " + projectId + ") ===\n");
        report.append("Total Applicants: ").append(filteredApplicants.size()).append("\n\n");
        
        return report.toString() + generateApplicantReport(filteredApplicants);
    }
    
    @Override
    public String generateReceipt(String applicantNric) {
        // Find the applicant
        Optional<User> userOpt = userRepository.findByNric(applicantNric);
        
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Applicant)) {
            return "Applicant with NRIC " + applicantNric + " not found.";
        }
        
        Applicant applicant = (Applicant) userOpt.get();
        
        // Check if the applicant has a booked flat
        if (applicant.getApplicationStatus() != ApplicationStatus.BOOKED) {
            return "Applicant with NRIC " + applicantNric + " has not booked a flat.";
        }
        
        // Get project details
        Optional<Project> projectOpt = projectRepository.findById(applicant.getAppliedProjectId());
        if (projectOpt.isEmpty()) {
            return "Project with ID " + applicant.getAppliedProjectId() + " not found.";
        }
        
        Project project = projectOpt.get();
        
        // Generate receipt
        StringBuilder receipt = new StringBuilder("=== BTO Flat Booking Receipt ===\n\n");
        receipt.append("Applicant Details:\n");
        receipt.append("NRIC: ").append(applicant.getNric()).append("\n");
        receipt.append("Name: ").append(applicant.getName()).append("\n");
        receipt.append("Age: ").append(applicant.getAge()).append("\n");
        receipt.append("Marital Status: ").append(applicant.getMaritalStatus()).append("\n\n");
        
        receipt.append("Booking Details:\n");
        receipt.append("Project: ").append(project.getProjectName()).append(" @ ").append(project.getNeighborhood()).append("\n");
        receipt.append("Flat Type: ").append(applicant.getAppliedFlatType()).append("\n");
        
        // Calculate price
        int price = 0;
        if ("2-Room".equalsIgnoreCase(applicant.getAppliedFlatType())) {
            price = project.getType1Price();
        } else if ("3-Room".equalsIgnoreCase(applicant.getAppliedFlatType())) {
            price = project.getType2Price();
        }
        
        receipt.append("Price: $").append(price).append("\n\n");
        
        receipt.append("Status: ").append(applicant.getApplicationStatus()).append("\n\n");
        
        receipt.append("Thank you for choosing HDB BTO!\n");
        
        return receipt.toString();
    }
    
    @Override
    public boolean exportReportToFile(String report, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(report);
            return true;
        } catch (IOException e) {
            System.out.println("Error exporting report to file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the count of booked units for a specific project and flat type.
     * 
     * @param projectId The ID of the project
     * @param flatType The flat type (e.g., "2-Room", "3-Room")
     * @return The count of booked units
     */
    private int getBookedUnitsCount(int projectId, String flatType) {
        return (int) applicationRepository.findByProject(projectId).stream()
            .filter(a -> a.getApplicationStatus() == ApplicationStatus.BOOKED)
            .filter(a -> a.getAppliedFlatType().equalsIgnoreCase(flatType))
            .count();
    }
}
