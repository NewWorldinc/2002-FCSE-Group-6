package com.SC2002.bto.control;

import java.util.List;

import com.SC2002.bto.di.ServiceLocator;
import com.SC2002.bto.entities.Applicant;
import com.SC2002.bto.entities.ApplicationStatus;
import com.SC2002.bto.entities.Project;
import com.SC2002.bto.entities.User;
import com.SC2002.bto.service.IApplicationService;
import com.SC2002.bto.service.IProjectService;

/**
 * ReportController handles the generation of reports for the BTO Management System.
 * Follows the Dependency Inversion Principle by depending on service interfaces.
 */
public class ReportController {
    
    private final IProjectService projectService;
    private final IApplicationService applicationService;
    
    /**
     * Constructs a ReportController with the default services.
     */
    public ReportController() {
        this.projectService = ServiceLocator.get(IProjectService.class);
        this.applicationService = ServiceLocator.get(IApplicationService.class);
    }
    
    /**
     * Constructs a ReportController with the specified services.
     * 
     * @param projectService The project service
     * @param applicationService The application service
     */
    public ReportController(IProjectService projectService, IApplicationService applicationService) {
        this.projectService = projectService;
        this.applicationService = applicationService;
    }

    /**
     * Generates a report on applicants, including their status and application details.
     *
     * @param users the list of users to include in the report.
     * @return a formatted string containing the applicant report.
     */
    public String generateApplicantReport(List<User> users) {
        StringBuilder report = new StringBuilder("=== Applicant Report ===\n");

        int total = 0, pending = 0, success = 0, booked = 0, rejected = 0;

        for (User user : users) {
            if (!(user instanceof Applicant)) continue;
            Applicant a = (Applicant) user;
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

        System.out.println("Applicant report generated.");
        return report.toString();
    }

    /**
     * Generates a report on flat bookings, including availability and booking status.
     *
     * @param projects the list of projects to include in the report.
     * @return a formatted string containing the flat booking report.
     */
    public String generateFlatBookingReport(List<Project> projects) {
        StringBuilder report = new StringBuilder("=== Flat Booking Report ===\n");

        for (Project p : projects) {
            int booked2 = projectService.getBookedUnits(p.getProjectId(), "2-Room");
            int booked3 = projectService.getBookedUnits(p.getProjectId(), "3-Room");
            int original2 = p.getType1Units() + booked2;
            int original3 = p.getType2Units() + booked3;

            report.append("Project: ").append(p.getProjectName()).append(" @ ").append(p.getNeighborhood()).append("\n")
                  .append("2-Room: ").append(p.getType1Units()).append(" left / ").append(booked2).append(" booked out of ").append(original2).append("\n")
                  .append("3-Room: ").append(p.getType2Units()).append(" left / ").append(booked3).append(" booked out of ").append(original3).append("\n")
                  .append("Visible: ").append(p.isVisible()).append("\n")
                  .append("Application Period: ").append(p.getApplicationOpeningDate())
                  .append(" to ").append(p.getApplicationClosingDate()).append("\n")
                  .append("------------------------------------------------\n");
        }

        System.out.println("Flat booking report generated.");
        return report.toString();
    }
}
