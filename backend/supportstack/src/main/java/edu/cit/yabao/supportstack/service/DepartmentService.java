package edu.cit.yabao.supportstack.service;

import edu.cit.yabao.supportstack.exception.InvalidCredentialsException;
import edu.cit.yabao.supportstack.model.Department;
import edu.cit.yabao.supportstack.model.User;
import edu.cit.yabao.supportstack.repository.DepartmentRepository;
import edu.cit.yabao.supportstack.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DepartmentService(DepartmentRepository departmentRepository, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    public List<Department> getAllActiveDepartments() {
        return departmentRepository.findAll().stream()
                .filter(Department::getIsActive)
                .toList();
    }

    public Department getDepartmentByCode(String code) {
        return departmentRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new InvalidCredentialsException("Department not found"));
    }

    public Department createDepartment(String code, String name, String description, String createdByUsername) {
        User createdByUser = userRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(createdByUsername, createdByUsername)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        Department department = new Department();
        department.setCode(code.toUpperCase());
        department.setName(name);
        department.setDescription(description);
        department.setCreatedByUser(createdByUser);
        department.setIsActive(true);

        return departmentRepository.save(department);
    }
}
