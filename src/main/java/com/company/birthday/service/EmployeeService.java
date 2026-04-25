package com.company.birthday.service;

import com.company.birthday.dto.request.EmployeeFormRequest;
import com.company.birthday.dto.response.EmployeeListResponse;
import com.company.birthday.dto.response.UpcomingBirthdayResponse;
import com.company.birthday.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeService {

	Page<EmployeeListResponse> getActiveEmployees(String keyword, Pageable pageable);

	List<UpcomingBirthdayResponse> getUpcomingBirthdays();

	List<Department> getAllDepartments();

	EmployeeListResponse createEmployee(EmployeeFormRequest request);

	EmployeeListResponse updateEmployee(Integer employeeId, EmployeeFormRequest request);

	void deleteEmployee(Integer employeeId);

	java.util.Map<String, Object> importEmployee(MultipartFile file);

	byte[] downloadEmployeeTemplate();
}
