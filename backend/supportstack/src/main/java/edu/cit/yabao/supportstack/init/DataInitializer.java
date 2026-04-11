package edu.cit.yabao.supportstack.init;

import edu.cit.yabao.supportstack.model.Department;
import edu.cit.yabao.supportstack.model.User;
import edu.cit.yabao.supportstack.repository.DepartmentRepository;
import edu.cit.yabao.supportstack.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DataInitializer(DepartmentRepository departmentRepository, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            initializeDepartments();
        } catch (Exception e) {
            System.err.println("Warning: Failed to initialize departments: " + e.getMessage());
            // App continues even if seeding fails
        }
    }

    private void initializeDepartments() {
        // Only seed if no departments exist
        if (departmentRepository.count() > 0) {
            return;
        }

        // Try to get or create a system user for department creation
        User systemUser = null;
        try {
            systemUser = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("system", "system@supportstack.local")
                    .orElse(null);

            if (systemUser == null) {
                systemUser = new User();
                systemUser.setName("System");
                systemUser.setUsername("system");
                systemUser.setEmail("system@supportstack.local");
                systemUser.setPasswordHash("");
                systemUser = userRepository.save(systemUser);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not create system user for departments: " + e.getMessage());
            return; // Cannot proceed without a user
        }

        // Create sample departments
        String[][] departments = {
                {"IT", "Information Technology", "Technology and network support"},
                {"HR", "Human Resources", "Human resources and payroll"},
                {"FIN", "Finance", "Accounting and finance matters"},
                {"ADMIN", "Administration", "Administrative support"},
                {"ACAD", "Academic", "Academic and course-related support"}
        };

        for (String[] deptData : departments) {
            try {
                if (departmentRepository.findByCodeIgnoreCase(deptData[0]).isEmpty()) {
                    Department dept = new Department();
                    dept.setCode(deptData[0]);
                    dept.setName(deptData[1]);
                    dept.setDescription(deptData[2]);
                    dept.setCreatedByUser(systemUser);
                    dept.setIsActive(true);
                    departmentRepository.save(dept);
                    System.out.println("Seeded department: " + deptData[0]);
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to create department " + deptData[0] + ": " + e.getMessage());
            }
        }
    }
}
