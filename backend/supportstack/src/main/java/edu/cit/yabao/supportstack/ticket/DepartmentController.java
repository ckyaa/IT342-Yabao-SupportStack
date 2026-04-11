package edu.cit.yabao.supportstack.ticket;

import edu.cit.yabao.supportstack.dto.ApiResponse;
import edu.cit.yabao.supportstack.model.Department;
import edu.cit.yabao.supportstack.service.DepartmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments() {
        List<Department> departments = departmentService.getAllActiveDepartments();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }
}
