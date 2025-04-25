import com.SC2002.bto.boundary.CLI;
import com.SC2002.bto.di.ServiceLocator;
import com.SC2002.bto.utils.FileManager;
import com.SC2002.bto.repository.IApplicationRepository;
import com.SC2002.bto.repository.IEnquiryRepository;
import com.SC2002.bto.repository.IOfficerRegistrationRepository;
import com.SC2002.bto.repository.IProjectRepository;
import com.SC2002.bto.repository.IUserRepository;
import com.SC2002.bto.repository.RepositoryFactory;
import com.SC2002.bto.service.IApplicationService;
import com.SC2002.bto.service.IEnquiryService;
import com.SC2002.bto.service.IOfficerRegistrationService;
import com.SC2002.bto.service.IProjectService;
import com.SC2002.bto.service.IReportService;
import com.SC2002.bto.service.IUserService;
import com.SC2002.bto.service.IValidationService;
import com.SC2002.bto.service.impl.ApplicationService;
import com.SC2002.bto.service.impl.EnquiryService;
import com.SC2002.bto.service.impl.OfficerRegistrationService;
import com.SC2002.bto.service.impl.ProjectService;
import com.SC2002.bto.service.impl.ReportService;
import com.SC2002.bto.service.impl.UserService;
import com.SC2002.bto.service.impl.ValidationService;

/**
 * Main class that initializes the application.
 * Follows the Dependency Inversion Principle by initializing repositories and services.
 */
public class Main {
    public static void main(String[] args) {
        // Ensure data directory exists before any file operations
        boolean dirExists = FileManager.ensureDataDirectoryExists();
        if (!dirExists) {
            System.out.println("Error: Could not create data directory. Exiting application.");
            return;
        }
        
        // Get repositories from the factory
        IProjectRepository projectRepository = RepositoryFactory.getProjectRepository();
        IEnquiryRepository enquiryRepository = RepositoryFactory.getEnquiryRepository();
        IApplicationRepository applicationRepository = RepositoryFactory.getApplicationRepository();
        IOfficerRegistrationRepository officerRegistrationRepository = RepositoryFactory.getOfficerRegistrationRepository();
        IUserRepository userRepository = RepositoryFactory.getUserRepository();
        
        // Initialize services
        IProjectService projectService = new ProjectService(projectRepository);
        IEnquiryService enquiryService = new EnquiryService(enquiryRepository);
        IApplicationService applicationService = new ApplicationService(applicationRepository, projectRepository);
        IOfficerRegistrationService officerRegistrationService = new OfficerRegistrationService(officerRegistrationRepository, projectRepository);
        IReportService reportService = new ReportService(projectRepository, applicationRepository, userRepository);
        IUserService userService = new UserService(userRepository);
        IValidationService validationService = new ValidationService();
        
        // Register repositories in the ServiceLocator
        ServiceLocator.register(IProjectRepository.class, projectRepository);
        ServiceLocator.register(IEnquiryRepository.class, enquiryRepository);
        ServiceLocator.register(IApplicationRepository.class, applicationRepository);
        ServiceLocator.register(IOfficerRegistrationRepository.class, officerRegistrationRepository);
        ServiceLocator.register(IUserRepository.class, userRepository);
        
        // Register services in the ServiceLocator
        ServiceLocator.register(IProjectService.class, projectService);
        ServiceLocator.register(IEnquiryService.class, enquiryService);
        ServiceLocator.register(IApplicationService.class, applicationService);
        ServiceLocator.register(IOfficerRegistrationService.class, officerRegistrationService);
        ServiceLocator.register(IReportService.class, reportService);
        ServiceLocator.register(IUserService.class, userService);
        ServiceLocator.register(IValidationService.class, validationService);
        
        // Start the CLI
        new CLI().start();
    }
}
